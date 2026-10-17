package media;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

        HBox toolbar = new HBox(10); // 10 — расстояние между кнопками
        toolbar.setPadding(new Insets(10));
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);

        // 3. Собираем окно
        BorderPane root = new BorderPane();
        root.setTop(toolbar); // панель — сверху
        root.setCenter(table); // таблица — по центру

        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("InternetMedia — Статьи");
        stage.setScene(scene);
        stage.show();

        // 4. Загружаем данные
        loadData();
    }

    /** Загружает статьи из БД и заполняет таблицу. */
    private void loadData() {
        try {
            List<Article> articles = service.getAll();
            ObservableList<Article> data = FXCollections.observableArrayList(articles);
            table.setItems(data);
        } catch (Exception e) {
            showError("Ошибка загрузки данных: " + e.getMessage());
        }
    }

    /** Показывает окно с ошибкой. */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Удаляет выбранную статью. */
    private void deleteArticle() {
        // 1. Получить выбранную статью
        Article selected = table.getSelectionModel().getSelectedItem();

        // 2. Проверка: выбрана ли строка
        if (selected == null) {
            showWarning("Выберите статью для удаления");
            return;
        }

        // 3. Подтверждение
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить статью \"" + selected.getTitle() + "\"?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return; // пользователь нажал Отмена
        }

        // 4. Удаление
        try {
            service.delete(selected.getId());
            loadData();
            showInfo("Статья удалена");
        } catch (Exception e) {
            showError("Ошибка удаления: " + e.getMessage());
        }
    }

    /** Показывает предупреждение. */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Показывает сообщение об успехе. */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Готово");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Открывает форму добавления статьи. */
    private void openAddForm() {
        // 1. Создаём диалог
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Добавить статью");
        dialog.setHeaderText("Заполните поля");

        // 2. Кнопки
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 3. Поля
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

        // 4. Раскладка — сетка
        GridPane grid = new GridPane();
        grid.setHgap(10); // отступ по горизонтали
        grid.setVgap(10); // отступ по вертикали
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

        // 5. Показываем диалог и ждём нажатия кнопки
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    // 6. Забираем данные из полей
                    String title = titleField.getText().trim();
                    String content = contentArea.getText().trim();
                    String category = categoryField.getText().trim();
                    Status status = statusCombo.getValue();
                    User author = authorCombo.getValue();

                    // 7. Простая проверка
                    if (title.isEmpty() || content.isEmpty() || category.isEmpty()) {
                        showWarning("Заполните все поля");
                        return;
                    }
                    if (author == null) {
                        showWarning("Выберите автора");
                        return;
                    }

                    // 8. Сохраняем через Service
                    service.createArticle(title, content, category, status, author.getId());

                    // 9. Обновляем таблицу
                    loadData();
                    showInfo("Статья добавлена");

                } catch (Exception e) {
                    showError("Ошибка добавления: " + e.getMessage());
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}