package media.service;

import media.model.Article;
import media.model.Status;
import media.dao.ArticleRepository;
import media.dao.UserRepository;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsService {
    private final ArticleRepository articleRepository = new ArticleRepository();
    private final UserRepository    userRepository    = new UserRepository();

    public Map<String, Long> getStatistics() throws SQLException {
        List<Article> all = articleRepository.findAll();

        Map<String, Long> stats = new LinkedHashMap<>();

        stats.put("Всего пользователей", userRepository.countAll());
        stats.put("Всего статей",        articleRepository.countAll());

        stats.put("Черновиков (DRAFT)",
                all.stream().filter(a -> a.getStatus() == Status.DRAFT).count());
        stats.put("Опубликовано (PUBLISHED)",
                all.stream().filter(a -> a.getStatus() == Status.PUBLISHED).count());
        stats.put("В архиве (ARCHIVED)",
                all.stream().filter(a -> a.getStatus() == Status.ARCHIVED).count());

        stats.put("Уникальных категорий",
                all.stream().map(Article::getCategory).distinct().count());
        stats.put("Уникальных авторов",
                all.stream().map(Article::getAuthorId).distinct().count());

        stats.put("Средняя длина названия",
                (long) all.stream().mapToInt(a -> a.getTitle().length()).average().orElse(0));

        return stats;
    }
}