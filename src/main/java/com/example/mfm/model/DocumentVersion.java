package com.example.mfm.model;

import java.time.LocalDateTime;

public class DocumentVersion {
    private int version;
    private String content;
    private LocalDateTime updatedAt;

    public DocumentVersion() {
    }

    public DocumentVersion(int version, String content, LocalDateTime updatedAt) {
        this.version = version;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
