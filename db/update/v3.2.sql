USE `moPat_user`;

CREATE TABLE IF NOT EXISTS `pin_authorization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sid` bigint(20) NOT NULL,
  `created_on` datetime DEFAULT CURDATE() NOT NULL,
  `remaining_tries` INT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_fk_6` (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

ALTER TABLE `pin_authorization`
  ADD CONSTRAINT `foreign_fk_6` FOREIGN KEY (`sid`) REFERENCES `acl_sid` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE moPat_user.acl_sid ADD use_pin TINYINT(1) DEFAULT FALSE NULL;
ALTER TABLE moPat_user.acl_sid ADD pin varchar(255) DEFAULT NULL NULL;#

USE `moPat`;

INSERT INTO `configuration` (`id`, `type`, `configuration_group_id`, `parent`, `position`, `attribute`, `configuration_type`, `description_message_code`, `class`, `label_message_code`, `test_method`, `update_method`, `uuid`, `value`, `pattern`) VALUES
(78, 'GENERAL', 1, NULL, 15, 'enableGlobalPinAuth', 'BOOLEAN', NULL, 'GLOBAL', 'configuration.label.enableGlobalPinAuth', NULL, NULL, '5c4ca0df-fe5e-4582-8e9e-e290e1ed8efe', 'true', NULL);