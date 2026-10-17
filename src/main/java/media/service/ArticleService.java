package media.service;

import media.exception.BusinessException;
import media.exception.EntityNotFoundException;
import media.model.Article;
import media.model.Status;
import media.dao.ArticleRepository;
import media.dao.UserRepository;

import java.sql.SQLException;
import java.util.List;

public class ArticleService {
    private final ArticleRepository articleRepository = new ArticleRepository();
    private final UserRepository userRepository = new UserRepository();

    public void createArticle(String title, String content, String category,
                              Status status, int authorId) throws SQLException {
        if (title == null || title.isBlank())
            throw new BusinessException("Название не может быть пустым");
        if (content == null || content.isBlank())
            throw new BusinessException("Текст не может быть пустым");
        if (category == null || category.isBlank())
            throw new BusinessException("Категория не может быть пустой");
        if (status == null)
            throw new BusinessException("Статус не может быть пустым");
        if (userRepository.findById(authorId) == null)
            throw new EntityNotFoundException("Автор с ID " + authorId + " не найден");

        Article a = new Article(0, title, content, category, status, null, authorId, null);
        articleRepository.create(a);
    }

    public List<Article> getAll() throws SQLException {
        return articleRepository.findAll();
    }

    public Article getById(int id) throws SQLException {
        Article a = articleRepository.findById(id);
        if (a == null)
            throw new EntityNotFoundException("Статья с ID " + id + " не найдена");
        return a;
    }

    public void update(Article a) throws SQLException {
        if (articleRepository.findById(a.getId()) == null)
            throw new EntityNotFoundException("Статья с ID " + a.getId() + " не найдена");
        articleRepository.update(a);
    }

    public void delete(int id) throws SQLException {
        if (articleRepository.findById(id) == null)
            throw new EntityNotFoundException("Статья с ID " + id + " не найдена");
        articleRepository.delete(id);
    }

    public List<Article> searchByTitle(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank())
            throw new BusinessException("Поисковый запрос не может быть пустым");
        return articleRepository.searchByTitle(keyword);
    }

    public List<Article> searchByCategory(String category) throws SQLException {
        if (category == null || category.isBlank())
            throw new BusinessException("Категория не может быть пустой");
        return articleRepository.searchByCategory(category);
    }

    public List<Article> filterByStatus(Status status) throws SQLException {
        if (status == null)
            throw new BusinessException("Статус не может быть пустым");
        return articleRepository.filterByStatus(status);
    }

    public List<Article> filterByAuthor(int authorId) throws SQLException {
        if (userRepository.findById(authorId) == null)
            throw new EntityNotFoundException("Автор с ID " + authorId + " не найден");
        return articleRepository.filterByAuthor(authorId);
    }

    public List<Article> filterCombined(String category, Status status) throws SQLException {
        return articleRepository.filterCombined(category, status);
    }

    public List<Article> sort(String column, boolean asc) throws SQLException {
        return articleRepository.sort(column, asc);
    }
}