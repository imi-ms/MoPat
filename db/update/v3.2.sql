USE `moPat_user`;

CREATE TABLE IF NOT EXISTS `pin_authorization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sid` bigint(20) NOT NULL,
  `created_on` datetime DEFAULT CURDATE() NOT NULL,
  `remaining_tries` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_fk_6` (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

ALTER TABLE `pin_authorization`
  ADD CONSTRAINT `foreign_fk_6` FOREIGN KEY (`sid`) REFERENCES `acl_sid` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE moPat_user.acl_sid ADD use_pin TINYINT(1) DEFAULT FALSE NULL;
ALTER TABLE moPat_user.acl_sid ADD pin varchar(255) DEFAULT NULL NULL;

USE `moPat`;

INSERT INTO `configuration` (`id`, `type`, `configuration_group_id`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`, `pattern`) VALUES
(78, 'GENERAL', 1, NULL, 15, 'enableGlobalPinAuth', 'BOOLEAN', NULL, 'GLOBAL', 'configuration.label.enableGlobalPinAuth', NULL, NULL, '5c4ca0df-fe5e-4582-8e9e-e290e1ed8efe', 'true', NULL);

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

INSERT INTO moPat.SelectConfiguration_OPTIONS
(SelectConfiguration_id, `options`)
VALUES(33, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetrieverByPID');