package handler.Article;

import db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.exception.HttpException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static webserver.http.response.HttpStatusCode.INTERNAL_SERVER_ERROR;

public class ArticleDao {

    private static final Logger logger = LoggerFactory.getLogger(ArticleDao.class);
    private static final ArticleDao INSTANCE = new ArticleDao();

    private ArticleDao() {
    }

    public static ArticleDao getInstance() {
        return INSTANCE;
    }

    public Optional<Article> findById(long id) {
        String sql = "SELECT id, content, user_name FROM articles WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToArticle(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("findById() 실패: id={}, SQL=\"{}\"", id, sql, e);
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }
        return Optional.empty();
    }

    public long findMaxId() {
        String sql = "SELECT MAX(id) FROM articles";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("findMaxId() 실패: SQL=\"{}\"", sql, e);
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }
        return 0;
    }

    public boolean existsById(long id) {
        String sql = "SELECT COUNT(*) FROM articles WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("existsById() 실패: id={}, SQL=\"{}\"", id, sql, e);
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }
        return false;
    }

    public List<Article> findAllByUserName(String userName) {
        String sql = "SELECT id, content, user_name FROM articles WHERE user_name = ?";
        List<Article> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToArticle(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("findAllByUserId() 실패: userName={}, SQL=\"{}\"", userName, sql, e);
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }
        return list;
    }

    public void save(Article article) {
        String sql = "INSERT INTO articles(content, user_name) VALUES(?,?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, article.getContent());
            ps.setString(2, article.getUserName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    article.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            logger.error("save() 실패: article={}, SQL=\"{}\"", article, sql, e);
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }
    }

    // ResultSet → Article 변환
    private Article mapRowToArticle(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String content = rs.getString("content");
        String userName = rs.getString("user_name");
        return new Article(id, content, userName);
    }

}
