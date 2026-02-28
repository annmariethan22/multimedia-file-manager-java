package com.example.mfm.persistence;

import com.example.mfm.model.AppState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonStorage {
    private final Path folder;
    private final Path stateFile;
    private final ObjectMapper mapper;

    public JsonStorage(Path folder) {
        this.folder = folder;
        this.stateFile = folder.resolve("state.json");
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public AppState load() throws IOException {
        Files.createDirectories(folder);
        if (!Files.exists(stateFile)) {
            return new AppState();
        }
        return mapper.readValue(stateFile.toFile(), AppState.class);
    }

    public void save(AppState state) throws IOException {
        Files.createDirectories(folder);
        mapper.writerWithDefaultPrettyPrinter().writeValue(stateFile.toFile(), state);
    }
}
