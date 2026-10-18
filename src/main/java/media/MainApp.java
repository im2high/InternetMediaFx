package media;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import media.model.Article;
import media.service.ArticleService;
import javafx.scene.layout.GridPane;
import media.model.Status;
import media.model.User;
import media.service.UserService;

import java.util.List;

public class MainApp extends Application {

    private TableView<Article> table = new TableView<>();
    private ArticleService service = new ArticleService();
    private UserService userService = new UserService();

    @Override
    public void start(Stage stage) {
        TableColumn<Article, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Article, String> titleCol = new TableColumn<>("Название");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Article, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Article, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Article, String> authorCol = new TableColumn<>("Автор");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("authorName"));

        table.getColumns().addAll(idCol, titleCol, categoryCol, statusCol, authorCol);

        Button addBtn = new Button("Добавить");
        Button editBtn = new Button("Редактировать");
        Button deleteBtn = new Button("Удалить");
        Button refreshBtn = new Button("Обновить");

        addBtn.setOnAction(e -> openAddForm());
        editBtn.setOnAction(e -> System.out.println("Нажато: Редактировать"));
        deleteBtn.setOnAction(e -> deleteArticle());
        refreshBtn.setOnAction(e -> {
            System.out.println("Нажато: Обновить");
            loadData();
        });

        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(table);

        Scene scene = new Scene(root, 1000, 1000);

        stage.setTitle("InternetMedia — Статьи");
        stage.setScene(scene);
        stage.show();

        loadData();
    }

    private void loadData() {
        try {
            List<Article> articles = service.getAll();
            ObservableList<Article> data = FXCollections.observableArrayList(articles);
            table.setItems(data);
        } catch (Exception e) {
            showError("Ошибка загрузки данных: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteArticle() {
        Article selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Выберите статью для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить статью \"" + selected.getTitle() + "\"?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            service.delete(selected.getId());
            loadData();
            showInfo("Статья удалена");
        } catch (Exception e) {
            showError("Ошибка удаления: " + e.getMessage());
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Готово");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openAddForm() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Добавить статью");
        dialog.setHeaderText("Заполните поля");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Название статьи");

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Текст статьи");
        contentArea.setPrefRowCount(6);

        TextField categoryField = new TextField();
        categoryField.setPromptText("Например: Технологии");

        ComboBox<Status> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Status.values());
        statusCombo.setValue(Status.DRAFT);

        ComboBox<User> authorCombo = new ComboBox<>();
        authorCombo.setPromptText("Выберите автора");
        try {
            authorCombo.getItems().addAll(userService.getAllUsers());
        } catch (Exception e) {
            showError("Ошибка загрузки авторов: " + e.getMessage());
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("Название:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Текст:"), 0, 1);
        grid.add(contentArea, 1, 1);
        grid.add(new Label("Категория:"), 0, 2);
        grid.add(categoryField, 1, 2);
        grid.add(new Label("Статус:"), 0, 3);
        grid.add(statusCombo, 1, 3);
        grid.add(new Label("Автор:"), 0, 4);
        grid.add(authorCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String category = categoryField.getText().trim();
            User author = authorCombo.getValue();

            if (title.isEmpty() || content.isEmpty() || category.isEmpty()) {
                showWarning("Заполните все поля");
                event.consume();
                return;
            }
            if (author == null) {
                showWarning("Выберите автора");
                event.consume();
                return;
            }

            try {
                service.createArticle(title, content, category,
                        statusCombo.getValue(), author.getId());
                loadData();
                showInfo("Статья добавлена");
            } catch (Exception e) {
                showError("Ошибка добавления: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}