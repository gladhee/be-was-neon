package handler.Article;

public class Article {

    private long id;
    private final String content;
    private final String userId;

    public Article(long id, String content, String userId) {
        this.id = id;
        this.content = content;
        this.userId = userId;
    }

    public Article(String content, String userId) {
        this(0, content, userId);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

}
