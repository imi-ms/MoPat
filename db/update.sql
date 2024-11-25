use `moPat_user`;

ALTER TABLE `acl_sid` ADD `last_selected_clinic_id` BIGINT NULL;


USE `moPat`;

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
DELETE FROM `configuration_group` WHERE `id`=3;


DELETE FROM `configuration` where `id` in (31,32,33,34,35,43,44,45,46);

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

ALTER TABLE moPat.encounter ADD preselected_clinic_id BIGINT NULL;
ALTER TABLE moPat.encounter ADD CONSTRAINT encounter_clinic_FK FOREIGN KEY (preselected_clinic_id) REFERENCES moPat.clinic(id);
