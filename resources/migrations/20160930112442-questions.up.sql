INSERT INTO question (id, text) values (1, 'Is this working?');
--;;
INSERT INTO answer (id, text)
VALUES  (1, 'No'),
	(2, 'Maybe'),
	(3, 'Definitly'),
	(4, 'Ask again later');
--;;
INSERT INTO question_answer (question_id, answer_id, correct)
VALUES (1, 1, false),
       (1, 2, true),
       (1, 3, false),
       (1, 4, false);
