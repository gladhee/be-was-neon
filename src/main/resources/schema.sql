DROP TABLE IF EXISTS articles;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id   VARCHAR(50) PRIMARY KEY ,
    name      VARCHAR(100) NOT NULL UNIQUE ,
    password  VARCHAR(100) NOT NULL,
    email     VARCHAR(100) NOT NULL
);

CREATE TABLE articles (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    content   VARCHAR(1000) NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_name) REFERENCES users(name)
);
