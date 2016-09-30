CREATE TABLE question_answer (
       question_id	     INT REFERENCES question(id),
       answer_id	     INT REFERENCES answer(id),
       correct		     boolean default false
);
