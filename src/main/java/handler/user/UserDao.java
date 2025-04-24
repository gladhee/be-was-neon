package handler.user;

import db.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.exception.HttpException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static webserver.http.response.HttpStatusCode.INTERNAL_SERVER_ERROR;

public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private static final UserDao INSTANCE = new UserDao();

    private UserDao() {
    }

    public static UserDao getInstance() {
        return INSTANCE;
    }

    public Optional<User> findByUserId(String userId) {
        String sql = "SELECT user_id,name,password,email FROM users WHERE user_id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("password"),
                            rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("findByUserId() 실패: userId={}, SQL=\"{}\"", userId, sql, e);
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }

        return Optional.empty();
    }

    public List<User> findAll() {
        String sql = "SELECT user_id,name,password,email FROM users";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("email")
                ));
            }
            return users;
        } catch (SQLException e) {
            logger.debug("findAll() 실패: SQL=\"{}\"", sql, e);
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }
    }

    public void save(User user) {
        String sql = "INSERT INTO users(user_id,name,password,email) VALUES(?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.debug("save() 실패: user={}, SQL=\"{}\"", user, sql, e);
            throw new HttpException(INTERNAL_SERVER_ERROR);
        }
    }

}
