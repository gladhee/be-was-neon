package handler;

import db.Database;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.annotation.RequestMapping;
import webserver.http.common.HttpSession;
import webserver.http.request.HttpRequest;
import webserver.model.Model;
import webserver.resolver.ResolveResponse;
import webserver.util.QueryStringParser;

import java.util.Map;

public class Handler {

    private static final Logger logger = LoggerFactory.getLogger(Handler.class);

    public Handler() {
    }

    @RequestMapping(method = "GET", path = "/")
    public String getIndex(HttpSession session, Model model) {
        logger.debug("getIndex");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.debug("User not logged in");
            return "index";
        }
        model.addAttribute("user", user);

        return "index";
    }

    @RequestMapping(method = "POST", path = "/login")
    public ResolveResponse<String> login(HttpRequest request, HttpSession session) {
        logger.debug("getLogin");
        String body = request.getBody();
        Map<String, String> queryString = QueryStringParser.parse(body);
        String userId = queryString.get("userId");
        String password = queryString.get("password");
        User user = Database.findUserById(userId);
        if (user == null) {
            logger.debug("User not found: {}", userId);
            return ResolveResponse.redirect("/login/login_failed.html");
        }
        if (!user.getPassword().equals(password)) {
            logger.debug("Invalid password for user: {}", userId);
            return ResolveResponse.redirect("/login/login_failed.html");
        }
        session.setAttribute("user", user);
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
