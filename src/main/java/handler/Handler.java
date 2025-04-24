package handler;

import handler.Article.Article;
import handler.Article.ArticleDao;
import handler.user.UserDao;
import handler.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.annotation.RequestMapping;
import webserver.http.common.HttpSession;
import webserver.http.exception.HttpException;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpStatusCode;
import webserver.model.Model;
import webserver.resolver.ResolveResponse;
import webserver.util.QueryStringParser;

import java.util.Map;
import java.util.Optional;

import static webserver.http.response.HttpStatusCode.NOT_FOUND;

public class Handler {

    private static final Logger logger = LoggerFactory.getLogger(Handler.class);
    private final UserDao userDao;
    private final ArticleDao articleDao;

    public Handler(UserDao userDao, ArticleDao articleDao) {
        this.userDao = userDao;
        this.articleDao = articleDao;
    }

    @RequestMapping(method = "GET", path = "/")
    public String showPost(HttpRequest request, HttpSession session, Model model) {
        logger.debug("showPost");

        // 쿼리스트링에서 id 파싱 (없으면 0)
        Map<String, String> queryString = request.getRequestLine().getQueryString();
        long id = queryString.containsKey("id")
                ? Long.parseLong(queryString.get("id"))
                : articleDao.findMaxId();
        logger.debug("id: {}", id);

        Optional<Article> current = articleDao.findById(id);
        if (current.isEmpty()) {
            throw new HttpException(NOT_FOUND);
        }

        // 이전/다음 id 계산
        long nextId = articleDao.existsById(id - 1) ? id - 1 : id;
        long prevId = articleDao.existsById(id + 1) ? id + 1 : id;

        model.addAttribute("post", current.get());
        model.addAttribute("prevLink", "/?id=" + prevId);
        model.addAttribute("nextLink", "/?id=" + nextId);

        return "index";
    }

    @RequestMapping(method = "POST", path = "/login")
    public ResolveResponse<String> login(HttpRequest request, HttpSession session) {
        logger.debug("getLogin");
        String body = request.getBody();
        Map<String, String> queryString = QueryStringParser.parse(body);
        String userId = queryString.get("userId");
        String password = queryString.get("password");

        Optional<User> user = userDao.findByUserId(userId);
        if (user.isEmpty()) {
            logger.debug("User not found: {}", userId);
            return ResolveResponse.redirect("/login/login_failed.html");
        }
        if (!user.get().isCorrectPassword(password)) {
            logger.debug("Invalid password for user: {}", userId);
            return ResolveResponse.redirect("/login/login_failed.html");
        }
        session.setAttribute("user", user.get());
        logger.debug("User logged in: {}", user);
        return ResolveResponse.redirect("/");
    }

    @RequestMapping(method = "POST", path = "/logout")
    public ResolveResponse<String> logout(HttpSession session) {
        logger.debug("getLogout");
        session.invalidate();
        logger.debug("User logged out");

        return ResolveResponse.redirect("/");
    }

}
