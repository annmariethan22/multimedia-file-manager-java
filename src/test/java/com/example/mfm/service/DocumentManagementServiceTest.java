package com.example.mfm.service;

import com.example.mfm.model.Document;
import com.example.mfm.model.Role;
import com.example.mfm.model.User;
import com.example.mfm.persistence.JsonStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DocumentManagementServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void adminBootstrapAndLoginWorks() throws Exception {
        DocumentManagementService service = new DocumentManagementService(new JsonStorage(tempDir.resolve("medialab")));

        User admin = service.login("medialab", "medialab_2025").orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole());
        assertFalse(service.getCategories().isEmpty());
    }

    @Test
    void authorCanCreateAndVersionDocument() throws Exception {
        DocumentManagementService service = new DocumentManagementService(new JsonStorage(tempDir.resolve("medialab")));
        User admin = service.login("medialab", "medialab_2025").orElseThrow();
        String categoryId = service.getCategories().get(0).getId();

        User author = service.addUser(admin, "A", "B", Role.AUTHOR, Set.of(categoryId), "author1", "pw");

        Document doc = service.createDocument(author, "Doc", categoryId, "first");
        assertEquals(1, doc.latestVersion().getVersion());

        service.updateDocument(author, doc.getId(), "second");
        assertEquals(2, doc.latestVersion().getVersion());
    }

    @Test
    void simpleUserSeesOnlyLatestVersion() throws Exception {
        DocumentManagementService service = new DocumentManagementService(new JsonStorage(tempDir.resolve("medialab")));
        User admin = service.login("medialab", "medialab_2025").orElseThrow();
        String categoryId = service.getCategories().get(0).getId();

        User author = service.addUser(admin, "Auth", "U", Role.AUTHOR, Set.of(categoryId), "author2", "pw");
        User simple = service.addUser(admin, "Simple", "U", Role.SIMPLE_USER, Set.of(categoryId), "simple1", "pw");

        Document doc = service.createDocument(author, "Doc2", categoryId, "v1");
        service.updateDocument(author, doc.getId(), "v2");

        assertEquals(1, service.getVisibleVersions(simple, doc.getId()).size());
        assertEquals(2, service.getVisibleVersions(author, doc.getId()).get(0).getVersion());
    }
}
