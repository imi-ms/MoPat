USE `moPat`;

SELECT MAX(id)
INTO @largest_id
FROM moPat.configuration;

-- Configuration may change with changing groups added. Please make sure this fits to your server config.
INSERT INTO moPat.configuration (`id`, `type`, `configuration_group_id`, `parent`, `position`,
                                 `attribute`, `configuration_type`, `description_message_code`,
                                 `class`, `label_message_code`, `test_method`, `update_method`,
                                 `uuid`, `value`, `pattern`)
VALUES (@largest_id + 1, 'GENERAL', 1, NULL, 17, 'enableEncounterTemplateDownload', 'BOOLEAN',
        'configuration.label.enableEncounterTemplateDownload', 'GLOBAL',
        'configuration.label.enableEncounterTemplateDownload', NULL, NULL,
        'd8219938-021c-49b9-838e-1880b04e7e2e', false, NULL);
