# be-was-2024
코드스쿼드 백엔드 교육용 WAS 2024 개정판

## 1. index.html 응답

### static html file 응답

- http://localhost:8080/index.html 접속시 src/main/resources/static/index.html 파일을 응답

### HTTP Request 출력

- HTTP Request를 콘솔에 출력
- 적절하게 파싱 후 `log.debug()`로 출력

### 구조 변경

- `Thread` 기반 구조에서 `Concurrent` 기반 구조로 변경

## 2. 다양한 컴텐츠 타입 지원

### MIME Type 지원

- `html`, `css`, `js`, `ico`, `png`, `jpg` 에 대한 MIME Type 지원

## 3. GET 회원가입

### GET 으로 회원가입 기능

- `http://localhost:8080/register.html` 접속시 회원가입 폼을 응답
- Request parameter로 `userId`, `password`, `name`, `email`을 받음
- 파싱 후 `model.User`클래스에 저장

### 품질 개선

- 유지보수 하기 용이하도록 코드 리팩토링

## 4. POST 회원가입

### POST 으로 회원가입 기능

- 기존 회원가입 기능을 POST 방식으로 변경
- 가입 이후 `http://localhost:8080/index.html`으로 리다이렉트
- GET 으로 회원가입 시도시 **실패**해야함

## 5. 쿠키를 이용한 로그인

### 로그인 기능 구현

- `http://localhost:8080/login/index.html` 접속시 로그인 폼을 응답
- Request parameter로 `userId`, `password`를 받음

### 로그인 성공시

- `http://localhost:8080/index.html`으로 리다이렉트
- 로그인 성공시 `Set-Cookie` 헤더를 이용하여 쿠키를 저장
- 

### 로그인 실패시

- `http://localhost:8080/login/login_failed.html`으로 리다이렉트

## 6. 동적인 HTML

### 동적인 HTML 응답

- 사용자가 로그인 상태인 경우 `index.html`에서 사용자 이름을 표시
- 사용자가 로그인 상태가 아닐 경우 `index.html`에서 [로그인] 버튼을 표시
- 로그인 상태인 경우 `/user/list`에서 사용자 목록을 출력
- 로그인 상태가 아닐 경우 `/user/list`에서 로그인 페이지로 리다이렉트

## 7. 게시글 글쓰기

### 글쓰기 기능 구현

- index.html에 [글쓰기] 버튼을 추가
- 글쓰기 버튼 클릭시 `http://localhost:8080/write.html`로 이동
- `write.html`에서 제목과 내용을 입력받음
- 제목과 내용은 `POST` 방식으로 전송

### 에러페이지 구현

- 발생한 서버오류를 확인할 수 있는 error.html 페이지 구현

### DB 연동

- 게시글, 회원 정보 등을 `H2` 및 `JDBC`를 이용해 DB에 저장
