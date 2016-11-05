CREATE TABLE game_users (
       game_id	     INT REFERENCES game(id),
       user_id	     INT REFERENCES users(id),
       answered	     INT DEFAULT 0,
       correct	     INT DEFAULT 0
);
