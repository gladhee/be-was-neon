package handler.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import provider.RequestBuilder;
import webserver.http.common.HttpSession;
import webserver.http.exception.HttpException;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpStatusCode;
import webserver.model.Model;
import webserver.resolver.ResolveResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private UserDao mockUserDao;

    private UserHandler handler;
    private HttpSession session;
    private Model model;

    @BeforeEach
    void setUp() {
        handler = new UserHandler(mockUserDao);
        session = new HttpSession("test-session");
        model = new Model();
    }

    @Test
    @DisplayName("유저 목록 접근시 로그인하지 않으면 'login' 뷰를 반환한다")
    void 로그인_안_된_유저_로그인_페이지_리디렉션_테스트() {
        String viewName = handler.listUsers(session, model);

        assertThat(viewName).isEqualTo("login");
    }

    @Test
    @DisplayName("로그인한 경우 user-list 뷰와 모델에 itemsHtml 이 채워진다")
    void 유저_목록_페이지_반환_테스트() {
        // given
        User currentUser = new User("glad", "test", "글래드", "glad@codesquad.com");
        session.setAttribute("user", currentUser);

        List<User> allUsers = List.of(
                new User("honux", "test", "호눅스", "honux@codesquad.com"),
                new User("bingbing", "test", "빙빙", "bingbing@codesquad.com")
        );
        when(mockUserDao.findAll()).thenReturn(allUsers);

        // when
        String viewName = handler.listUsers(session, model);

        // then
        assertThat(viewName).isEqualTo("user-list");

        String html = (String) model.getAttribute("itemsHtml");
        assertThat(html)
                .contains("호눅스").contains("honux@codesquad.com")
                .contains("빙빙").contains("bingbing@codesquad.com");
        verify(mockUserDao).findAll();
    }

    @Test
    @DisplayName("유저 생성 요청시 올바르게 생성 후 리다이렉션 된다")
    void 유저_생성_테스트() {
        // given
        String body = "userId=glad&name=글래드&password=test&email=glad@codesquad.com";
        HttpRequest req = RequestBuilder.post("/users")
                .body(body)
                .build();

        when(mockUserDao.findByUserId("glad"))
                .thenReturn(Optional.empty());

        // when
        ResolveResponse<String> resp = handler.createUser(req);

        // then
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatusCode.FOUND);
        assertThat(resp.getHeaders().get("Location")).isEqualTo("/");

        verify(mockUserDao).save(argThat(u ->
                u.getUserId().equals("glad") &&
                        u.getName().equals("글래드") &&
                        u.getPassword().equals("test") &&
                        u.getEmail().equals("glad@codesquad.com")
        ));
    }

    @Test
    @DisplayName("유저 생성 요청시 중복된 아이디로 요청하면 CONFLICT 응답을 받는다")
    void 유저_생성_중복_예외_테스트() {
        // given
        String body = "userId=glad&name=글래드&password=test&email=glad@codesquad.com";
        HttpRequest req = RequestBuilder.post("/users")
                .body(body)
                .build();

        when(mockUserDao.findByUserId("glad"))
                .thenReturn(Optional.of(new User("glad", "test", "글래드", "glad@codesquad.com")));

        // when & then
        assertThatThrownBy(() -> handler.createUser(req))
                .isInstanceOf(HttpException.class)
                .hasMessageContaining("409 Conflict");

        verify(mockUserDao, never()).save(any());
    }

    @Test
    @DisplayName("유저 생성 요청시 필드가 비어있으면 BAD_REQUEST 응답을 받는다")
    void 유저_생성_필드_비어있음_예외_테스트() {
        // given:
        String body = "userId=jigi&name=&password=pass";
        HttpRequest req = RequestBuilder.post("/users")
                .body(body)
                .build();

        // when & then
        assertThatThrownBy(() -> handler.createUser(req))
                .isInstanceOf(HttpException.class)
                .hasMessageContaining("400 Bad Request");

        verify(mockUserDao, never()).save(any());
    }

}
