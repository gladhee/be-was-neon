package webserver.view;

import handler.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http.common.HttpSession;
import webserver.model.Model;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngineTest {

    @Test
    @DisplayName("로그인 된 유저는 index.html(메인페이지)에 자신의 이름이 포함되어 빌딩되어야 한다.")
    void 로그인_된_유저_메인_페이지_자신의_이름_포함_테스트() {
        // given
        String viewName = "index";
        User user = new User("javajigi", "test", "자바지기", "javajigi@inop.com");
        ModelAndView mav = new ModelAndView(viewName);
        HttpSession session = new HttpSession("123456789");
        session.setAttribute("user", user);

        // when
        TemplateEngine te = new TemplateEngine(mav, session);
        String result = te.render();

        // then
        assertThat(result).contains("자바지기");
        assertThat(result).contains("<html>");
    }

    @Test
    @DisplayName("로그인 안 된 유저는 index.html(메인페이지)에 자신의 이름이 포함되어 빌딩되어서는 안된다.")
    void 로그인_안_된_유저_메인_페이지_빌딩_테스트() {
        // given
        String viewName = "index";
        ModelAndView mav = new ModelAndView(viewName);

        // when
        TemplateEngine te = new TemplateEngine(mav, null);
        String result = te.render();

        // then
        assertThat(result).doesNotContain("자바지기");
        assertThat(result).contains("<html>");
    }

    @Test
    @DisplayName("로그인이 된 유저 목록들을 모두 빌딩해야 한다(user-list.html).")
    void 유저_목록_빌딩_테스트() {
        // given
        String viewName = "user-list";
        Model model = new Model();
        HttpSession session = new HttpSession("123456789");
        User user = new User("javajigi", "test", "자바지기", "javajigi@inop.com");
        session.setAttribute("user", user);

        User user2 = new User("glad", "test", "글래드", "glad@codesquad.com");
        User user3 = new User("honux", "test", "호눅스", "honux@codesquad.com");
        Database.addUser(user);
        Database.addUser(user2);
        Database.addUser(user3);

        Collection<User> users = Database.findAll();
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

        // when
        ModelAndView mav = new ModelAndView(viewName, model);
        TemplateEngine te = new TemplateEngine(mav, session);
        String result = te.render();

        // then
        assertThat(result).contains("자바지기");
        assertThat(result).contains("글래드");
        assertThat(result).contains("호눅스");
        assertThat(result).contains("<html");
    }

}
