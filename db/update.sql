USE moPat;

START TRANSACTION;

-- Add column 'version' in the table 'questionnaire'
ALTER TABLE questionnaire ADD COLUMN version INT NOT NULL DEFAULT 1;

-- Create new table 'questionnaire_version_group' with AUTO_INCREMENT, InnoDB and utf8
CREATE TABLE IF NOT EXISTS questionnaire_version_group (
    id BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;

-- Add new column 'version_group_id' in 'questionnaire'a
ALTER TABLE questionnaire
    ADD COLUMN version_group_id BIGINT,
    ADD CONSTRAINT FK_group FOREIGN KEY (version_group_id) REFERENCES questionnaire_version_group(id);

-- Add index to 'version_group_id' in the 'questionnaire' table
CREATE INDEX idx_version_group_id ON questionnaire(version_group_id);

-- Create a new group for each questionnaire with the same name
INSERT INTO questionnaire_version_group (name)
SELECT name
FROM questionnaire
WHERE version_group_id IS NULL;

-- Update the 'questionnaire' table to assign the new group IDs
UPDATE questionnaire q
JOIN (
    SELECT q.id AS questionnaire_id, qvg.id AS version_group_id
    FROM questionnaire q
    JOIN questionnaire_version_group qvg ON qvg.name = q.name
    WHERE q.version_group_id IS NULL
) AS subquery ON q.id = subquery.questionnaire_id
SET q.version_group_id = subquery.version_group_id;

COMMIT;