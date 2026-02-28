package com.example.mfm.service;

import com.example.mfm.model.*;
import com.example.mfm.persistence.JsonStorage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DocumentManagementService {
    private final JsonStorage storage;
    private final AppState state;

    public DocumentManagementService(JsonStorage storage) throws IOException {
        this.storage = storage;
        this.state = storage.load();
        bootstrapDefaults();
    }

    /** Persists all in-memory changes into the JSON files. */
    public void shutdown() throws IOException { storage.save(state); }

    /** Authenticates a user using username/password and returns the user if credentials are valid. */
    public Optional<User> login(String username, String password) {
        return state.getUsers().stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst();
    }

    /** Returns all categories in the system sorted by name. */
    public List<Category> getCategories() {
        return state.getCategories().stream().sorted(Comparator.comparing(Category::getName)).toList();
    }

    /** Creates a new category. Admin only. */
    public Category addCategory(User actor, String categoryName) {
        requireRole(actor, Role.ADMIN);
        boolean exists = state.getCategories().stream().anyMatch(c -> c.getName().equalsIgnoreCase(categoryName));
        if (exists) throw new IllegalArgumentException("Category already exists");
        Category c = new Category(UUID.randomUUID().toString(), categoryName);
        state.getCategories().add(c);
        return c;
    }

    /** Renames an existing category. Admin only. */
    public void renameCategory(User actor, String categoryId, String newName) {
        requireRole(actor, Role.ADMIN);
        Category category = findCategory(categoryId);
        category.setName(newName);
    }

    /** Deletes a category and all documents in that category. Admin only. */
    public void deleteCategory(User actor, String categoryId) {
        requireRole(actor, Role.ADMIN);
        state.getCategories().removeIf(c -> c.getId().equals(categoryId));
        Set<String> removedDocs = state.getDocuments().stream().filter(d -> d.getCategoryId().equals(categoryId)).map(Document::getId).collect(Collectors.toSet());
        state.getDocuments().removeIf(d -> removedDocs.contains(d.getId()));
        state.getUsers().forEach(u -> u.getWatchedDocumentIds().removeIf(removedDocs::contains));
    }

    /** Returns all users. Admin only. */
    public List<User> getUsers(User actor) {
        requireRole(actor, Role.ADMIN);
        return List.copyOf(state.getUsers());
    }

    /** Creates a user. Admin only. */
    public User addUser(User actor, String firstName, String lastName, Role role, Set<String> categoryIds, String username, String password) {
        requireRole(actor, Role.ADMIN);
        if (state.getUsers().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
            throw new IllegalArgumentException("Username already exists");
        }
        User u = new User(UUID.randomUUID().toString(), firstName, lastName, role, categoryIds, username, password);
        state.getUsers().add(u);
        return u;
    }

    /** Deletes a user. Admin only. */
    public void deleteUser(User actor, String userId) {
        requireRole(actor, Role.ADMIN);
        state.getUsers().removeIf(u -> u.getId().equals(userId));
    }

    /** Creates a document with version 1. Author or Admin only, in accessible category. */
    public Document createDocument(User actor, String title, String categoryId, String content) {
        requireAtLeastAuthor(actor);
        if (!actor.getAccessibleCategoryIds().contains(categoryId) && actor.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("No access to category");
        }
        Document doc = new Document(UUID.randomUUID().toString(), title, actor.getId(), actor.displayName(), categoryId, LocalDateTime.now());
        doc.getVersions().add(new DocumentVersion(1, content, LocalDateTime.now()));
        state.getDocuments().add(doc);
        return doc;
    }

    /** Updates a document by adding a new version with incremented version number. */
    public DocumentVersion updateDocument(User actor, String documentId, String content) {
        requireAtLeastAuthor(actor);
        Document document = findDocument(documentId);
        if (actor.getRole() != Role.ADMIN && !Objects.equals(document.getAuthorUserId(), actor.getId())) {
            throw new IllegalArgumentException("Only owner author or admin can update");
        }
        int nextVersion = document.latestVersion().getVersion() + 1;
        DocumentVersion version = new DocumentVersion(nextVersion, content, LocalDateTime.now());
        document.getVersions().add(version);
        return version;
    }

    /** Deletes a document and cleans watchers for that document. */
    public void deleteDocument(User actor, String documentId) {
        requireAtLeastAuthor(actor);
        Document document = findDocument(documentId);
        if (actor.getRole() != Role.ADMIN && !Objects.equals(document.getAuthorUserId(), actor.getId())) {
            throw new IllegalArgumentException("Only owner author or admin can delete");
        }
        state.getDocuments().removeIf(d -> d.getId().equals(documentId));
        state.getUsers().forEach(u -> u.getWatchedDocumentIds().remove(documentId));
    }

    /** Searches visible documents by optional category id, title term and author term. */
    public List<Document> searchDocuments(User user, String categoryId, String titleTerm, String authorTerm) {
        Predicate<Document> visibility = d -> user.getRole() == Role.ADMIN || user.getAccessibleCategoryIds().contains(d.getCategoryId());
        Predicate<Document> byCategory = d -> categoryId == null || categoryId.isBlank() || d.getCategoryId().equals(categoryId);
        Predicate<Document> byTitle = d -> titleTerm == null || titleTerm.isBlank() || d.getTitle().toLowerCase().contains(titleTerm.toLowerCase());
        Predicate<Document> byAuthor = d -> authorTerm == null || authorTerm.isBlank() || d.getAuthorName().toLowerCase().contains(authorTerm.toLowerCase());

        return state.getDocuments().stream()
                .filter(visibility.and(byCategory).and(byTitle).and(byAuthor))
                .sorted(Comparator.comparing(Document::getTitle))
                .toList();
    }

    /** Returns versions visible to user: latest for simple users, latest+2 previous for author/admin. */
    public List<DocumentVersion> getVisibleVersions(User user, String documentId) {
        Document document = findDocument(documentId);
        List<DocumentVersion> versions = new ArrayList<>(document.getVersions());
        versions.sort(Comparator.comparingInt(DocumentVersion::getVersion).reversed());
        if (user.getRole() == Role.SIMPLE_USER) {
            return versions.isEmpty() ? List.of() : List.of(versions.get(0));
        }
        return versions.stream().limit(3).toList();
    }

    /** Adds a document to the user's watch list. */
    public void watchDocument(User user, String documentId) { user.getWatchedDocumentIds().add(documentId); }

    /** Removes a document from the user's watch list. */
    public void unwatchDocument(User user, String documentId) { user.getWatchedDocumentIds().remove(documentId); }

    /** Returns documents currently watched by the user and still existing in state. */
    public List<Document> getWatchedDocuments(User user) {
        Set<String> watchedIds = user.getWatchedDocumentIds();
        return state.getDocuments().stream().filter(d -> watchedIds.contains(d.getId())).toList();
    }

    /** Computes notifications for watched documents that have a newer unseen latest version. */
    public List<String> collectWatchNotifications(User user) {
        List<String> notifications = new ArrayList<>();
        for (Document d : getWatchedDocuments(user)) {
            DocumentVersion latest = d.latestVersion();
            String key = d.getId() + ":" + latest.getVersion();
            if (!user.getLastSeenVersionKeys().contains(key)) {
                notifications.add("Το έγγραφο '" + d.getTitle() + "' έχει νέα έκδοση: v" + latest.getVersion());
                user.getLastSeenVersionKeys().removeIf(existing -> existing.startsWith(d.getId() + ":"));
                user.getLastSeenVersionKeys().add(key);
            }
        }
        return notifications;
    }

    /** Returns system stats for current user: categories, documents, watched documents. */
    public Map<String, Integer> getStats(User user) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("categories", state.getCategories().size());
        stats.put("documents", state.getDocuments().size());
        stats.put("watched", getWatchedDocuments(user).size());
        return stats;
    }

    private Document findDocument(String documentId) {
        return state.getDocuments().stream().filter(d -> d.getId().equals(documentId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    private Category findCategory(String categoryId) {
        return state.getCategories().stream().filter(c -> c.getId().equals(categoryId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    private void requireAtLeastAuthor(User actor) {
        if (actor.getRole() == Role.SIMPLE_USER) throw new IllegalArgumentException("Author or admin required");
    }

    private void requireRole(User actor, Role required) {
        if (actor.getRole() != required) throw new IllegalArgumentException("Role " + required + " required");
    }

    private void bootstrapDefaults() {
        if (state.getCategories().isEmpty()) {
            state.getCategories().add(new Category(UUID.randomUUID().toString(), "General"));
            state.getCategories().add(new Category(UUID.randomUUID().toString(), "Research"));
        }
        if (state.getUsers().isEmpty()) {
            Set<String> allCategoryIds = state.getCategories().stream().map(Category::getId).collect(Collectors.toSet());
            User admin = new User(UUID.randomUUID().toString(), "Default", "Admin", Role.ADMIN, allCategoryIds, "medialab", "medialab_2025");
            state.getUsers().add(admin);
        }
    }
}
