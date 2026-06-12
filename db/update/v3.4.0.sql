USE `moPat`;

SELECT MAX(id)
INTO @largest_id
FROM moPat.configuration;

-- Configuration may change with changing groups added. Please make sure this fits to your server config.
-- Adjust configuration_group_id to fit to your FHIR config group. Copy the values for all available FHIR configs on the server
-- You can find the ids with this query:
-- SELECT id FROM moPat.configuration_group WHERE configuration_group.label_message_code LIKE '%FHIR';
-- On a server with no additional config groups, the id should be 11
INSERT INTO moPat.configuration (`id`, `type`, `configuration_group_id`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`, `pattern`) VALUES
    (@largest_id + 1, 'GENERAL', 1, NULL, 17, 'enableApiTokenAccess', 'BOOLEAN', 'configuration.label.enableApiTokenAccess', 'GLOBAL', 'configuration.label.enableApiTokenAccess', NULL, NULL, '25fd8f5e-2e90-4cd1-8a30-d84c88889c5c', false, NULL),
    (@largest_id + 2, 'GENERAL', 1, 17, 18, 'apiKey', 'STRING', 'configuration.label.apiKey', 'GLOBAL', 'configuration.label.apiKey', NULL, NULL, '9b48f864-8cb7-4704-82e2-12045755e2aa', '', NULL);
