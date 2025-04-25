package handler.user;

import handler.Article.Article;
import handler.Article.ArticleDao;
import handler.Article.ArticleHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import provider.RequestBuilder;
import webserver.http.common.HttpSession;
import webserver.http.request.HttpRequest;
import webserver.resolver.ResolveResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static webserver.http.response.HttpStatusCode.FOUND;

@ExtendWith(MockitoExtension.class)
class ArticleHandlerTest {

    @Mock
    private UserDao mockUserDao;
    @Mock
    private ArticleDao mockArticleDao;

    private ArticleHandler articleHandler;

    @BeforeEach
    void setUp() {
        articleHandler = new ArticleHandler(mockUserDao, mockArticleDao);
    }

    @Test
    @DisplayName("게시글 작성 페이지 요청 시 로그인한 유저는 게시글 작성 페이지로 이동한다")
    void 로그인_안_된_유저_게시글_작성_폼에서_로그인_페이지로_리다이렉트_테스트() {
        // given
        HttpSession session = new HttpSession("1234567890");
        session.setAttribute("user", null);

        // when
        String viewName = articleHandler.showWritePage(session);

        // then
        assertThat(viewName).isEqualTo("index");
    }

    @Test
    @DisplayName("로그인 된 유저는 게시글 작성 페이지로 이동한다")
    void 로그인_된_유저_게시글_페이지_이동_테스트() {
        // given
        HttpSession session = new HttpSession("1234567890");
        User user = new User("glad", "test", "글래드", "glad@codesquad.com");
        session.setAttribute("user", user);

        // when
        String viewName = articleHandler.showWritePage(session);

        // then
        assertThat(viewName).isEqualTo("article/index");
    }

    @Test
    @DisplayName("게시글 생성 시 로그인이 안 된 유저는 로그인 페이지로 리다이렉트 된다")
    void 로그인_안_된_유저_로그인_페이지로_리다이렉트_테스트() {
        // given
        HttpSession session = new HttpSession("1234567890");
        session.setAttribute("user", null);
        HttpRequest request = RequestBuilder.post("/articles")
                .body("content=게시글 내용")
                .build();

        // when
        ResolveResponse<String> ret = articleHandler.create(request, session);

        // then
        assertThat(ret.getStatusCode()).isEqualTo(FOUND);
        assertThat(ret.getHeaders().get("Location")).isEqualTo("login");
    }

    @Test
    @DisplayName("게시글 생성 시 로그인 된 유저는 게시글이 생성되고 메인 페이지로 리다이렉트 된다")
    void 개시물_생성_테스트() {
        // given
        HttpSession session = new HttpSession("1234567890");
        User user = new User("glad", "test", "글래드", "glad@codesquad.com");
        session.setAttribute("user", user);
        HttpRequest request = RequestBuilder.post("/articles")
                .body("content=Hello World")
                .build();

        // when
        ResolveResponse<String> ret = articleHandler.create(request, session);

        // then
        assertThat(ret.getStatusCode()).isEqualTo(FOUND);
        assertThat(ret.getHeaders().get("Location")).isEqualTo("/");
        verify(mockArticleDao).save(any(Article.class));
    }

}
