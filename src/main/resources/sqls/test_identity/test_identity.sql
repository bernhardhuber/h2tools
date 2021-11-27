--
-- Sample SQL Script
--
DROP TABLE IF EXISTS TEST_IDENTITY;
CREATE TABLE TEST_IDENTITY(ID IDENTITY PRIMARY KEY, NAME VARCHAR(255), AGE INT);

INSERT INTO TEST_IDENTITY VALUES(1, 'Hello', 1);
INSERT INTO TEST_IDENTITY VALUES(2, 'World', 2);
INSERT INTO TEST_IDENTITY VALUES(3, 'Earth', 3);

SELECT * FROM TEST_IDENTITY ORDER BY ID;

UPDATE TEST_IDENTITY SET NAME='Hi' WHERE ID=1;
DELETE FROM TEST_IDENTITY WHERE ID=2;

SELECT COUNT(*) FROM TEST;

