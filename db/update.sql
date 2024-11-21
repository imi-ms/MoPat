USE `moPat`;

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

INSERT INTO moPat.clinic_configuration (`type`,`attribute`,configuration_type,description_message_code,class,label_message_code,`position`,test_method,update_method,uuid,value,parent,mapped_configuration_group) VALUES
	 ('GENERAL','usePatientDataLookup','BOOLEAN',NULL,'GLOBAL','configuration.label.usePatientDataLookup',1,NULL,'','ea896f07-7666-4d20-ad2d-9e2e65cbca89','false',NULL,'configurationGroup.label.usePatientLookUp'),
	 ('GENERAL','registerPatientData','BOOLEAN',NULL,'GLOBAL','configuration.label.registerPatientData',9,NULL,'','92ddfc6f-5e16-426a-8203-4edf22fec17c','false',NULL,NULL),
	 ('GENERAL','usePseudonymizationService','BOOLEAN',NULL,'GLOBAL','configuration.label.pseudonymizationService',6,NULL,'','0f64ebe5-40e6-4d0d-8a3a-3af7274aa9c8','false',NULL,'configurationGroup.label.pseudonymization');

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

--
-- Daten für Tabelle `SelectConfiguration_OPTIONS`
--

INSERT INTO `SelectClinicConfiguration_OPTIONS` (`SelectClinicConfiguration_id`, `options`) VALUES
(33, 'de.imi.mopat.helper.controller.RandomPatientDataRetrieverImpl'),
(33, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever'),
(33, 'de.imi.mopat.helper.controller.DummyPatientDataRetrieverImpl');

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


use `moPat_user`;

ALTER TABLE `acl_sid` ADD `last_selected_clinic_id` BIGINT NULL;
