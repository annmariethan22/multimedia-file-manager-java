package com.example.mfm.model;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String id;
    private String firstName;
    private String lastName;
    private Role role;
    private Set<String> accessibleCategoryIds = new HashSet<>();
    private String username;
    private String password;
    private Set<String> watchedDocumentIds = new HashSet<>();
    private Set<String> lastSeenVersionKeys = new HashSet<>();

    public User() {
    }

    public User(String id, String firstName, String lastName, Role role, Set<String> accessibleCategoryIds, String username, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.accessibleCategoryIds = new HashSet<>(accessibleCategoryIds);
        this.username = username;
        this.password = password;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Set<String> getAccessibleCategoryIds() { return accessibleCategoryIds; }
    public void setAccessibleCategoryIds(Set<String> accessibleCategoryIds) { this.accessibleCategoryIds = accessibleCategoryIds; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<String> getWatchedDocumentIds() { return watchedDocumentIds; }
    public void setWatchedDocumentIds(Set<String> watchedDocumentIds) { this.watchedDocumentIds = watchedDocumentIds; }
    public Set<String> getLastSeenVersionKeys() { return lastSeenVersionKeys; }
    public void setLastSeenVersionKeys(Set<String> lastSeenVersionKeys) { this.lastSeenVersionKeys = lastSeenVersionKeys; }

    public String displayName() {
        return firstName + " " + lastName;
    }
}
