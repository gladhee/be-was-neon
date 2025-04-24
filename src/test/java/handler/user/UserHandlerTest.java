package handler.user;

import db.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import provider.RequestBuilder;
import webserver.http.exception.HttpException;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpStatusCode;
import webserver.resolver.ResolveResponse;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserHandlerTest {

    private UserHandler handler;

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

        handler = new UserHandler();
    }

    @Test
    @DisplayName("유저 생성 요청시 올바르게 생성 후 리다이렉션 된다")
    void 유저_생성_테스트() {
        // given
        HttpRequest request = RequestBuilder.post("/create")
                .body("userId=javajigi&name=자바지기&password=password&email=javajigi%40slipp.net")
                .build();

        // when
        ResolveResponse<?> response = handler.createUser(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.FOUND);
        assertThat(response.getHeaders().get("Location")).isEqualTo("/");
    }

    @Test
    @DisplayName("유저 생성 요청시 중복된 아이디로 요청하면 CONFLICT 응답을 받는다")
    void 유저_생성_중복_예외_테스트() {
        // given
        HttpRequest request = RequestBuilder.post("/create")
                .body("userId=javajigi&name=자바지기&password=password&email=javajigi%40slipp.net")
                .build();
        handler.createUser(request);

        // when & then
        assertThatThrownBy(() -> handler.createUser(request))
                .isInstanceOf(HttpException.class)
                .hasMessageContaining("409 Conflict");
    }

    @Test
    @DisplayName("유저 생성 요청시 필드가 비어있으면 BAD_REQUEST 응답을 받는다")
    void 유저_생성_필드_비어있음_예외_테스트() {
        // given
        HttpRequest request = RequestBuilder.post("/create")
                .body("userId=javajigi&name=&password=password&email=javajigi%40slipp.net")
                .build();

        // when & then
        assertThatThrownBy(() -> handler.createUser(request))
                .isInstanceOf(HttpException.class)
                .hasMessageContaining("400 Bad Request");
    }

}
