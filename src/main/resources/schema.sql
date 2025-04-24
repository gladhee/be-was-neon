DROP TABLE IF EXISTS articles;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   VARCHAR(50),
    name      VARCHAR(100) NOT NULL,
    password  VARCHAR(100) NOT NULL,
    email     VARCHAR(100) NOT NULL
);

CREATE TABLE articles (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    content   VARCHAR(1000) NOT NULL,
    user_id   BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
