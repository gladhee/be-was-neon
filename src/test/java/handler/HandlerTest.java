package handler;

import handler.user.User;
import handler.user.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import provider.RequestBuilder;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpStatusCode;
import webserver.resolver.ResolveResponse;
import webserver.resolver.SessionResolver;
import webserver.session.SessionManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandlerTest {

    @Mock
    private UserDao mockUserDao;

    private Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Handler(mockUserDao);
        SessionManager.getInstance().removeSession("1234567890");
    }

    @Test
    @DisplayName("로그인 요청시 올바르게 로그인 후 리다이렉션 된다")
    void 정상_로그인_테스트() {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        when(mockUserDao.findByUserId("javajigi")).thenReturn(Optional.of(user));

        // when
        HttpRequest request = RequestBuilder.post("/login")
                .header("Cookie", "JSESSIONID=1234567890")
                .body("userId=javajigi&password=test")
                .build();
        SessionResolver.injectSession(request);

        // when
        ResolveResponse<?> response = handler.login(request, request.getSession());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.FOUND);
        assertThat(response.getHeaders().get("Location")).isEqualTo("/");
        assertThat(request.getSession().getAttribute("user")).isEqualTo(user);
        verify(mockUserDao).findByUserId("javajigi");
    }

    @Test
    @DisplayName("로그인 요청시 잘못된 비밀번호로 요청하면 로그인 실패 페이지로 리다이렉트 된다")
    void 로그인_잘못된_비밀번호_리다이렉트_테스트() {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        when(mockUserDao.findByUserId("javajigi")).thenReturn(Optional.of(user));

        HttpRequest request = RequestBuilder.post("/login")
                .header("Cookie", "JSESSIONID=1234567890")
                .body("userId=javajigi&password=wrongPassword")
                .build();
        SessionResolver.injectSession(request);

        // when
        ResolveResponse<?> response = handler.login(request, request.getSession());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.FOUND);
        assertThat(response.getHeaders().get("Location"))
                .isEqualTo("/login/login_failed.html");
        // 세션에는 user 가 저장되지 않아야 함
        assertThat(request.getSession().getAttribute("user")).isNull();
        verify(mockUserDao).findByUserId("javajigi");
    }

    @Test
    @DisplayName("로그인 요청시 잘못된 아이디로 요청하면 로그인 실패 페이지로 리다이렉트 된다")
    void 로그인_잘못된_아이디_리다이렉트_테스트() {
        // given
        when(mockUserDao.findByUserId("wrongUserId")).thenReturn(Optional.empty());

        HttpRequest request = RequestBuilder.post("/login")
                .header("Cookie", "JSESSIONID=1234567890")
                .body("userId=wrongUserId&password=test")
                .build();
        SessionResolver.injectSession(request);

        // when
        ResolveResponse<?> response = handler.login(request, request.getSession());

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.FOUND);
        assertThat(response.getHeaders().get("Location"))
                .isEqualTo("/login/login_failed.html");
        assertThat(request.getSession().getAttribute("user")).isNull();
        verify(mockUserDao).findByUserId("wrongUserId");
    }

}
