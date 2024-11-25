--
-- Datenbank: `moPat`
--
CREATE DATABASE `moPat` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `moPat`;

-- --------------------------------------------------------
--
-- Tabellenstruktur für Tabelle `configuration`
--

CREATE TABLE IF NOT EXISTS `configuration` (
  `id` bigint(20) AUTO_INCREMENT NOT NULL,
  `configuration_group_id` bigint(20) NOT NULL,
  `parent` bigint(20) DEFAULT NULL,
  `position` int(11) NOT NULL,
  `type` varchar(31) DEFAULT NULL,
  `attribute` varchar(255) NOT NULL,
  `configuration_type` varchar(255) NOT NULL,
  `description_message_code` varchar(255) DEFAULT NULL,
  `class` varchar(255) NOT NULL,
  `label_message_code` varchar(255) NOT NULL,
  `test_method` varchar(255) DEFAULT NULL,
  `update_method` varchar(255) DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `pattern` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_configuration_group_id` (`configuration_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8,
  AUTO_INCREMENT = 58;

--
-- Daten für Tabelle `configuration`
--

INSERT INTO `configuration` (`id`, `type`, `configuration_group_id`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`, `pattern`) VALUES
(1, 'SELECT', 1, NULL, 1, 'defaultLanguage', 'SELECT', NULL, 'GLOBAL', 'configuration.label.defaultLanguage', NULL, '', '44bfc178-b763-4cb7-a4ea-c34d5eaafcc3', 'de_DE', ''),
(2, 'GENERAL', 1, NULL, 2, 'logo', 'IMAGE', 'configuration.description.logo', 'GLOBAL', 'configuration.label.logo', NULL, '', 'd22708cf-8804-4463-a39f-66c10509b242', NULL, ''),
(3, 'SELECT', 1, NULL, 3, 'caseNumberType', 'SELECT', NULL, 'de.imi.mopat.controller.SurveyController', 'configuration.label.caseNumberType', NULL, '', '83dc6771-e305-4793-91bc-44a9b36ca501', 'text', ''),
(4, 'GENERAL', 4, NULL, 1, 'exportPath', 'LOCAL_PATH', 'configuration.description.exportPath', 'de.imi.mopat.io.impl.EncounterExporterTemplateOrbis', 'configuration.label.exportOrbisPath', NULL, '', '76d2fb9b-735a-4017-8b05-3e36476068df', '/var/lib/tomcat10/export/', ''),
(5, 'GENERAL', 6, NULL, 1, 'exportInDirectory', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.exportODMInDirectory', NULL, '', 'b7ac8e70-1fe1-11e5-867f-0800200c9a66', 'false', ''),
(6, 'GENERAL', 6, 5, 2, 'exportPath', 'LOCAL_PATH', 'configuration.description.exportPath', 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.exportODMPath', NULL, NULL, 'd044a646-88d4-4a87-bd74-6a23f8c90bf1', '/var/lib/tomcat10/export/ODM/', ''),
(7, 'GENERAL', 6, NULL, 3, 'exportViaRest', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.exportODMViaRest', NULL, '', '0d5f5690-1fe2-11e5-867f-0800200c9a66', 'false', ''),
(8, 'GENERAL', 6, 7, 4, 'exportUrl', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.exportODMUrl', NULL, NULL, 'd044a646-88d4-4a87-bd74-6a23f8c90bf1', '', ''),
(9, 'GENERAL', 5, NULL, 1, 'exportInDirectory', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7InDirectory', NULL, '', '2e147a90-24b6-11e5-867f-0800200c9a66', 'false', ''),
(10, 'GENERAL', 5, 9, 2, 'exportPath', 'LOCAL_PATH', 'configuration.description.exportPath', 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7Path', NULL, NULL, '33dcbbe5-24b6-11e5-867f-0800200c9a66', '/var/lib/tomcat10/export/HL7/', ''),
(11, 'GENERAL', 5, NULL, 3, 'exportViaCommunicationServer', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7ViaCommunicationServer', NULL, '', '33dcbbe4-24 b6-11e5-867f-0800200c9a66', 'false', ''),
(12, 'GENERAL', 5, 11, 4, 'exportHost', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7Host', NULL, NULL, '33dcbbe3-24b6-11e5-867f-0800200c9a66', '', NULL),
(13, 'GENERAL', 5, 11, 5, 'exportPort', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7Port', NULL, NULL, '47d2e702-24b6-11e5-867f-0800200c9a66', '', NULL),
(14, 'GENERAL', 5, 11, 6, 'sendingFacility', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7SendingFacility', NULL, NULL, 'aed9be70-2582-11e5-867f-0800200c9a66', 'TEST', NULL),
(15, 'GENERAL', 5, 11, 7, 'receivingApplication', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7ReceivingApplication', NULL, NULL, 'aed9be71-2582-11e5-867f-0800200c9a66', 'ORBIS', NULL),
(16, 'GENERAL', 5, 11, 8, 'receivingFacility', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7ReceivingFacility', NULL, NULL, 'aed9be72-2582-11e5-867f-0800200c9a66', 'CHRONO', NULL),
(17, 'GENERAL', 5, 11, 9, 'OBRFillerOrderNumber', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7_OBRFillerOrderNumber', NULL, NULL, 'aed9be73-2582-11e5-867f-0800200c9a66', 'MOPAT-1', NULL),
(18, 'GENERAL', 1, NULL, 4, 'objectStoragePath', 'STRING', 'configuration.description.storagePath', 'GLOBAL', 'configuration.label.object.storagePath', NULL, '', '9f8b7c96-6f15-4ddf-99f5-8cc883094035', '/var/lib/tomcat10/upload/', ''),
(19, 'GENERAL', 7, NULL, 1, 'finishedEncounterTimeWindowInMillis', 'LONG', 'configuration.description.finishedEncounterTimeWindowInMillis', 'GLOBAL', 'configuration.label.encounter.finishedEncounterTimeWindowInMillis', NULL, '', '2a4e7641-c364-4d52-8feb-2e681a48f701', '2592000000', ''),
(20, 'GENERAL', 7, NULL, 2, 'incompleteEncounterTimeWindowInMillis', 'LONG', 'configuration.description.incompleteEncounterTimeWindowInMillis', 'GLOBAL', 'configuration.label.encounter.incompleteEncounterTimeWindowInMillis', NULL, '', '99c9b323-fd07-4fc6-b433-0dcffaf3d623', '15552000000', ''),
(21, 'GENERAL', 8, NULL, 1, 'applicationMailerActivated', 'BOOLEAN', NULL, 'de.imi.mopat.helper.controller.ApplicationMailer', 'configuration.label.applicationMailer.activated', NULL, '', '2e1a4815-9e46-449f-8763-c229970304ff', 'false', ''),
(22, 'GENERAL', 8, 21, 2, 'mailSenderHost', 'STRING', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.mailSender.host', NULL, '', '0721fec1-99ba-476d-b24e-744c08225f2f', 'mailsystem.mopat.de', ''),
(23, 'GENERAL', 8, 21, 3, 'mailSenderPort', 'INTEGER', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.mailSender.port', NULL, '', 'a7ff206c-93ba-4bdc-8583-16c0ca395136', '12345', ''),
(24, 'GENERAL', 8, 21, 6, 'mailSenderUsername', 'STRING', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.mailSender.username', NULL, '', '7ddceb4a-dc8a-46e7-957a-7768b644c225', 'mopat', ''),
(25, 'GENERAL', 8, 21, 7,  'mailSenderPassword', 'PASSWORD', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.mailSender.password', NULL, '', '352e233a-8fa8-4261-bd51-f300efb8c055', '', ''),
(26, 'PATTERN', 8, 21, 8, 'mailFrom', 'PATTERN', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.mailSender.from', NULL, '', 'feea560b-eb9d-47a4-9910-f4b0fb162efa', '', '^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$'),
(27, 'GENERAL', 8, 21, 5, 'mailSenderAuth', 'BOOLEAN', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.mailSender.auth', NULL, '', 'f412942d-61ea-4986-bd89-6a7bac25095e', 'false', ''),
(28, 'GENERAL', 8, 21, 4, 'mailSenderStartTLS', 'BOOLEAN', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.mailSender.startTLS', NULL, '', '9836b4e0-c62c-4ce4-97f5-b3d89e00046b', 'false', ''),
(29, 'PATTERN', 8, 21, 9, 'mailFooterMail', 'PATTERN', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.applicationMailer.eMailFooter', NULL, '', '2b1bead5-28a6-407f-94c0-42c02eb28b98', '', '^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$'),
(30, 'GENERAL', 8, 21, 10, 'mailFooterPhone', 'STRING', NULL, 'de.imi.mopat.helper.controller.MailSender', 'configuration.label.applicationMailer.phoneFooter', NULL, '', '39348db8-c551-4ee0-b3c1-cd84278839d5', '', ''),
(36, 'GENERAL', 2, NULL, 1, 'activeDirectoryLdapAuthenticationProviderActivated', 'BOOLEAN', NULL, 'de.imi.mopat.auth.MoPatActiveDirectoryLdapAuthenticationProvider', 'configuration.label.activeDirectoryLdapAuthenticationProviderActivated', NULL, '', '6024666a-df73-46db-b0a3-50e1c973adfb', 'false', ''),
(37, 'GENERAL', 2, 36, 3, 'activeDirectoryLdapAuthenticationProviderDomain', 'STRING', NULL, 'de.imi.mopat.auth.MoPatActiveDirectoryLdapAuthenticationProvider', 'configuration.label.activeDirectoryLdapAuthenticationProviderDomain', NULL, '', '010754f6-fe1f-42a5-9024-4bffcde7f6f7', '', ''),
(38, 'SELECT', 2, 36, 4, 'activeDirectoryLdapAuthenticationProviderDefaultLanguage', 'SELECT', NULL, 'de.imi.mopat.auth.MoPatActiveDirectoryLdapAuthenticationProvider', 'configuration.label.activeDirectoryLdapAuthenticationProviderDefaultLanguage', NULL, '', '3a346f60-48ca-4591-b36d-22efea480126', 'de_DE', ''),
(39, 'GENERAL', 2, 36, 2, 'activeDirectoryLdapAuthenticationProviderUrl', 'STRING', NULL, 'de.imi.mopat.auth.MoPatActiveDirectoryLdapAuthenticationProvider', 'configuration.label.activeDirectoryLdapAuthenticationProviderUrl', NULL, '', 'f9e9c145-49c5-48a9-94cf-2212f529615d', '', NULL),
(40, 'GENERAL', 2, 36, 5, 'activeDirectoryLdapAuthenticationProviderSupportPhone', 'STRING', NULL, 'de.imi.mopat.auth.MoPatActiveDirectoryLdapAuthenticationProvider', 'configuration.label.activeDirectoryLdapAuthenticationProviderSupportPhone', NULL, '', 'b17b097e-7519-4d61-9480-18758696bd1f', '', ''),
(41, 'PATTERN', 9, NULL, 1, 'supportMail', 'PATTERN', NULL, 'GLOBAL', 'configuration.label.support.mail', NULL, '', '44d444ea-7d0b-4de7-be16-6b9b9d54e44f', '', '^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$'),
(42, 'GENERAL', 9, NULL, 2, 'supportPhone', 'STRING', NULL, 'GLOBAL', 'configuration.label.support.phone', NULL, '', '86c25785-be69-4e69-abda-7cec26328273', '', ''),
(47, 'GENERAL', 1, NULL, 5, 'baseUrl', 'STRING', 'configuration.description.baseUrl', 'GLOBAL', 'configuration.label.baseUrl', NULL, '', '3e227a5c-53fc-483b-8c6d-ae0b863cdd2f', 'http://localhost:8080', ''),
(48, 'GENERAL', 7, NULL, 3, 'finishedEncounterMailaddressTimeWindowInMillis', 'LONG', 'configuration.description.finishedEncounterMailaddressTimeWindowInMillis', 'GLOBAL', 'configuration.label.encounter.finishedEncounterMailaddressTimeWindowInMillis', NULL, '', '504f512e-321d-49cd-a5b6-397ae1cc8438', '2592000000', ''),
(49, 'GENERAL', 10, NULL, 1, 'metadataExporterODMOID', 'STRING', 'configuration.description.metadataExporterODMOID', 'GLOBAL', 'configuration.label.metadataExporterODMOID', NULL, NULL, '325a0e07-76e8-427a-be4b-c7abcbc46bc0', '', ''),
(50, 'GENERAL', 10, NULL, 2, 'metadataExporterPDF', 'STRING', 'configuration.description.metadataExporterPDF', 'GLOBAL', 'configuration.label.metadataExporterPDF', NULL, NULL, '09b7de62-419e-4dfa-86d7-89cb19e5fd47', '', ''),
(51, 'GENERAL', 11, NULL, 1, 'exportInDirectory', 'BOOLEAN', 'configuration.description.exportPath', 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.exportFHIRInDirectory', NULL, '', '58b19b67-48d2-49c4-82d1-5b7b74bb1aa7', 'false', ''),
(52, 'GENERAL', 11, 51, 2, 'exportPath', 'LOCAL_PATH', 'configuration.description.exportPath', 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.exportFHIRPath', NULL, NULL, 'dd9e7969-545e-4480-8856-c38411c94985', '/var/lib/tomcat10/export/FHIR/', ''),
(53, 'GENERAL', 11, NULL, 4, 'exportViaCommunicationServer', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.exportFHIRViaCommunicationServer', NULL, '', '31e10f0d-1829-4258-ba4b-6a15da47189b', 'false', ''),
(54, 'GENERAL', 11, 53, 5, 'exportUrl', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.exportFHIRUrl', NULL, '', '33dcd659-ba20-4d0c-884d-98c0d830f5b3', '', ''),
(55, 'GENERAL', 6, NULL, 5, 'exportODMviaHL7', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.exportODMviaHL7', NULL, NULL, '0c42087d-e1c8-487d-a9b4-6685dbe2a236', 'false', ''),
(56, 'GENERAL', 6, 55, 6, 'ODMviaHL7Hostname', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.ODMviaHL7Hostname', NULL, NULL, '0fcd895d-8511-42b3-9228-9ee0903f7df5', '', ''),
(57, 'GENERAL', 6, 55, 7, 'ODMviaHL7Port', 'INTEGER', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.ODMviaHL7Port', NULL, NULL, '911d6f1e-f811-4c9b-9222-34d4c8351f1a', '', ''),
(58, 'GENERAL', 7, NULL, 3, 'finishedEncounterScheduledTimeWindowInMillis', 'LONG', 'configuration.description.finishedEncounterScheduledTimeWindowInMillis', 'GLOBAL', 'configuration.label.encounter.finishedEncounterScheduledTimeWindowInMillis', NULL, '', '45999eae-c086-4811-af60-e162c254425f', '7776000000', ''),
(59, 'GENERAL', 7, NULL, 4, 'incompleteEncounterScheduledTimeWindowInMillis', 'LONG', 'configuration.description.incompleteEncounterScheduledTimeWindowInMillis', 'GLOBAL', 'configuration.label.encounter.incompleteEncounterScheduledTimeWindowInMillis', NULL, '', '03629855-d5c8-41c5-885f-6ac603cd6268', '15552000000', ''),
(60, 'GENERAL', 6, 55, 8, 'ODMviaHL7SendingFacility', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.ODMviaHL7SendingFacility', NULL, NULL, 'aed9be70-2582-11e5-867f-0800200c9a66', 'TEST', NULL),
(61, 'GENERAL', 6, 55, 9, 'ODMviaHL7ReceivingApplication', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.ODMviaHL7ReceivingApplication', NULL, NULL, 'aed9be71-2582-11e5-867f-0800200c9a66', 'ORBIS', NULL),
(62, 'GENERAL', 6, 55, 10, 'ODMviaHL7ReceivingFacility', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.ODMviaHL7ReceivingFacility', NULL, NULL, 'aed9be72-2582-11e5-867f-0800200c9a66', 'CHRONO', NULL),
(63, 'GENERAL', 6, 55, 11, 'ODMviaHL7OBRFillerOrderNumber', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateODM', 'configuration.label.ODMviaHL7OBRFillerOrderNumber', NULL, NULL, 'aed9be73-2582-11e5-867f-0800200c9a66', 'MOPAT-1', NULL),
(64, 'GENERAL', 12, NULL, 1, 'exportInDirectory', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateREDCap', 'configuration.label.exportREDCapInDirectory', NULL, '', '504c1a22-cb35-411b-ab7b-520a8b2261d1', 'false', ''),
(65, 'GENERAL', 12, 64, 2, 'exportPath', 'LOCAL_PATH', 'configuration.description.exportPath', 'de.imi.mopat.io.impl.EncounterExporterTemplateREDCap', 'configuration.label.exportREDCapPath', NULL, NULL, 'a96b3b02-bc51-4198-b22a-4d344bb2708d', '/var/lib/tomcat10/export/REDCap/', ''),
(67, 'GENERAL', 12, NULL, 3, 'exportViaRest', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateREDCap', 'configuration.label.exportREDCapViaRest', NULL, '', '231be6ba-b788-45a9-8097-83e1e6971a05', 'false', ''),
(68, 'GENERAL', 12, 67, 4, 'exportUrl', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateREDCap', 'configuration.label.exportREDCapUrl', NULL, NULL, 'ff7e6cd6-89e1-43c1-969d-07e7236f38ec', '', ''),
(69, 'GENERAL', 12, 67, 5, 'apiToken', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateREDCap', 'configuration.label.exportREDCapApiToken', NULL, NULL, '0511fee5-7553-4cb2-8db8-19390719151e', '', ''),
(70, 'GENERAL', 5, NULL, 10, 'useTLS', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7UseTLS', NULL, NULL, '7d77b532-044e-4a09-b197-7cacef92a95e', 'false', NULL),
(71, 'GENERAL', 5, 70, 11, 'serverCert', 'FILE', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7ServerCertificatePath', NULL, NULL, 'ec9ce377-b215-4329-8ddd-cc05670de1b0', NULL, NULL),
(72, 'GENERAL', 5, NULL, 12, 'useClientAuth', 'BOOLEAN', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7UseClientAuth', NULL, NULL, '78b90206-d010-457d-857a-465f5e7abcee', 'false', NULL),
(73, 'GENERAL', 5, 72, 13, 'clientPKCSPath', 'FILE', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7ClientPKCSPath', NULL, NULL, '3ffa04e7-c604-4ceb-8604-3f2d55d56c80', NULL, NULL),
(74, 'GENERAL', 5, 72, 14, 'clientPKCSPassword', 'PASSWORD', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2', 'configuration.label.exportHL7ClientPKCSPassword', NULL, NULL, '7273b090-d149-45f2-a787-6af5838345b6', '', NULL),
(75, 'GENERAL', 1, NULL, 12, 'imageUploadPath', 'LOCAL_PATH', NULL, 'GLOBAL', 'configuration.label.imageUploadPath', NULL, NULL, 'fb1db996-4538-47e3-bd7b-7c5ce4f03264', '/var/lib/tomcat10/images', NULL),
(76, 'GENERAL', 10, NULL, 3, 'FHIRsystemURI', 'STRING', NULL, 'GLOBAL', 'configuration.label.FHIRsystemURI', NULL, NULL, 'cbd13c7f-a41b-42fa-9f87-7f78bc7e8a5d', 'https://mopat.uni-muenster.de/FHIR/', NULL),
(77, 'GENERAL', 1, NULL, 14, 'webappRootPath', 'STRING', NULL, 'GLOBAL', 'configuration.label.webappRootPath', NULL, NULL, 'b35db8b1-f143-4e5d-a423-37660b981319', '/var/lib/tomcat10/webapps/ROOT', NULL),
(78, 'GENERAL', 1, NULL, 15, 'enableGlobalPinAuth', 'BOOLEAN', NULL, 'GLOBAL', 'configuration.label.enableGlobalPinAuth', NULL, NULL, '5c4ca0df-fe5e-4582-8e9e-e290e1ed8efe', 'true', NULL),
(80, 'GENERAL', 13, NULL, 2, 'activateHisToggle', 'BOOLEAN', NULL, 'de.imi.mopat.controller.SurveyController', 'configuration.label.survey.activateHisToggle', NULL, '', '8c75cd85-fe2d-43d0-bf20-d1f4509f868b', 'false', ''),
(81, 'SELECT', 13, NULL, 3, 'patientDataRetrieverClass', 'SELECT', NULL, 'de.imi.mopat.helper.controller.ClinicPatientDataRetrieverFactoryBean', 'configuration.label.patientRetrieverClass', NULL, '', '7ff58798-b41f-42a2-b170-b2350fdeb70e', 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever', ''),
(82, 'GENERAL', 13, NULL, 4, 'HL7v22PatientInformationRetrieverHostname', 'STRING', NULL, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever', 'configuration.label.HL7v22PatientInformationRetrieverHostname', NULL, '', '82957ee9-2409-4978-84af-c3cf0edb9293', '', ''),
(83, 'GENERAL', 13, NULL, 5, 'HL7v22PatientInformationRetrieverPort', 'INTEGER', NULL, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever', 'configuration.label.HL7v22PatientInformationRetrieverPort', NULL, '', 'b6410c08-fc15-4f67-9601-9ccee136898d', '', ''),
(84, 'GENERAL', 14, NULL, 7, 'pseudonymizationServiceUrl', 'STRING', NULL, 'de.imi.mopat.controller.PseudonymizationController', 'configuration.label.pseudonymizationService.path', NULL, '', '59ef4f9c-b298-44e0-b9b4-d372e9f8a066', '', ''),
(85, 'GENERAL', 14, NULL, 8, 'pseudonymizationServiceApiKey', 'STRING', NULL, 'de.imi.mopat.controller.PseudonymizationController', 'configuration.label.pseudonymizationServiceApiKey', NULL, '', '75694313-f767-4107-b26f-6f5406484bb8', '', '');


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

--
-- Tabellenstruktur für Tabelle `Configuration_Group`
--
CREATE TABLE IF NOT EXISTS `configuration_group` (
`id` bigint(20) AUTO_INCREMENT NOT NULL,
`uuid` varchar(255) DEFAULT NULL,
`position` bigint(20) NOT NULL,
`name` varchar(255) DEFAULT NULL,
`label_message_code` varchar(255) NOT NULL,
`repeating` tinyint(1) NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8,
  AUTO_INCREMENT=10;

--
-- Daten für die Tabelle `Configuration_Group`
--

INSERT INTO `configuration_group` (`id`, `uuid`, `position`, `name`, `label_message_code`, `repeating`) VALUES
(1, '2ec9135e-f690-44ed-8984-d9165aa35a42', 1, 'name', 'configurationGroup.label.general', 0),
(2, 'b17f4076-acfc-4000-a9cd-ffaa245674d2', 2, 'name', 'configurationGroup.label.activeDirectoryAuthentication', 0),
(4, '8febaeec-8319-4a42-8560-4fa102004df0', 6, 'name', 'configurationGroup.label.ORBIS', 1),
(5, '8721ce9d-4e48-4b8e-98a3-7dd55c9314f2', 7, 'name', 'configurationGroup.label.HLSeven', 1),
(6, '0f1ce478-0302-42b8-9cb0-902821ebcdf4', 8, 'name', 'configurationGroup.label.ODM', 1),
(7, '3f3e368b-b2f8-44a5-ab30-92c49fb7d1a9', 11, 'name', 'configurationGroup.label.encounter', 0),
(8, '19b19f2f-8373-4ff0-beba-0a0c64b7ee86', 12, 'name', 'configurationGroup.label.mail', 0),
(9, 'f2bca53d-b533-41d5-8acb-52e011e6332e', 13, 'name', 'configurationGroup.label.support', 0),
(10, '95ec39c7-98ae-4f7d-b3e5-57416d01a2b0', 5, 'name', 'configurationGroup.label.metadataExporter', 0),
(11, '9a66d5da-023b-4248-a0d5-96c408c3fa34', 9, 'name', 'configurationGroup.label.FHIR', 1),
(12, 'c8f38a0d-f6e9-4a70-a34f-d6db8ce58479', 10, 'name', 'configurationGroup.label.REDCap', 1),
(13, '99f1e2d6-a7b9-4c12-8cbf-0e38dec7a8e8', 3, 'patient lookup 1', 'configurationGroup.label.usePatientLookUp', 1),
(14, '897e23cf-4155-4e82-b56f-a58a9aaef431', 4, 'pseudonymization 1', 'configurationGroup.label.pseudonymization', 1);


--
-- Constraints für die Tabelle `configuration`
--
ALTER TABLE `configuration`
	ADD CONSTRAINT `fk_configuration_group_id` FOREIGN KEY (`configuration_group_id`) REFERENCES `configuration_group` (`id`) ON DELETE CASCADE,
	ADD CONSTRAINT `fk_configuration_parent` FOREIGN KEY (`parent`) REFERENCES `configuration` (`id`) ON DELETE CASCADE;

--
-- Tabellenstruktur für Tabelle `SelectConfiguration_OPTIONS`
--

CREATE TABLE IF NOT EXISTS `SelectConfiguration_OPTIONS` (
  `SelectConfiguration_id` bigint(20) DEFAULT NULL,
  `options` varchar(255) NOT NULL,
  KEY `SelectConfiguration_OPTIONS_SelectConfiguration_id` (`SelectConfiguration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Daten für Tabelle `SelectConfiguration_OPTIONS`
--

INSERT INTO `SelectConfiguration_OPTIONS` (`SelectConfiguration_id`, `options`) VALUES
(1, 'de_DE'),
(1, 'en_GB'),
(3, 'number'),
(3, 'text'),
(81, 'de.imi.mopat.helper.controller.RandomPatientDataRetrieverImpl'),
(81, 'de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever'),
(81, 'de.imi.mopat.helper.controller.DummyPatientDataRetrieverImpl'),
(38, 'de_DE'),
(38, 'en_GB');

CREATE TABLE IF NOT EXISTS `SelectClinicConfiguration_OPTIONS` (
  `SelectClinicConfiguration_id` bigint(20) DEFAULT NULL,
  `options` varchar(255) NOT NULL,
  KEY `SelectClinicConfiguration_OPTIONS_SelectClinicConfiguration_id` (`SelectClinicConfiguration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- moPat.clinic_configuration_group_mapping definition

CREATE TABLE IF NOT EXISTS `clinic_configuration_group_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `clinic_configuration_mapping_id` bigint DEFAULT NULL,
  `configuration_group_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb3;

ALTER TABLE `clinic_configuration_group_mapping` ADD CONSTRAINT `clinic_configuration_group_mapping_fk1` FOREIGN KEY (`clinic_configuration_mapping_id`) REFERENCES `clinic_configuration_mapping` (`id`);
ALTER TABLE `clinic_configuration_group_mapping` ADD CONSTRAINT `clinic_configuration_group_mapping_fk2` FOREIGN KEY (`configuration_group_id`) REFERENCES `configuration_group` (`id`);


CREATE TABLE IF NOT EXISTS `operator` (
  `id` bigint(20) NOT NULL,
  `operator_type` varchar(31) NOT NULL,
  `display_sign` varchar(255) DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `operator` (`id`, `operator_type`, `display_sign`, `uuid`) VALUES
('1', 'Plus', '+', '81d2abc0-13f9-11e5-b939-0800200c9a66'),
('2', 'Minus', '-', 'a400abc3-13f9-11e5-b939-0800200c9a66'),
('3', 'Multiply', '*', '9560c1b3-b708-496d-b88c-a8ed0fd91bbe'),
('4', 'Divide', '/', 'bfc59730-61c4-11e5-a837-0800200c9a66'),
('5', 'ValueOfQuestion', 'valueOf', 'e3f3d368-13f9-11e5-b939-0800200c9a66'),
('6', 'Value', 'value', 'fd54ce40-6692-11e5-a837-0800200c9a66'),
('7', 'Sum', 'sum', 'c46c2380-516b-11e5-b970-0800200c9a66'),
('8','Greater','>','05ae06a2-794d-4474-8183-548cea9f3bc4'),
('9','GreaterEquals','>=','dcfac7c0-7d27-4e18-b25d-f5a605e219e1'),
('10','Less','<','178aef8a-d6df-4a5f-b884-e042a7d03f3d'),
('11','LessEquals','<=','23bd7357-f6d0-47fd-ab29-942573d6876e'),
('12','Equals','==','5c1c202c-2938-454b-926e-415cb816ac44'),
('13','Different','!=','54e25316-ab53-466e-bb9d-ab7141f0b767'),
('14','Counter','counter','3e12ae36-a128-4247-a38b-2a975892e5bf'),
('15','Average','average','52b7aed6-19aa-4670-bb64-75034f754eae'),
('16','ValueOfScore','valueOfScore','ea04e07d-94a5-4d76-b0e1-af6c26f3e316'),
('17', 'Maximum', 'maximum', '62bbea30-7105-4a1a-a91d-90dff7596c65'),
('18', 'Minimum', 'minimum', '4f6898e7-b711-4c3a-b678-71200959f8f9');



--
-- Constraints der Tabelle `SelectConfiguration_OPTIONS`
--
ALTER TABLE `SelectConfiguration_OPTIONS`
  ADD CONSTRAINT `SelectConfiguration_OPTIONS_SelectConfiguration_id` FOREIGN KEY (`SelectConfiguration_id`) REFERENCES `configuration` (`id`);

-- Constraints der Tabelle `SelectClinicConfiguration_OPTIONS`

ALTER TABLE `SelectClinicConfiguration_OPTIONS`
  ADD CONSTRAINT `SelectClinicConfiguration_OPTIONS_SelectClinicConfiguration_id` FOREIGN KEY (`SelectClinicConfiguration_id`) REFERENCES `clinic_configuration` (`id`);

--
-- Datenbank: `moPat_user`
--
CREATE DATABASE `moPat_user` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `moPat_user`;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `acl_class`
--

CREATE TABLE IF NOT EXISTS `acl_class` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_uk_2` (`class`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Daten für Tabelle `acl_class`
--

INSERT INTO `acl_class` (`id`, `class`) VALUES
(1, 'de.imi.mopat.model.Clinic'),
(2, 'de.imi.mopat.model.Bundle');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `acl_entry`
--

CREATE TABLE IF NOT EXISTS `acl_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `acl_object_identity` bigint(20) NOT NULL,
  `ace_order` int(11) NOT NULL,
  `sid` bigint(20) NOT NULL,
  `mask` int(11) NOT NULL,
  `granting` tinyint(1) NOT NULL,
  `audit_success` tinyint(1) NOT NULL,
  `audit_failure` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_fk_5` (`sid`),
  KEY `foreign_fk_4` (`acl_object_identity`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `acl_object_identity`
--

CREATE TABLE IF NOT EXISTS `acl_object_identity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `object_id_class` bigint(20) NOT NULL,
  `object_id_identity` bigint(20) NOT NULL,
  `parent_object` bigint(20) DEFAULT NULL,
  `owner_sid` bigint(20) DEFAULT NULL,
  `entries_inheriting` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_uk_3` (`object_id_class`,`object_id_identity`),
  KEY `foreign_fk_1` (`parent_object`),
  KEY `foreign_fk_3` (`owner_sid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `acl_sid`
--

CREATE TABLE IF NOT EXISTS `acl_sid` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `principal` tinyint(1) NOT NULL,
  `sid` varchar(100) NOT NULL,
  `password` varchar(255),
  `salt` varchar(255),
  `uuid` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `firstname` varchar(255) NOT NULL,
  `lastname` varchar(255) NOT NULL,
  `is_enabled` tinyint(1) NOT NULL,
  `last_selected_clinic_id` BIGINT NULL,
  `use_pin` tinyint(1),
  `pin` varchar(255),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=7 ;

--
-- Daten für Tabelle `acl_sid`
--

INSERT INTO `acl_sid` (`id`, `principal`, `sid`, `password`, `salt`, `uuid`, `email`, `firstname`, `lastname`, `is_enabled`, `use_pin`, `pin`) VALUES
(1,1,'admin','$2a$10$Ooo1W1Ym7aa7iECp/KbRSO6sEKf7RaIr5JZt8zi7STBw9fdy7u5Di',NULL,'10ab9c09-6e0b-4e2d-b8f8-a93ec452a44e','admin@mopat.com','admin','admin',1, 0, NULL),
(2,1,'user','$2a$10$AkrCrkk3WPvje.Pa8yRwH.dvLjWpHBtcE/.MXOQWturYRf0BGfDia',NULL,'94ebebf1-03b6-4d10-8156-c117514caff3','user@mopat.com','user','user',1, 0, NULL);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `sequence`
--

CREATE TABLE IF NOT EXISTS `SEQUENCE` (
  `SEQ_NAME` varchar(50) NOT NULL,
  `SEQ_COUNT` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`SEQ_NAME`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Daten für Tabelle `sequence`
--

INSERT INTO `SEQUENCE` (`SEQ_NAME`, `SEQ_COUNT`) VALUES
('SEQ_GEN', 400);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `userroles`
--

CREATE TABLE IF NOT EXISTS `userroles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user` bigint(20) NOT NULL,
  `authority` varchar(255) NOT NULL,
  `uuid` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user` (`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

INSERT INTO `userroles` (`id`, `user`, `authority`,`uuid`) VALUES
(1, 1,'ROLE_ADMIN','9d84a79b-aa42-40c5-93d7-46713f9138de'),
(2, 2,'ROLE_USER','e1e33ac2-3f4a-467d-a213-8521e014ac87');

--
-- Tabellenstruktur für Tabelle `invitation`
--

CREATE TABLE IF NOT EXISTS `invitation` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `expirationDate` date DEFAULT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `lastname` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  `owner` bigint(20) DEFAULT NULL,
  `personal_text` text,
  `locale` text,
  PRIMARY KEY (`id`),
  KEY `FK_invitation_owner` (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Tabellenstruktur für Tabelle `forgot_password_token`
--

CREATE TABLE IF NOT EXISTS `forgot_password_token` (
  `id` bigint(20) NOT NULL,
  `expirationDate` date DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  `user` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_forgot_password_token_user` (`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Tabellenstruktur für Tabelle `invitation_acl_object_identity`
--

CREATE TABLE IF NOT EXISTS `invitation_acl_object_identity` (
  `acl_object_identity_id` bigint(20) NOT NULL,
  `invitation_id` bigint(20) NOT NULL,
  PRIMARY KEY (`acl_object_identity_id`,`invitation_id`),
  KEY `FK_invitation_acl_object_identity_invitation_id` (`invitation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table for the quick pin login
--
CREATE TABLE IF NOT EXISTS `pin_authorization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sid` bigint(20) NOT NULL,
  `created_on` datetime DEFAULT CURDATE() NOT NULL,
  `remaining_tries` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_fk_6` (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `forgot_password_token`
--
ALTER TABLE `forgot_password_token`
  ADD CONSTRAINT `FK_forgot_password_token_user` FOREIGN KEY (`user`) REFERENCES `acl_sid` (`id`);
--
-- Constraints der Tabelle `acl_entry`
--
ALTER TABLE `acl_entry`
  ADD CONSTRAINT `foreign_fk_4` FOREIGN KEY (`acl_object_identity`) REFERENCES `acl_object_identity` (`id`);
ALTER TABLE `acl_entry`
  ADD CONSTRAINT `foreign_fk_5` FOREIGN KEY (`sid`) REFERENCES `acl_sid` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints der Tabelle `acl_object_identity`
--
ALTER TABLE `acl_object_identity`
  ADD CONSTRAINT `foreign_fk_1` FOREIGN KEY (`parent_object`) REFERENCES `acl_object_identity` (`id`),
  ADD CONSTRAINT `foreign_fk_2` FOREIGN KEY (`object_id_class`) REFERENCES `acl_class` (`id`),
  ADD CONSTRAINT `foreign_fk_3` FOREIGN KEY (`owner_sid`) REFERENCES `acl_sid` (`id`);

--
-- Constraints der Tabelle `pin_authorization`
--
ALTER TABLE `pin_authorization`
  ADD CONSTRAINT `foreign_fk_6` FOREIGN KEY (`sid`) REFERENCES `acl_sid` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints der Tabelle `userroles`
--
ALTER TABLE `userroles`
  ADD CONSTRAINT `userroles_ibfk_2` FOREIGN KEY (`user`) REFERENCES `acl_sid` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints der Tabelle `invitation`
--
ALTER TABLE `invitation`
  ADD CONSTRAINT `FK_invitation_owner` FOREIGN KEY (`owner`) REFERENCES `acl_sid` (`id`);

--
-- Constraints der Tabelle `invitation_acl_object_identity`
--
ALTER TABLE `invitation_acl_object_identity`
  ADD CONSTRAINT `FK_invitation_acl_object_identity_invitation_id` FOREIGN KEY (`invitation_id`) REFERENCES `invitation` (`id`),
  ADD CONSTRAINT `invitationaclobject_identityacl_object_identity_id` FOREIGN KEY (`acl_object_identity_id`) REFERENCES `acl_object_identity` (`id`);

--
-- Database: `moPat_audit`
--
CREATE DATABASE `moPat_audit` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `moPat_audit`;

-- --------------------------------------------------------

--
-- Table structure for table `audit_entry`
--

CREATE TABLE IF NOT EXISTS `audit_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `module` varchar(255) DEFAULT NULL,
  `action` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `sender_receiver` text,
  `log_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=0 ;

-- --------------------------------------------------------

--
-- Table structure for table `sequence`
--

CREATE TABLE IF NOT EXISTS `SEQUENCE` (
  `SEQ_NAME` varchar(50) NOT NULL,
  `SEQ_COUNT` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`SEQ_NAME`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Daten für Tabelle `sequence`
--

INSERT INTO `SEQUENCE` (`SEQ_NAME`, `SEQ_COUNT`) VALUES
('SEQ_GEN', 400);

--
-- User permissions for mopat databases
--

GRANT SELECT , INSERT , UPDATE , DELETE , CREATE , DROP , INDEX , ALTER ON `moPat` . * TO 'mopat'@'%';
GRANT SELECT , INSERT , UPDATE , DELETE ON `moPat\_user` . * TO 'mopat'@'%';
GRANT INSERT ON `moPat\_audit`.* TO 'mopat'@'%';
GRANT SELECT, UPDATE (SEQ_COUNT) ON `moPat_audit`.`SEQUENCE` TO 'mopat'@'%';
