package handler.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.annotation.RequestMapping;
import webserver.http.common.HttpSession;
import webserver.http.exception.HttpException;
import webserver.http.request.HttpRequest;
import webserver.model.Model;
import webserver.resolver.ResolveResponse;
import webserver.util.QueryStringParser;

import java.util.List;
import java.util.Map;

import static webserver.http.response.HttpStatusCode.BAD_REQUEST;
import static webserver.http.response.HttpStatusCode.CONFLICT;

public class UserHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
    private final UserDao userDao;

    public UserHandler(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(method = "GET", path = "/users")
    public String listUsers(HttpSession session, Model model) {
        logger.debug("getUsers");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.debug("User not logged in");
            return "login";
        }
        List<User> users = userDao.findAll();

        StringBuilder sb = new StringBuilder();
        for (User u : users) {
            sb.append("""
                    <li class="user-item">
                      <div class="user-avatar"></div>
                      <div class="user-info">
                        <span class="user-name">%s</span>
                        <span class="user-email">%s</span>
                      </div>
                    </li>
                    """.formatted(u.getName(), u.getEmail()));
        }

        model.addAttribute("itemsHtml", sb.toString());
        return "user-list";
    }

    @RequestMapping(method = "POST", path = "/users")
    public ResolveResponse<String> createUser(HttpRequest request) {
        logger.debug("getCreate");
        String body = request.getBody();
        Map<String, String> queryString = QueryStringParser.parse(body);
        String userId = queryString.get("userId");
        String name = queryString.get("name");
        String password = queryString.get("password");
        String email = queryString.get("email");

        validateNotBlank(userId, name, password, email);
        if (userDao.findByUserId(userId).isPresent()) {
            logger.debug("users = {}", userDao.findAll());
            logger.debug("User already exists: {}", userId);
            throw new HttpException(CONFLICT);
        }

        User user = new User(userId, password, name, email);
        userDao.save(user);
        logger.debug("User created: {}", user);
        return ResolveResponse.redirect("/");
    }

    private void validateNotBlank(String... values) {
        for (String value : values) {
            if (value == null || value.isBlank()) {
                logger.debug("userId and name and password and email are null!");
                throw new HttpException(BAD_REQUEST);
            }
        }
    }

}
