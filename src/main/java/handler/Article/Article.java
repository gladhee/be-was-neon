package handler.Article;

public class Article {

    private long id;
    private final String content;
    private final String userName;

    public Article(long id, String content, String userName) {
        this.id = id;
        this.content = content;
        this.userName = userName;
    }

    public Article(String content, String userName) {
        this(0, content, userName);
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

    public String getUserName() {
        return userName;
    }

}
