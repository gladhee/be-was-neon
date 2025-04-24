package handler.Article;

import handler.user.User;
import handler.user.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.annotation.RequestMapping;
import webserver.http.common.HttpSession;

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

}
