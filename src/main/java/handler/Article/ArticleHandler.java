package handler.Article;

import handler.user.User;
import handler.user.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.annotation.RequestMapping;
import webserver.http.common.HttpSession;
import webserver.http.request.HttpRequest;
import webserver.resolver.ResolveResponse;
import webserver.util.QueryStringParser;

import java.util.Map;

public class ArticleHandler {

    private static final Logger logger = LoggerFactory.getLogger(ArticleHandler.class);
    private final UserDao userDao;
    private final ArticleDao articleDao;

    public ArticleHandler(UserDao userDao, ArticleDao articleDao) {
        this.userDao = userDao;
        this.articleDao = articleDao;
    }

    @RequestMapping(method = "GET", path = "/articles")
    public String showWritePage(HttpSession session) {
        logger.debug("showWritePage");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.debug("user is null");
            return "index";
        }

        logger.debug("user is {}", user);
        return "article/index";
    }

    @RequestMapping(method = "POST", path = "/articles")
    public ResolveResponse<String> create(HttpRequest request, HttpSession session) {
        logger.debug("create");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.debug("user is null");
            return ResolveResponse.redirect("login");
        }

        String body = request.getBody();
        Map<String, String> queryString = QueryStringParser.parse(body);
        String content = queryString.get("content");
        Article article = new Article(content, user.getName());
        articleDao.save(article);
        return ResolveResponse.redirect("/");
    }

}
