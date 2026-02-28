package com.example.mfm.ui;

import com.example.mfm.model.*;
import com.example.mfm.persistence.JsonStorage;
import com.example.mfm.service.DocumentManagementService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;

public class MediaLabApp extends Application {
    private DocumentManagementService service;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        this.service = new DocumentManagementService(new JsonStorage(Path.of("medialab")));
        showLoginScene();
    }

    private void showLoginScene() {
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Label message = new Label();
        Button loginButton = new Button("Login");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(message, 1, 3);

        loginButton.setOnAction(e -> service.login(usernameField.getText(), passwordField.getText())
                .ifPresentOrElse(this::showMainScene, () -> message.setText("Invalid credentials")));

        stage.setTitle("MediaLab Login");
        stage.setScene(new Scene(grid, 420, 220));
        stage.show();
    }

    private void showMainScene(User user) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox statsBox = new VBox(8);
        statsBox.setPadding(new Insets(10));
        Label categoriesLabel = new Label();
        Label docsLabel = new Label();
        Label watchedLabel = new Label();
        statsBox.getChildren().addAll(new Label("Στατιστικά"), categoriesLabel, docsLabel, watchedLabel);
        refreshStats(user, categoriesLabel, docsLabel, watchedLabel);
        root.setLeft(statsBox);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(documentsTab(user, categoriesLabel, docsLabel, watchedLabel));
        tabs.getTabs().add(watchTab(user, categoriesLabel, docsLabel, watchedLabel));
        tabs.getTabs().add(searchTab(user));
        if (user.getRole() == Role.ADMIN) {
            tabs.getTabs().add(categoriesTab(user, categoriesLabel, docsLabel, watchedLabel));
            tabs.getTabs().add(usersTab(user));
        }
        root.setCenter(tabs);

        List<String> notifications = service.collectWatchNotifications(user);
        if (!notifications.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, String.join("\n", notifications), ButtonType.OK);
            alert.setHeaderText("Ενημέρωση παρακολουθήσεων");
            alert.showAndWait();
        }

        stage.setTitle("MediaLab Documents - " + user.displayName());
        stage.setScene(new Scene(root, 1100, 700));
        stage.setOnCloseRequest(e -> {
            try { service.shutdown(); } catch (Exception ignored) { }
        });
    }

    private Tab categoriesTab(User admin, Label categoriesLabel, Label docsLabel, Label watchedLabel) {
        ListView<Category> list = new ListView<>();
        refreshCategories(list);
        TextField nameField = new TextField();
        nameField.setPromptText("Νέο όνομα κατηγορίας");

        Button add = new Button("Προσθήκη");
        add.setOnAction(e -> {
            service.addCategory(admin, nameField.getText());
            refreshCategories(list);
            refreshStats(admin, categoriesLabel, docsLabel, watchedLabel);
        });

        Button rename = new Button("Μετονομασία");
        rename.setOnAction(e -> {
            Category c = list.getSelectionModel().getSelectedItem();
            if (c != null) {
                service.renameCategory(admin, c.getId(), nameField.getText());
                refreshCategories(list);
            }
        });

        Button delete = new Button("Διαγραφή");
        delete.setOnAction(e -> {
            Category c = list.getSelectionModel().getSelectedItem();
            if (c != null) {
                service.deleteCategory(admin, c.getId());
                refreshCategories(list);
                refreshStats(admin, categoriesLabel, docsLabel, watchedLabel);
            }
        });

        VBox content = new VBox(8, list, nameField, new HBox(8, add, rename, delete));
        content.setPadding(new Insets(10));
        return new Tab("Κατηγορίες", content);
    }

    private Tab usersTab(User admin) {
        ListView<String> list = new ListView<>();
        TextField fn = new TextField(); fn.setPromptText("Όνομα");
        TextField ln = new TextField(); ln.setPromptText("Επώνυμο");
        TextField un = new TextField(); un.setPromptText("username");
        TextField pw = new TextField(); pw.setPromptText("password");
        ComboBox<Role> role = new ComboBox<>(); role.getItems().addAll(Role.values()); role.getSelectionModel().select(Role.SIMPLE_USER);
        refreshUsers(admin, list);

        Button add = new Button("Προσθήκη χρήστη");
        add.setOnAction(e -> {
            String firstCategory = service.getCategories().isEmpty() ? null : service.getCategories().get(0).getId();
            service.addUser(admin, fn.getText(), ln.getText(), role.getValue(), firstCategory == null ? java.util.Set.of() : java.util.Set.of(firstCategory), un.getText(), pw.getText());
            refreshUsers(admin, list);
        });

        VBox content = new VBox(8, list, fn, ln, un, pw, role, add);
        content.setPadding(new Insets(10));
        return new Tab("Χρήστες", content);
    }

    private Tab searchTab(User user) {
        TextField title = new TextField();
        TextField author = new TextField();
        ComboBox<Category> category = new ComboBox<>();
        category.getItems().addAll(service.getCategories());
        ListView<String> results = new ListView<>();
        Button search = new Button("Αναζήτηση");
        search.setOnAction(e -> {
            Category selected = category.getValue();
            List<Document> docs = service.searchDocuments(user, selected == null ? null : selected.getId(), title.getText(), author.getText());
            results.getItems().setAll(docs.stream().map(d -> d.getTitle() + " | " + d.getAuthorName() + " | v" + d.latestVersion().getVersion()).toList());
        });
        VBox content = new VBox(8, new Label("Τίτλος"), title, new Label("Συγγραφέας"), author, new Label("Κατηγορία"), category, search, results);
        content.setPadding(new Insets(10));
        return new Tab("Αναζήτηση", content);
    }

    private Tab watchTab(User user, Label categoriesLabel, Label docsLabel, Label watchedLabel) {
        ListView<Document> available = new ListView<>();
        ListView<Document> watched = new ListView<>();
        refreshDocumentsForUser(user, available);
        watched.getItems().setAll(service.getWatchedDocuments(user));

        Button watch = new Button("Παρακολούθηση ->");
        watch.setOnAction(e -> {
            Document d = available.getSelectionModel().getSelectedItem();
            if (d != null) {
                service.watchDocument(user, d.getId());
                watched.getItems().setAll(service.getWatchedDocuments(user));
                refreshStats(user, categoriesLabel, docsLabel, watchedLabel);
            }
        });

        Button unwatch = new Button("<- Διακοπή");
        unwatch.setOnAction(e -> {
            Document d = watched.getSelectionModel().getSelectedItem();
            if (d != null) {
                service.unwatchDocument(user, d.getId());
                watched.getItems().setAll(service.getWatchedDocuments(user));
                refreshStats(user, categoriesLabel, docsLabel, watchedLabel);
            }
        });

        HBox content = new HBox(8, new VBox(new Label("Διαθέσιμα"), available), new VBox(8, watch, unwatch), new VBox(new Label("Παρακολουθούμενα"), watched));
        content.setPadding(new Insets(10));
        return new Tab("Παρακολούθηση", content);
    }

    private Tab documentsTab(User user, Label categoriesLabel, Label docsLabel, Label watchedLabel) {
        ListView<Document> docs = new ListView<>();
        refreshDocumentsForUser(user, docs);
        TextField title = new TextField(); title.setPromptText("Τίτλος");
        TextArea content = new TextArea(); content.setPromptText("Κείμενο");
        ComboBox<Category> category = new ComboBox<>(); category.getItems().addAll(service.getCategories());
        ListView<String> versions = new ListView<>();

        docs.getSelectionModel().selectedItemProperty().addListener((obs, oldDoc, selected) -> {
            if (selected != null) {
                versions.getItems().setAll(service.getVisibleVersions(user, selected.getId()).stream().map(v -> "v" + v.getVersion() + ": " + v.getContent()).toList());
            }
        });

        Button create = new Button("Δημιουργία");
        create.setDisable(user.getRole() == Role.SIMPLE_USER);
        create.setOnAction(e -> {
            if (category.getValue() != null) {
                service.createDocument(user, title.getText(), category.getValue().getId(), content.getText());
                refreshDocumentsForUser(user, docs);
                refreshStats(user, categoriesLabel, docsLabel, watchedLabel);
            }
        });

        Button update = new Button("Νέα έκδοση");
        update.setDisable(user.getRole() == Role.SIMPLE_USER);
        update.setOnAction(e -> {
            Document selected = docs.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.updateDocument(user, selected.getId(), content.getText());
                refreshDocumentsForUser(user, docs);
            }
        });

        Button delete = new Button("Διαγραφή");
        delete.setDisable(user.getRole() == Role.SIMPLE_USER);
        delete.setOnAction(e -> {
            Document selected = docs.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.deleteDocument(user, selected.getId());
                refreshDocumentsForUser(user, docs);
                refreshStats(user, categoriesLabel, docsLabel, watchedLabel);
                versions.getItems().clear();
            }
        });

        VBox contentBox = new VBox(8, new Label("Έγγραφα"), docs, new Label("Τίτλος"), title, new Label("Κατηγορία"), category, new Label("Κείμενο"), content, new HBox(8, create, update, delete), new Label("Εκδόσεις"), versions);
        contentBox.setPadding(new Insets(10));
        return new Tab("Έγγραφα", contentBox);
    }

    private void refreshStats(User user, Label categoriesLabel, Label docsLabel, Label watchedLabel) {
        var stats = service.getStats(user);
        categoriesLabel.setText("Κατηγορίες: " + stats.get("categories"));
        docsLabel.setText("Έγγραφα: " + stats.get("documents"));
        watchedLabel.setText("Παρακολουθήσεις: " + stats.get("watched"));
    }

    private void refreshCategories(ListView<Category> list) {
        list.getItems().setAll(service.getCategories());
    }

    private void refreshUsers(User admin, ListView<String> list) {
        list.getItems().setAll(service.getUsers(admin).stream().map(u -> u.getUsername() + " (" + u.getRole() + ")").toList());
    }

    private void refreshDocumentsForUser(User user, ListView<Document> list) {
        List<Document> docs = service.searchDocuments(user, null, null, null);
        list.getItems().setAll(docs);
        list.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Document item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " [" + item.getAuthorName() + "]");
            }
        });
    }
}
