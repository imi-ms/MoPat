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

ALTER TABLE `acl_sid` ADD `last_selected_clinic_id` BIGINT NULL;

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

UPDATE `configuration_group` SET `position` = `position` + 1 WHERE `position`>3;

INSERT INTO `configuration_group` (`id`, `uuid`, `position`, `name`, `label_message_code`, `repeating`) VALUES
(13, '99f1e2d6-a7b9-4c12-8cbf-0e38dec7a8e8', 3, 'patient lookup 1', 'configurationGroup.label.usePatientLookUp', 1),
(14, '897e23cf-4155-4e82-b56f-a58a9aaef431', 4, 'pseudonymization 1', 'configurationGroup.label.pseudonymization', 1);

INSERT INTO `configuration` (`id`, `type`, `configuration_group_id`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`, `pattern`) VALUES
(80, 'GENERAL', 13, NULL, 2, 'activateHisToggle', 'BOOLEAN', NULL, 'de.imi.mopat.controller.SurveyController', 'configuration.label.survey.activateHisToggle', NULL, '', '8c75cd85-fe2d-43d0-bf20-d1f4509f868b', 'false', ''),
(81, 'SELECT', 13, NULL, 3, 'patientDataRetrieverClass', 'SELECT', NULL, 'de.imi.mopat.helper.controller.ClinicPatientDataRetrieverFactoryBean', 'configuration.label.patientRetrieverClass', NULL, '', '7ff58798-b41f-42a2-b170-b2350fdeb70e', 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever', ''),
(82, 'GENERAL', 13, NULL, 4, 'HL7v22PatientInformationRetrieverHostname', 'STRING', NULL, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever', 'configuration.label.HL7v22PatientInformationRetrieverHostname', NULL, '', '82957ee9-2409-4978-84af-c3cf0edb9293', '', ''),
(83, 'GENERAL', 13, NULL, 5, 'HL7v22PatientInformationRetrieverPort', 'INTEGER', NULL, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever', 'configuration.label.HL7v22PatientInformationRetrieverPort', NULL, '', 'b6410c08-fc15-4f67-9601-9ccee136898d', '', ''),
(84, 'GENERAL', 14, NULL, 7, 'pseudonymizationServiceUrl', 'STRING', NULL, 'de.imi.mopat.controller.PseudonymizationController', 'configuration.label.pseudonymizationService.path', NULL, '', '59ef4f9c-b298-44e0-b9b4-d372e9f8a066', '', ''),
(85, 'GENERAL', 14, NULL, 8, 'pseudonymizationServiceApiKey', 'STRING', NULL, 'de.imi.mopat.controller.PseudonymizationController', 'configuration.label.pseudonymizationServiceApiKey', NULL, '', '75694313-f767-4107-b26f-6f5406484bb8', '', '');


UPDATE SelectConfiguration_OPTIONS set `SelectConfiguration_id` = 81 WHERE `SelectConfiguration_id`=33;

UPDATE configuration AS cfg1 JOIN configuration AS cfg2 ON cfg2.id = 32 SET cfg1.value = cfg2.value WHERE cfg1.id = 80;
UPDATE configuration AS cfg1 JOIN configuration AS cfg2 ON cfg2.id = 33 SET cfg1.value = cfg2.value WHERE cfg1.id = 81;
UPDATE configuration AS cfg1 JOIN configuration AS cfg2 ON cfg2.id = 34 SET cfg1.value = cfg2.value WHERE cfg1.id = 82;
UPDATE configuration AS cfg1 JOIN configuration AS cfg2 ON cfg2.id = 35 SET cfg1.value = cfg2.value WHERE cfg1.id = 83;
UPDATE configuration AS cfg1 JOIN configuration AS cfg2 ON cfg2.id = 45 SET cfg1.value = cfg2.value WHERE cfg1.id = 84;
UPDATE configuration AS cfg1 JOIN configuration AS cfg2 ON cfg2.id = 46 SET cfg1.value = cfg2.value WHERE cfg1.id = 85;

-- moPat.clinic_configuration definition

CREATE TABLE IF NOT EXISTS `clinic_configuration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(31) DEFAULT NULL,
  `attribute` varchar(255) NOT NULL,
  `configuration_type` varchar(255) NOT NULL,
  `description_message_code` varchar(255) DEFAULT NULL,
  `class` varchar(255) NOT NULL,
  `label_message_code` varchar(255) NOT NULL,
  `position` int NOT NULL,
  `test_method` varchar(255) DEFAULT NULL,
  `update_method` varchar(255) DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `parent` bigint DEFAULT NULL,
  `mapped_configuration_group` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_clinic_configuration_parent` (`parent`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb3;

ALTER TABLE `clinic_configuration`
	ADD CONSTRAINT `fk_clinic_configuration_parent` FOREIGN KEY (`parent`) REFERENCES `clinic_configuration` (`id`) ON DELETE CASCADE;

INSERT INTO clinic_configuration (`id`,`type`,`attribute`,configuration_type,description_message_code,class,label_message_code,`position`,test_method,update_method,uuid,value,parent,mapped_configuration_group) VALUES
	 (1,'GENERAL','usePatientDataLookup','BOOLEAN',NULL,'GLOBAL','configuration.label.usePatientDataLookup',1,NULL,'','ea896f07-7666-4d20-ad2d-9e2e65cbca89','false',NULL,'configurationGroup.label.usePatientLookUp'),
	 (2,'GENERAL','registerPatientData','BOOLEAN',NULL,'GLOBAL','configuration.label.registerPatientData',9,NULL,'','92ddfc6f-5e16-426a-8203-4edf22fec17c','false',NULL,NULL),
	 (3,'GENERAL','usePseudonymizationService','BOOLEAN',NULL,'GLOBAL','configuration.label.pseudonymizationService',6,NULL,'','0f64ebe5-40e6-4d0d-8a3a-3af7274aa9c8','false',NULL,'configurationGroup.label.pseudonymization');

-- moPat.clinic_configuration_mapping definition

CREATE TABLE IF NOT EXISTS `clinic_configuration_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `value` varchar(255) DEFAULT NULL,
  `clinic_id` bigint DEFAULT NULL,
  `clinic_configuration_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_clinic_id` (`clinic_id`),
  KEY `fk_clinic_configuration_id` (`clinic_configuration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

ALTER TABLE `clinic_configuration_mapping`
    ADD CONSTRAINT FK_clinic_configuration_id FOREIGN KEY (clinic_configuration_id) REFERENCES clinic_configuration(id) ON DELETE CASCADE;

ALTER TABLE `clinic_configuration_mapping`
    ADD CONSTRAINT FK_clinic_id FOREIGN KEY (clinic_id) REFERENCES clinic(id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS `SelectClinicConfiguration_OPTIONS` (
  `SelectClinicConfiguration_id` bigint(20) DEFAULT NULL,
  `options` varchar(255) NOT NULL,
  KEY `SelectClinicConfiguration_OPTIONS_SelectClinicConfiguration_id` (`SelectClinicConfiguration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `SelectClinicConfiguration_OPTIONS`
  ADD CONSTRAINT `SelectClinicConfiguration_OPTIONS_SelectClinicConfiguration_id` FOREIGN KEY (`SelectClinicConfiguration_id`) REFERENCES `clinic_configuration` (`id`);


-- moPat.clinic_configuration_group_mapping definition

CREATE TABLE IF NOT EXISTS `clinic_configuration_group_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `clinic_configuration_mapping_id` bigint DEFAULT NULL,
  `configuration_group_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb3;


ALTER TABLE `clinic_configuration_group_mapping` ADD CONSTRAINT `clinic_configuration_group_mapping_fk1` FOREIGN KEY (`clinic_configuration_mapping_id`) REFERENCES `clinic_configuration_mapping` (`id`);
ALTER TABLE `clinic_configuration_group_mapping` ADD CONSTRAINT `clinic_configuration_group_mapping_fk2` FOREIGN KEY (`configuration_group_id`) REFERENCES `configuration_group` (`id`);


INSERT INTO `clinic_configuration_mapping` (value, clinic_id, clinic_configuration_id)
SELECT cfg1.value, c.id, 1
FROM `clinic` c
JOIN `configuration` cfg1 ON cfg1.id = 31
UNION ALL
SELECT cfg2.value, c.id, 2
FROM `clinic` c
JOIN `configuration` cfg2 ON cfg2.id = 43
UNION ALL
SELECT cfg3.value, c.id, 3
FROM `clinic` c
JOIN `configuration` cfg3 ON cfg3.id = 44;

-- For clinic_configuration_id = 1 and group id 13
INSERT INTO `clinic_configuration_group_mapping` (clinic_configuration_mapping_id, configuration_group_id)
SELECT ccm.id, 13
FROM `clinic_configuration_mapping` ccm
WHERE ccm.clinic_configuration_id = 1;

-- For clinic_configuration_id = 3 and group id 14
INSERT INTO `clinic_configuration_group_mapping` (clinic_configuration_mapping_id, configuration_group_id)
SELECT ccm.id, 14
FROM `clinic_configuration_mapping` ccm
WHERE ccm.clinic_configuration_id = 3;


ALTER TABLE encounter ADD clinic_id BIGINT NULL;
ALTER TABLE encounter ADD CONSTRAINT encounter_clinic_FK FOREIGN KEY (clinic_id) REFERENCES moPat.clinic(id);

ALTER TABLE encounter_scheduled ADD clinic_id BIGINT NULL;
ALTER TABLE encounter_scheduled ADD CONSTRAINT encounter_scheduled_clinic_FK FOREIGN KEY (clinic_id) REFERENCES moPat.clinic(id);

DELETE FROM `configuration` where `id` in (31,32,33,34,35,43,44,45,46);
DELETE FROM `configuration_group` WHERE `id`=3;