USE `moPat`;

INSERT INTO moPat.configuration (`id`, `type`, `configuration_group_id`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`, `pattern`) VALUES
(@largest_id + 1, 'GENERAL', 7, NULL, 5, 'fileDeletionTimeWindowInMillis', 'LONG', 'configuration.description.fileDeletionTimeWindowInMillis', 'GLOBAL', 'configuration.label.encounter.fileDeletionTimeWindowInMillis', NULL, NULL, 'd6a762c1-ed44-49e8-9831-2d2ef6505ce6', '2592000000', NULL);