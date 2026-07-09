USE
`moPat`;

ALTER TABLE moPat.configuration MODIFY COLUMN value TEXT DEFAULT NULL NULL;

UPDATE export_template
SET export_template_type = REPLACE(export_template_type, 'FHIR', 'FHIR_DSTU3')
WHERE export_template_type LIKE 'FHIR';

UPDATE moPat.export_template
SET export_template_type = 'HL7v2'
WHERE export_template_type = 'ORBIS';

SELECT id
INTO @ORBIS_CONFIG_GROUP
FROM moPat.configuration_group
WHERE label_message_code = 'configurationGroup.label.ORBIS' LIMIT 1;

SELECT id
INTO @HL7v2_CONFIG_GROUP
FROM moPat.configuration_group
WHERE label_message_code = 'configurationGroup.label.HLSeven' LIMIT 1;

UPDATE moPat.export_template
SET configuration_group = @HL7v2_CONFIG_GROUP
WHERE configuration_group = @ORBIS_CONFIG_GROUP;

DELETE
FROM moPat.configuration
WHERE label_message_code = 'configuration.label.exportOrbisPath';
DELETE
FROM moPat.configuration_group
WHERE label_message_code = 'configurationGroup.label.ORBIS';

ALTER TABLE question
    ADD COLUMN is_just_info TINYINT(1) NOT NULL DEFAULT 0;

SELECT MAX(id)
INTO @largest_id
FROM moPat.configuration;

-- Configuration may change with changing groups added. Please make sure this fits to your server config.
-- Adjust configuration_group_id to fit to your FHIR config group. Copy the values for all available FHIR configs on the server
-- You can find the ids with this query:
-- SELECT id FROM moPat.configuration_group WHERE configuration_group.label_message_code LIKE '%FHIR';
-- On a server with no additional config groups, the id should be 11
INSERT INTO moPat.configuration (`id`, `type`, `configuration_group_id`, `parent`, `position`,
                                 `attribute`, `configuration_type`, `description_message_code`,
                                 `class`, `label_message_code`, `test_method`, `update_method`,
                                 `uuid`, `value`, `pattern`)
VALUES (@largest_id + 1, 'GENERAL', 1, NULL, 19, 'enableEncounterTemplateDownload', 'BOOLEAN',
        'configuration.label.enableEncounterTemplateDownload', 'GLOBAL',
        'configuration.label.enableEncounterTemplateDownload', NULL, NULL,
        'd8219938-021c-49b9-838e-1880b04e7e2e', false, NULL);
