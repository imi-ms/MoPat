USE `moPat`;

-- moPat.clinic_configuration definition

CREATE TABLE IF NOT EXISTS `clinic_configuration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
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
  `type` varchar(31) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb3;

ALTER TABLE `clinic_configuration`
	ADD CONSTRAINT `fk_clinic_configuration_parent` FOREIGN KEY (`parent`) REFERENCES `clinic_configuration` (`id`) ON DELETE CASCADE;

INSERT INTO clinic_configuration (`id`, `type`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`) VALUES
(31, 'GENERAL',NULL, 1, 'usePatientDataLookup', 'BOOLEAN', NULL, 'GLOBAL', 'configuration.label.usePatientDataLookup', NULL, '', 'ea896f07-7666-4d20-ad2d-9e2e65cbca89', 'false'),
(32, 'GENERAL',31, 2, 'activateHisToggle', 'BOOLEAN', NULL, 'de.imi.mopat.controller.SurveyController', 'configuration.label.survey.activateHisToggle', NULL, '', '8c75cd85-fe2d-43d0-bf20-d1f4509f868b', 'false'),
(33, 'SELECT',31, 3, 'patientDataRetrieverClass', 'SELECT', NULL, 'de.imi.mopat.helper.controller.ClinicPatientDataRetrieverFactoryBean', 'configuration.label.patientRetrieverClass', NULL, '', '7ff58798-b41f-42a2-b170-b2350fdeb70e', 'de.imi.mopat.helper.controller.RandomPatientDataRetrieverImp'),
(34, 'GENERAL',31, 4, 'HL7v22PatientInformationRetrieverHostname', 'STRING', NULL, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever', 'configuration.label.HL7v22PatientInformationRetrieverHostname', NULL, '', '82957ee9-2409-4978-84af-c3cf0edb9293', ''),
(35, 'GENERAL',31, 5, 'HL7v22PatientInformationRetrieverPort', 'INTEGER', NULL, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever', 'configuration.label.HL7v22PatientInformationRetrieverPort', NULL, '', 'b6410c08-fc15-4f67-9601-9ccee136898d', ''),
(43, 'GENERAL',NULL, 9, 'registerPatientData', 'BOOLEAN', NULL, 'GLOBAL', 'configuration.label.registerPatientData', NULL, '', '92ddfc6f-5e16-426a-8203-4edf22fec17c', 'false'),
(44, 'GENERAL',NULL, 6, 'usePseudonymizationService', 'BOOLEAN', NULL, 'GLOBAL', 'configuration.label.pseudonymizationService', NULL, '', '0f64ebe5-40e6-4d0d-8a3a-3af7274aa9c8', 'false'),
(45, 'GENERAL',44, 7, 'pseudonymizationServiceUrl', 'STRING', NULL, 'de.imi.mopat.controller.PseudonymizationController', 'configuration.label.pseudonymizationService.path', NULL, '', '59ef4f9c-b298-44e0-b9b4-d372e9f8a066', ''),
(46, 'GENERAL',44, 8, 'pseudonymizationServiceApiKey', 'STRING', NULL, 'de.imi.mopat.controller.PseudonymizationController', 'configuration.label.pseudonymizationServiceApiKey', NULL, '', '75694313-f767-4107-b26f-6f5406484bb8', '');
-- moPat.clinic_configuration_mapping definition

CREATE TABLE IF NOT EXISTS `clinic_configuration_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `value` varchar(255) DEFAULT NULL,
  `clinic_id` bigint DEFAULT NULL,
  `clinic_configuration_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
  KEY `fk_clinic_id` (`clinic_id`)
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

--
-- Daten für Tabelle `SelectConfiguration_OPTIONS`
--

INSERT INTO `SelectClinicConfiguration_OPTIONS` (`SelectClinicConfiguration_id`, `options`) VALUES
(33, 'de.imi.mopat.helper.controller.RandomPatientDataRetrieverImpl'),
(33, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever'),
(33, 'de.imi.mopat.helper.controller.DummyPatientDataRetrieverImpl');

ALTER TABLE `SelectClinicConfiguration_OPTIONS`
  ADD CONSTRAINT `SelectClinicConfiguration_OPTIONS_SelectClinicConfiguration_id` FOREIGN KEY (`SelectClinicConfiguration_id`) REFERENCES `clinic_configuration` (`id`);
