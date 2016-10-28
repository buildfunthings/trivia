CREATE TABLE users (
       id    SERIAL,
       username  VARCHAR(64) UNIQUE,
       hash  VARCHAR(128),

       PRIMARY KEY (id)
);
--;;
INSERT INTO users (username, hash) VALUES ('arjen', 'bcrypt+sha512$8584b5740ca5f30abf42b75889657275$12$d7d655c128cc364257729e7d9412c37c810fa1eec4c2e519');
