package webserver.view;

import handler.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.http.common.HttpSession;
import webserver.model.Model;

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

        // 테스트용 가짜 아이템 HTML
        String sb = """
                <li class="user-item"><span class="user-name">글래드</span></li>
                """ + """
                <li class="user-item"><span class="user-name">호눅스</span></li>
                """;
        model.addAttribute("itemsHtml", sb);

        ModelAndView mav = new ModelAndView(viewName, model);

        // when
        TemplateEngine engine = new TemplateEngine(mav, session);
        String html = engine.render();

        // then
        assertThat(html)
                .contains("글래드")
                .contains("호눅스")
                .contains("<html");
    }

}
