package com.example.mfm.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Document {
    private String id;
    private String title;
    private String authorUserId;
    private String authorName;
    private String categoryId;
    private LocalDateTime createdAt;
    private List<DocumentVersion> versions = new ArrayList<>();

    public Document() {
    }

    public Document(String id, String title, String authorUserId, String authorName, String categoryId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.authorUserId = authorUserId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(String authorUserId) { this.authorUserId = authorUserId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<DocumentVersion> getVersions() { return versions; }
    public void setVersions(List<DocumentVersion> versions) { this.versions = versions; }

    public DocumentVersion latestVersion() {
        return versions.stream().max(Comparator.comparingInt(DocumentVersion::getVersion)).orElse(null);
    }
}
