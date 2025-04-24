package webserver;

import handler.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.RequestBuilder;
import webserver.http.common.HttpSession;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;
import webserver.mapper.HandlerMapper;
import webserver.resolver.SessionResolver;
import webserver.session.SessionManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DispatcherTest {

    @BeforeEach
    void setUp() {
        // db 초기화 리플렉션
        try {
            Field usersField = Database.class.getDeclaredField("users");
            usersField.setAccessible(true);
            Map<?, ?> usersMap = (Map<?, ?>) usersField.get(null);
            usersMap.clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // 세션 초기화
        try {
            Field sessionManagerField = SessionManager.class.getDeclaredField("sessions");
            sessionManagerField.setAccessible(true);
            Map<?, ?> sessionManagerMap = (Map<?, ?>) sessionManagerField.get(null);
            sessionManagerMap.clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // HandlerMapper 초기화
        HandlerMapper.getInstance().initialize();
    }

    @Test
    @DisplayName("정적 리소스 요청에 올바른 요청시 200 OK 응답과 정적 리소스 바디를 반환한다.")
    void 올바른_정적_리소스_요청시_200_ok_와_바디를_반환_테스트() throws IOException {
        // given

        HttpRequest request = RequestBuilder.get("/index.html").build();
        Dispatcher dispatcher = new Dispatcher(request);

        // when
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(response).isNotNull();
        assertThat(response).contains("200 OK");
        assertThat(response).contains("<html>");
    }

    @Test
    @DisplayName("정적 리소스 요청시 없는 리소스에 대해 404 Not Found 응답을 반환한다.")
    void 없는_정적_리소스_요청시_404_not_found_응답을_반환_테스트() throws IOException {
        // given
        HttpRequest request = RequestBuilder.get("/nonexistent.html").build();
        Dispatcher dispatcher = new Dispatcher(request);

        // when
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(response).isNotNull();
        assertThat(response).contains("404 Not Found");
    }

    @Test
    @DisplayName("POST /users 요청시 유저가 정상적으로 생성되면 302 Found 응답을 반환한다.")
    void 유저_생성_정상_요청시_302_found_응답을_반환_테스트() throws IOException {
        // given
        HttpRequest request = RequestBuilder.post("/users")
                .body("userId=javajigi&name=자바지기&password=test&email=javajigi@naver.com")
                .build();
        Dispatcher dispatcher = new Dispatcher(request);

        // when
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(response).isNotNull();
        assertThat(response).contains("302 Found");
    }

    @Test
    @DisplayName("POST /users 요청시 유저가 이미 존재하면 409 Conflict 응답을 반환한다.")
    void 중복된_유저_생성_요청시_409_Conflict_반환_테스트() throws IOException {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        HttpRequest request = RequestBuilder.post("/users")
                .body("userId=javajigi&name=자바지기&password=test&email=javajigi@naver.com")
                .build();
        Dispatcher newDispatcher = new Dispatcher(request);

        // when
        HttpResponse dispatchResult = newDispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(response).isNotNull();
        assertThat(response).contains("409 Conflict");
    }

    @Test
    @DisplayName("POST /users 요청시 파라미터가 부족하거나 잘못된 경우 400 Bad Request 응답을 반환한다.")
    void 잘못된_생성_요청시_400_Bad_Request_반환_테스트() throws IOException {
        // given
        HttpRequest request = RequestBuilder.post("/users")
                .body("userId=javajigi&password=")
                .build();
        Dispatcher dispatcher = new Dispatcher(request);

        // when
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(response).isNotNull();
        assertThat(response).contains("400 Bad Request");
    }

    @Test
    @DisplayName("POST /login 요청시 유저가 정상적으로 로그인되면 302 Found 응답을 반환한다.")
    void 유저_로그인_정상_요청시_302_found_응답을_반환_테스트() throws IOException {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        HttpRequest request = RequestBuilder.post("/login")
                .header("Cookie", "JSESSIONID=1234567890")
                .body("userId=javajigi&password=test")
                .build();
        Dispatcher dispatcher = new Dispatcher(request);

        // when
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(response).isNotNull();
        assertThat(response).contains("302 Found");
    }

    @Test
    @DisplayName("POST /login 요청시 유저가 존재하지 않으면 로그인 실패 페이지로 리다이렉트 된다.")
    void 존재하지_않는_유저_로그인_요청시_302_Found_반환_테스트() throws IOException {
        // given
        HttpRequest request = RequestBuilder.post("/login")
                .body("userId=nonexistent&password=test")
                .build();
        Dispatcher dispatcher = new Dispatcher(request);

        // when
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        System.out.println(response);
        assertThat(response).isNotNull();
        assertThat(response).contains("302 Found");
    }

    @Test
    @DisplayName("POST /login 요청시 파라미터가 부족하거나 잘못된 경우 로그인 실패 페이지로 리다이렉트 된다.")
    void 잘못된_로그인_요청시_302_Found_반환_테스트() throws IOException {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        HttpRequest request = RequestBuilder.post("/login")
                .body("userId=javajigi&password=")
                .build();
        Dispatcher dispatcher = new Dispatcher(request);

        // when
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(response).isNotNull();
        assertThat(response).contains("302 Found");
    }

    @Test
    @DisplayName("POST /login 요청시 정상 로그인 후 세션에 유저 정보가 담기며 Set Cookie 헤더가 포함된다.")
    void 정상_로그인_요청시_쿠키_및_세션_정상_주입_테스트() throws IOException {
        // given
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        // when
        HttpRequest request = RequestBuilder.post("/login")
                .body("userId=javajigi&password=test")
                .build();
        SessionResolver.injectSession(request);
        Dispatcher newDispatcher = new Dispatcher(request);

        HttpResponse dispatchResult = newDispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(request.getSession()).isNotNull();
        assertThat(request.getSession().getAttribute("user")).isNotNull();
        assertThat(response).contains("Set-Cookie: JSESSIONID=");
    }

    @Test
    @DisplayName("로그인한 유저가 GET / 요청시 해당 유저의 정보가 HTML 에 포함된다.")
    void 메인_페이지_유저_정보_포함_테스트() throws IOException {
        // given
        // 유저 생성
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        // 로그인 요청 -> 세션 메니저에서 setAttrubute 하기 위해서 호출
        HttpRequest loginRequest = RequestBuilder.post("/login")
                .body("userId=javajigi&password=test")
                .build();
        SessionResolver.injectSession(loginRequest);
        Dispatcher loginDispatcher = new Dispatcher(loginRequest);
        loginDispatcher.dispatch();

        // when
        HttpRequest request = RequestBuilder.get("/")
                .header("Cookie", "JSESSIONID=" + loginRequest.getSession().getId())
                .build();
        Dispatcher dispatcher = new Dispatcher(request);
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(request.getSession()).isNotNull();
        assertThat(request.getSession().getAttribute("user")).isNotNull();
        assertThat(response).contains("자바지기님");
    }

    @Test
    @DisplayName("POST /logout 요청시 세션이 무효화되고 302 Found 응답을 반환한다.")
    void 로그아웃_세션_삭제_테스트() throws IOException {
        // given
        // 유저 생성
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        // 로그인 요청 -> 세션 메니저에서 setAttrubute 하기 위해서 호출
        HttpRequest loginRequest = RequestBuilder.post("/login")
                .body("userId=javajigi&password=test")
                .build();
        SessionResolver.injectSession(loginRequest);
        Dispatcher loginDispatcher = new Dispatcher(loginRequest);
        loginDispatcher.dispatch();

        // when
        HttpRequest request = RequestBuilder.post("/logout")
                .header("Cookie", "JSESSIONID=" + loginRequest.getSession().getId())
                .build();
        Dispatcher dispatcher = new Dispatcher(request);
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);
        HttpSession session = request.getSession();

        // then
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("user")).isNull();
        assertThat(response).contains("302 Found");
        assertThat(response).doesNotContain("자바지기님");
    }

    @Test
    @DisplayName("GET /users 요청시 유저 목록이 HTML 에 포함된다.")
    void 저장된_모든_유저_항목_반환_테스트() throws IOException {
        // given
        // 유저 생성
        User user = new User("javajigi", "test", "자바지기", "javajigi@naver.com");
        Database.addUser(user);

        // 로그인 요청 -> 세션 메니저에서 setAttrubute 하기 위해서 호출
        HttpRequest loginRequest = RequestBuilder.post("/login")
                .body("userId=javajigi&password=test")
                .build();
        SessionResolver.injectSession(loginRequest);
        Dispatcher loginDispatcher = new Dispatcher(loginRequest);
        loginDispatcher.dispatch();

        User user2 = new User("glad", "test", "글래드", "glad@codesquad.com");
        User user3 = new User("honux", "test", "호눅스", "honux@codesquad.com");
        Database.addUser(user2);
        Database.addUser(user3);

        // when
        HttpRequest request = RequestBuilder.get("/users")
                .header("Cookie", "JSESSIONID=" + loginRequest.getSession().getId())
                .build();
        Dispatcher dispatcher = new Dispatcher(request);
        HttpResponse dispatchResult = dispatcher.dispatch();
        String response = new String(dispatchResult.getBytes(), StandardCharsets.UTF_8);

        // then
        assertThat(request.getSession()).isNotNull();
        assertThat(response).contains("자바지기");
        assertThat(response).contains("글래드");
        assertThat(response).contains("호눅스");
    }

}
