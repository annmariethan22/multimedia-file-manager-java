package com.example.mfm.model;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private List<User> users = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Document> documents = new ArrayList<>();

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }
}
