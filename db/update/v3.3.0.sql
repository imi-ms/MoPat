USE `moPat`;

ALTER TABLE moPat.configuration MODIFY COLUMN value TEXT DEFAULT NULL NULL;

UPDATE export_template
SET export_template_type = REPLACE(export_template_type, 'FHIR', 'FHIR_DSTU3')
WHERE export_template_type LIKE 'FHIR';

UPDATE moPat.export_template SET export_template_type = 'HL7v2' WHERE export_template_type = 'ORBIS';

SELECT id
INTO @ORBIS_CONFIG_GROUP
FROM moPat.configuration_group
WHERE label_message_code = 'configurationGroup.label.ORBIS'
LIMIT 1;

SELECT id
INTO @HL7v2_CONFIG_GROUP
FROM moPat.configuration_group
WHERE label_message_code = 'configurationGroup.label.HLSeven'
LIMIT 1;

UPDATE moPat.export_template SET configuration_group = @HL7v2_CONFIG_GROUP WHERE configuration_group = @ORBIS_CONFIG_GROUP;

DELETE FROM moPat.configuration WHERE label_message_code = 'configuration.label.exportOrbisPath';
DELETE FROM moPat.configuration_group WHERE label_message_code = 'configurationGroup.label.ORBIS';

ALTER TABLE question ADD COLUMN is_just_info TINYINT(1) NOT NULL DEFAULT 0;

SELECT MAX(id)
INTO @largest_id
FROM moPat.configuration;

INSERT INTO moPat.configuration (`id`, `type`, `configuration_group_id`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`, `pattern`) VALUES
(@largest_id + 1, 'GENERAL', 1, NULL, 16, 'imprintText', 'RICH_TEXT', 'configuration.description.imprint', 'GLOBAL', 'configuration.label.imprint', NULL, NULL, 'ff890137-bfb5-4e3a-a2a0-b51b7ff6b088', 'Universität Münster<br>Schlossplatz 2, 48149 Münster<br>Telephone: +49 (251) 83-0<br>Fax: +49 (251) 83-3 20 90<br>E-mail: verwaltung@uni-muenster.de<br><br>The University of Münster is a statutory body and an institution of the Land of North Rhine- Westphalia. It is represented by the Rector, Professor Dr. Johannes Wessels.<br><br>Turnover tax identification number: DE 126118759<br><br>Edited in accordance with §5 TMG by:<br>Univ.-Prof. Dr. rer. nat. Dominik Heider<br>Institute of Medical Informatics<br>Albert-Schweizer-Campus 1, Building A11<br>48149 Münster, Germany<br>Telephone: +49 (251) 83-55262<br>E-Mail:&nbsp;<a href="mailto:imi@uni-muenster.de" style="color: rgb(13, 110, 253);">imi@uni-muenster.de</a>', NULL),
(@largest_id + 2, 'GENERAL', 11, NULL, 6, 'exportFHIRViaHL7v2', 'BOOLEAN', 'configuration.description.exportFHIRViaHL7v2', 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.exportFHIRViaHL7v2', NULL, NULL, '08f48ba4-790b-47fa-9236-81956785bfda', false, NULL),
(@largest_id + 3, 'GENERAL', 11, 87, 7, 'FHIRViaHL7v2Host', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.FHIRViaHL7v2Host', NULL, NULL, 'a32a5e14-6c7d-4922-ba31-4c1274c7717c', '', NULL),
(@largest_id + 4, 'GENERAL', 11, 87, 8, 'FHIRViaHL7v2Port', 'INTEGER', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.FHIRViaHL7v2Port', NULL, NULL, '2393b1de-39d3-49a3-a2a8-dc0ab3c196e3', NULL, NULL),
(@largest_id + 5, 'GENERAL', 11, 87, 9, 'FHIRViaHL7v2SendingFacility', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.ODMviaHL7SendingFacility', NULL, NULL, '87e35940-97e2-4c3e-ae6c-b35ea65717e3', '', NULL),
(@largest_id + 6, 'GENERAL', 11, 87, 10, 'FHIRViaHL7v2ReceivingApplication', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.ODMviaHL7ReceivingApplication', NULL, NULL, 'd500a3a1-c1ce-4091-ace9-4557481dba37', '', NULL),
(@largest_id + 7, 'GENERAL', 11, 87, 11, 'FHIRViaHL7v2ReceivingFacility', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.ODMviaHL7ReceivingFacility', NULL, NULL, '613cc529-396e-4a06-b785-158c15629d02', '', NULL),
(@largest_id + 8, 'GENERAL', 11, 87, 11, 'FHIRViaHL7v2OBRFillerOrderNumber', 'STRING', NULL, 'de.imi.mopat.io.impl.EncounterExporterTemplateFHIR', 'configuration.label.ODMviaHL7OBRFillerOrderNumber', NULL, NULL, 'e78a6310-b2b1-4ee7-aea0-378ab33c3f73', '', NULL);