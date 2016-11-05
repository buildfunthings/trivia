CREATE TABLE game_questions (
       game_id		INT REFERENCES game(id),
       question_id	INT REFERENCES question(id),

       CONSTRAINT unique_question_in_game UNIQUE (game_id, question_id)
);
