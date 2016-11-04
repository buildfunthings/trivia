-- :name question-by-id :? :1
-- :doc Retrieve a question by its' ID
select * from question
 where id = :id

-- :name get-random-question-id :? :1
-- :doc Grab random question from database
SELECT id
  FROM question
OFFSET (random() * (SELECT count(*) from question))
 LIMIT 1;

-- :name get-question-data :? :*
-- :doc Get the question and answers by id
SELECT q.id as question_id, q.text as question_text, a.id as answer_id, a.text as answer_text
  FROM question q
 INNER JOIN question_answer qa ON qa.question_id = q.id
 INNER JOIN answer a ON a.id = qa.answer_id
 WHERE question_id = :id

-- :name is-answer-correct? :? :1
-- :doc Retrieve the correctness of the answer to the question
SELECT correct
  FROM question_answer
 WHERE question_id = :question_id
   AND answer_id = :answer_id

-- :name get-user-by-name :? :1
-- :doc Get the user by its username
SELECT username, hash
  FROM users
 WHERE username = :username

-- :name add-user :<!
-- :doc insert user to the database and return its id
INSERT INTO users (username, hash) VALUES (:username, :hash) RETURNING id
