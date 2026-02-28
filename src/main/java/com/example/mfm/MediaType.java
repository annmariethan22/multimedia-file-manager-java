package com.example.mfm;

import java.util.Set;

public enum MediaType {
    IMAGE(Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff")),
    VIDEO(Set.of("mp4", "mkv", "avi", "mov", "wmv", "webm")),
    AUDIO(Set.of("mp3", "wav", "flac", "aac", "ogg", "m4a")),
    DOCUMENT(Set.of("pdf", "doc", "docx", "txt", "rtf", "odt")),
    OTHER(Set.of());

    private final Set<String> extensions;

    MediaType(Set<String> extensions) {
        this.extensions = extensions;
    }

    public static MediaType fromExtension(String extension) {
        String normalized = extension.toLowerCase();
        for (MediaType mediaType : values()) {
            if (mediaType.extensions.contains(normalized)) {
                return mediaType;
            }
        }
        return OTHER;
    }
}
