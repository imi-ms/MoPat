USE `moPat`;

SELECT MAX(id)
INTO @largest_id
FROM moPat.configuration;


-- Determine the highest position in configuration group 1
SELECT COALESCE(MAX(position), 0)
INTO @max_position_group_1
FROM `moPat`.`configuration`
WHERE `configuration_group_id` = 1;


-- Configuration group 1 is used here.
-- If your target server uses a different configuration group for these settings,
-- please adjust configuration_group_id accordingly.

-- Inserts:
-- 1) enableApiTokenAccess
-- 2) apiKey as child of enableApiTokenAccess
INSERT INTO moPat.configuration (`id`, `type`, `configuration_group_id`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`, `pattern`) VALUES
    (@largest_id + 1, 'GENERAL', 1, NULL, @max_position_group_1 +1, 'enableApiTokenAccess', 'BOOLEAN', 'configuration.description.enableApiTokenAccess', 'GLOBAL', 'configuration.label.enableApiTokenAccess', NULL, NULL, '25fd8f5e-2e90-4cd1-8a30-d84c88889c5c', false, NULL),
    (@largest_id + 2, 'GENERAL', 1, @largest_id + 1,  @max_position_group_1 +2, 'apiKey', 'STRING', 'configuration.description.apiKey', 'GLOBAL', 'configuration.label.apiKey', NULL, NULL, '9b48f864-8cb7-4704-82e2-12045755e2aa', '', NULL);
