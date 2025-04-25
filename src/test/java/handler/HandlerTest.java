package handler;

import handler.Article.Article;
import handler.Article.ArticleDao;
import handler.user.User;
import handler.user.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import provider.RequestBuilder;
import webserver.http.exception.HttpException;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpStatusCode;
import webserver.model.Model;
import webserver.resolver.ResolveResponse;
import webserver.resolver.SessionResolver;
import webserver.session.SessionManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandlerTest {

    @Mock
    private UserDao mockUserDao;
    @Mock
    private ArticleDao mockArticleDao;
    @Mock
    private Model model;

    private Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Handler(mockUserDao, mockArticleDao);
        SessionManager.getInstance().removeSession("1234567890");
    }

    @Test
    @DisplayName("쿼리스트링이 없으면 최신 글을 보여준다")
    void 쿼리스트링_없으면_최신글_조회() {
        // given
        HttpRequest request = RequestBuilder.get("/").build();
        SessionResolver.injectSession(request);
        Article latest = new Article("최신글", "작성자");
        when(mockArticleDao.findMaxId()).thenReturn(3L);
        when(mockArticleDao.findById(3L)).thenReturn(Optional.of(latest));
        when(mockArticleDao.existsById(4L)).thenReturn(false);
        when(mockArticleDao.existsById(2L)).thenReturn(true);

        // when
        String view = handler.showPost(request, request.getSession(), model);

        // then
        assertThat(view).isEqualTo("index");
        verify(mockArticleDao).findMaxId();
        verify(mockArticleDao).findById(3L);
        verify(model).addAttribute("post", latest);
        verify(model).addAttribute("prevLink", "/?id=3"); // 최신이 3번이기 때문에 이전은 없음
        verify(model).addAttribute("nextLink", "/?id=2");
    }

    @Test
    @DisplayName("쿼리스트링 id로 글을 조회한다")
    void 쿼리스트링_id_글_조회() {
        // given
        HttpRequest request = RequestBuilder.get("/?id=2").build();
        SessionResolver.injectSession(request);
        Article post = new Article("글 내용", "작성자");
        when(mockArticleDao.findById(2L)).thenReturn(Optional.of(post));
        when(mockArticleDao.existsById(3L)).thenReturn(true);
        when(mockArticleDao.existsById(1L)).thenReturn(true);

        // when
        String view = handler.showPost(request, request.getSession(), model);

        // then
        assertThat(view).isEqualTo("index");
        verify(mockArticleDao).findById(2L);
        verify(model).addAttribute("post", post);
        verify(model).addAttribute("prevLink", "/?id=3");
        verify(model).addAttribute("nextLink", "/?id=1");
    }

    @Test
    @DisplayName("존재하지 않는 id는 404 예외를 발생시킨다")
    void 존재하지_않는_글_조회시_예외() {
        // given
        HttpRequest request = RequestBuilder.get("/?id=99").build();
        SessionResolver.injectSession(request);
        when(mockArticleDao.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.showPost(request, request.getSession(), model))
                .isInstanceOf(HttpException.class)
                .hasMessageContaining("404 Not Found");
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
