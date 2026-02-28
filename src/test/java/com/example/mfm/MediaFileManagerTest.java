package com.example.mfm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaFileManagerTest {

    private final MediaFileManager manager = new MediaFileManager();

    @Test
    void groupByMediaTypeCategorizesFiles(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("photo.jpg"), "a");
        Files.writeString(tempDir.resolve("song.mp3"), "b");
        Files.writeString(tempDir.resolve("notes.txt"), "c");
        Files.writeString(tempDir.resolve("archive.bin"), "d");

        Map<MediaType, List<Path>> grouped = manager.groupByMediaType(tempDir);

        assertEquals(1, grouped.get(MediaType.IMAGE).size());
        assertEquals(1, grouped.get(MediaType.AUDIO).size());
        assertEquals(1, grouped.get(MediaType.DOCUMENT).size());
        assertEquals(1, grouped.get(MediaType.OTHER).size());
    }

    @Test
    void organizeByTypeCopiesFiles(@TempDir Path tempDir) throws IOException {
        Path source = Files.createDirectories(tempDir.resolve("source"));
        Path destination = Files.createDirectories(tempDir.resolve("destination"));

        Files.writeString(source.resolve("photo.jpg"), "img");
        Files.writeString(source.resolve("video.mp4"), "vid");

        List<Path> copied = manager.organizeByType(source, destination, false);

        assertEquals(2, copied.size());
        assertTrue(Files.exists(destination.resolve("image/photo.jpg")));
        assertTrue(Files.exists(destination.resolve("video/video.mp4")));
        assertTrue(Files.exists(source.resolve("photo.jpg")));
    }

    @Test
    void organizeByTypeMovesFiles(@TempDir Path tempDir) throws IOException {
        Path source = Files.createDirectories(tempDir.resolve("source"));
        Path destination = Files.createDirectories(tempDir.resolve("destination"));

        Files.writeString(source.resolve("song.mp3"), "audio");

        manager.organizeByType(source, destination, true);

        assertFalse(Files.exists(source.resolve("song.mp3")));
        assertTrue(Files.exists(destination.resolve("audio/song.mp3")));
    }
}
