package media.model;

import java.time.LocalDateTime;

public class Article {
    private int id;
    private String title;
    private String content;
    private String category;
    private Status status;
    private LocalDateTime createdAt;
    private int authorId;
    private String authorName;

    public Article() {}

    public Article(int id, String title, String content, String category,
                   Status status, LocalDateTime createdAt, int authorId, String authorName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.authorId = authorId;
        this.authorName = authorName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    @Override
    public String toString() {
        return String.format(
            "[%d] \"%s\" | category=%s | status=%s | author=%s | created=%s%n    %s",
            id, title, category, status, authorName,
            (createdAt != null ? createdAt.toString() : "-"),
            (content.length() > 60 ? content.substring(0, 60) + "..." : content));
    }
}