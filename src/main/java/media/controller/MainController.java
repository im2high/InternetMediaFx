package media.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import media.model.Article;
import media.model.Status;
import media.model.User;
import media.service.ArticleService;
import media.service.StatisticsService;
import media.service.UserService;
import media.util.ExcelExporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainController {

    // ===== Поля из FXML =====
    @FXML private TableView<Article> table;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button refreshBtn;
    @FXML private Button statsBtn;
    @FXML private Button exportBtn;
    @FXML private Button resetBtn;

    // ===== Сервисы =====
    private final ArticleService service = new ArticleService();
    private final UserService userService = new UserService();
    private final StatisticsService statisticsService = new StatisticsService();
    private final List<Article> allArticles = new ArrayList<>();

    // ===== Инициализация =====
    @FXML
    public void initialize() {
        // Колонки таблицы
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
        table.getStyleClass().add("main-table");

        // Обработчики кнопок
        addBtn.setOnAction(e -> {
            if (openArticleForm(null)) loadData();
        });

        editBtn.setOnAction(e -> {
            Article selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showWarning("Выберите статью для редактирования");
                return;
            }
            if (openArticleForm(selected)) loadData();
        });

        deleteBtn.setOnAction(e -> deleteArticle());
        refreshBtn.setOnAction(e -> loadData());
        statsBtn.setOnAction(e -> showStatistics());
        exportBtn.setOnAction(e -> exportToExcel());

        // Фильтр по статусу
        statusFilterCombo.getItems().addAll(
                "Все статусы", "DRAFT", "PUBLISHED", "ARCHIVED");
        statusFilterCombo.setValue("Все статусы");

        // Слушатели фильтров
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        statusFilterCombo.setOnAction(e -> applyFilters());
        categoryFilterCombo.setOnAction(e -> applyFilters());

        // Кнопка сброса
        resetBtn.setOnAction(e -> {
            searchField.clear();
            statusFilterCombo.setValue("Все статусы");
            categoryFilterCombo.setValue("Все категории");
            applyFilters();
        });

        // Загрузка данных
        loadData();
    }

    // ===== Загрузка =====
    private void loadData() {
        try {
            allArticles.clear();
            allArticles.addAll(service.getAll());
            refreshCategoryFilter();
            applyFilters();
        } catch (Exception e) {
            showError("Ошибка загрузки данных: " + e.getMessage());
        }
    }

    private void refreshCategoryFilter() {
        String current = categoryFilterCombo.getValue();

        List<String> categories = allArticles.stream()
                .map(Article::getCategory)
                .distinct()
                .sorted()
                .toList();

        List<String> options = new ArrayList<>();
        options.add("Все категории");
        options.addAll(categories);

        categoryFilterCombo.setItems(FXCollections.observableArrayList(options));

        if (current != null && options.contains(current)) {
            categoryFilterCombo.setValue(current);
        } else {
            categoryFilterCombo.setValue("Все категории");
        }
    }

    private void applyFilters() {
        String search = searchField.getText() == null
                ? "" : searchField.getText().toLowerCase().trim();
        String status = statusFilterCombo.getValue();
        String category = categoryFilterCombo.getValue();

        List<Article> filtered = allArticles.stream()
                .filter(a -> search.isEmpty()
                        || a.getTitle().toLowerCase().contains(search)
                        || a.getContent().toLowerCase().contains(search)
                        || a.getCategory().toLowerCase().contains(search))
                .filter(a -> status == null || status.equals("Все статусы")
                        || a.getStatus().name().equals(status))
                .filter(a -> category == null || category.equals("Все категории")
                        || a.getCategory().equals(category))
                .toList();

        table.setItems(FXCollections.observableArrayList(filtered));
    }

    // ===== CRUD =====
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
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            service.delete(selected.getId());
            loadData();
            showInfo("Статья удалена");
        } catch (Exception e) {
            showError("Ошибка удаления: " + e.getMessage());
        }
    }

    private boolean openArticleForm(Article existing) {
        boolean isEdit = existing != null;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Редактировать статью" : "Добавить статью");
        dialog.setHeaderText(isEdit ? "Измените поля" : "Заполните поля");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField();
        TextArea contentArea = new TextArea();
        contentArea.setPrefRowCount(6);
        TextField categoryField = new TextField();

        ComboBox<Status> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Status.values());
        statusCombo.setValue(Status.DRAFT);

        ComboBox<User> authorCombo = new ComboBox<>();
        authorCombo.setPromptText("Выберите автора");
        try {
            authorCombo.getItems().addAll(userService.getAllUsers());
        } catch (Exception e) {
            showError("Ошибка загрузки авторов: " + e.getMessage());
            return false;
        }

        if (isEdit) {
            titleField.setText(existing.getTitle());
            contentArea.setText(existing.getContent());
            categoryField.setText(existing.getCategory());
            statusCombo.setValue(existing.getStatus());
            for (User u : authorCombo.getItems()) {
                if (u.getId() == existing.getAuthorId()) {
                    authorCombo.setValue(u);
                    break;
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
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

        final boolean[] saved = {false};
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
                if (isEdit) {
                    existing.setTitle(title);
                    existing.setContent(content);
                    existing.setCategory(category);
                    existing.setStatus(statusCombo.getValue());
                    existing.setAuthorId(author.getId());
                    service.update(existing);
                    showInfo("Статья обновлена");
                } else {
                    service.createArticle(title, content, category,
                            statusCombo.getValue(), author.getId());
                    showInfo("Статья добавлена");
                }
                saved[0] = true;
            } catch (Exception e) {
                showError("Ошибка сохранения: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
        return saved[0];
    }

    // ===== Статистика и экспорт =====
    private void showStatistics() {
        try {
            Map<String, Long> stats = statisticsService.getStatistics();
            StringBuilder text = new StringBuilder();
            for (String key : stats.keySet()) {
                text.append(key).append(": ").append(stats.get(key)).append("\n");
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Статистика");
            alert.setHeaderText("Показатели системы");
            alert.setContentText(text.toString());
            alert.showAndWait();
        } catch (Exception e) {
            showError("Ошибка загрузки статистики: " + e.getMessage());
        }
    }

    private void exportToExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить в Excel");
        chooser.setInitialFileName("articles.xlsx");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = chooser.showSaveDialog(null);
        if (file == null) return;

        try {
            List<Article> articles = table.getItems();
            ExcelExporter.exportArticles(articles, file.getAbsolutePath());
            showInfo("Экспортировано статей: " + articles.size());
        } catch (Exception e) {
            showError("Ошибка экспорта: " + e.getMessage());
        }
    }

    // ===== Сообщения =====
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
