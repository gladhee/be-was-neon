package handler.Article;

import model.User;
import webserver.annotation.RequestMapping;
import webserver.http.common.HttpSession;

public class ArticleHandler {

    @RequestMapping(method = "GET", path = "/articles")
    public String showWritePage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "index";
        }

        return "article/index";
    }

}
