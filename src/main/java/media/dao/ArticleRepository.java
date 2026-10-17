package media.dao;

import media.model.Article;
import media.model.Status;

import java.sql.*;
import java.util.List;

public class ArticleRepository extends BaseRepository<Article> {

    private static final String BASE_SELECT =
        "SELECT a.id, a.title, a.content, a.category, a.status, a.created_at, " +
        "       a.author_id, u.username AS author_name " +
        "FROM articles a " +
        "LEFT JOIN users u ON a.author_id = u.id ";


    @Override
    public void create(Article a) throws SQLException {
        String sql = "INSERT INTO articles(title, content, category, status, author_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getContent());
            ps.setString(3, a.getCategory());
            ps.setString(4, a.getStatus().name());
            ps.setInt(5, a.getAuthorId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public Article findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.id = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Article> findAll() throws SQLException {
        return queryList(BASE_SELECT + "ORDER BY a.id", null);
    }

    @Override
    public void update(Article a) throws SQLException {
        String sql = "UPDATE articles SET title = ?, content = ?, category = ?, status = ? " +
                     "WHERE id = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getContent());
            ps.setString(3, a.getCategory());
            ps.setString(4, a.getStatus().name());
            ps.setInt(5, a.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM articles WHERE id = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Article> searchByTitle(String keyword) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.title ILIKE ? ORDER BY a.id";
        return queryList(sql, ps -> ps.setString(1, "%" + keyword + "%"));
    }

    public List<Article> searchByCategory(String category) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.category = ? ORDER BY a.id";
        return queryList(sql, ps -> ps.setString(1, category));
    }

    public List<Article> filterByStatus(Status status) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.status = ? ORDER BY a.id";
        return queryList(sql, ps -> ps.setString(1, status.name()));
    }

    public List<Article> filterByAuthor(int authorId) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.author_id = ? ORDER BY a.id";
        return queryList(sql, ps -> ps.setInt(1, authorId));
    }

    public List<Article> filterCombined(String category, Status status) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.category = ? AND a.status = ? ORDER BY a.id";
        return queryList(sql, ps -> {
            ps.setString(1, category);
            ps.setString(2, status.name());
        });
    }

    public List<Article> sort(String column, boolean asc) throws SQLException {
        List<String> allowed = List.of("title", "category", "status", "created_at", "id");
        if (!allowed.contains(column)) column = "id";
        String sql = BASE_SELECT + "ORDER BY a." + column + (asc ? " ASC" : " DESC");
        return queryList(sql, null);
    }

    public long countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM articles";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    @Override
    protected Article mapRow(ResultSet rs) throws SQLException {
        Article a = new Article();
        a.setId(rs.getInt("id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setCategory(rs.getString("category"));
        a.setStatus(Status.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
        a.setAuthorId(rs.getInt("author_id"));
        a.setAuthorName(rs.getString("author_name"));
        return a;
    }
}