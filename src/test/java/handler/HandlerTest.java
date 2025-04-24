package handler;

import handler.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.RequestBuilder;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpStatusCode;
import webserver.resolver.ResolveResponse;
import webserver.resolver.SessionResolver;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HandlerTest {

    private Handler handler;

    @BeforeEach
    void setUp() {
        // db 초기화 리플렉션
        try {
            Field usersField = Database.class.getDeclaredField("users");
            usersField.setAccessible(true);                   // private 접근 허용
            Map<?, ?> usersMap = (Map<?, ?>) usersField.get(null);
            usersMap.clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        handler = new Handler();
    }

    @Test
    @DisplayName("로그인 요청시 올바르게 로그인 후 리다이렉션 된다")
    void 정상_로그인_테스트() {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        // when
        HttpRequest request = RequestBuilder.post("/login")
                .header("Cookie", "JSESSIONID=1234567890")
                .body("userId=javajigi&password=test")
                .build();
        SessionResolver.injectSession(request);
        ResolveResponse<?> response = handler.login(request, request.getSession());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.FOUND);
        assertThat(response.getHeaders().get("Location")).isEqualTo("/");
    }

    @Test
    @DisplayName("로그인 요청시 잘못된 비밀번호로 요청하면 로그인 실패 페이지로 리다이렉트 된다")
    void 로그인_잘못된_비밀번호_리다이렉트_테스트() {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        // when
        HttpRequest request = RequestBuilder.post("/login")
                .header("Cookie", "JSESSIONID=1234567890")
                .body("userId=javajigi&password=wrongPassword")
                .build();
        SessionResolver.injectSession(request);
        ResolveResponse<?> response = handler.login(request, request.getSession());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.FOUND);
        assertThat(response.getHeaders().get("Location")).isEqualTo("/login/login_failed.html");
    }

    @Test
    @DisplayName("로그인 요청시 잘못된 아이디로 요청하면 로그인 실패 페이지로 리다이렉트 된다")
    void 로그인_잘못된_아이디_리다이렉트_테스트() {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        // when
        HttpRequest request = RequestBuilder.post("/login")
                .header("Cookie", "JSESSIONID=1234567890")
                .body("userId=wrongUserId&password=test")
                .build();
        SessionResolver.injectSession(request);
        ResolveResponse<?> response = handler.login(request, request.getSession());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.FOUND);
        assertThat(response.getHeaders().get("Location")).isEqualTo("/login/login_failed.html");
    }

}
