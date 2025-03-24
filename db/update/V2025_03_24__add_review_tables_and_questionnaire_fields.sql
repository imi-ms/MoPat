USE `moPat`;

-- Add new column approval_status to questionnaire table
ALTER TABLE questionnaire ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- Set approval_status to APPROVED for existing questionnaires
UPDATE questionnaire SET approval_status = 'APPROVED';

-- Add new column created_by to questionnaire table
ALTER TABLE `questionnaire` ADD COLUMN `created_by` BIGINT(20) NOT NULL;

-- Set created_by to changed_by for existing questionnaires (assumes changed_by is the closest reference)
UPDATE `questionnaire` SET `created_by` = `changed_by`;

-- Add column 'main_questionnaire_id' to 'questionnaire_version_group'
ALTER TABLE `questionnaire_version_group`
    ADD COLUMN `main_questionnaire_id` BIGINT(20) NULL;

-- Set main_questionnaire_id to the questionnaire with the highest version in each group
UPDATE questionnaire_version_group qvg
SET main_questionnaire_id = (
    SELECT q.id FROM questionnaire q
    WHERE q.version_group_id = qvg.id
    ORDER BY q.version DESC LIMIT 1
);

-- Create 'review' table
CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    `questionnaire_id` BIGINT(20) NOT NULL,
    `status` NOT NULL DEFAULT 'PENDING',
    `editor_id` BIGINT(20) NOT NULL,
    `reviewer_id` BIGINT(20) DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`questionnaire_id`) REFERENCES `questionnaire` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Create 'review_message' table
CREATE TABLE IF NOT EXISTS `review_message` (
    `id` BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    `review_id` BIGINT(20) NOT NULL,
    `sender_id` BIGINT(20) NOT NULL,
    `receiver_id` BIGINT(20) NOT NULL,
    `message` TEXT NOT NULL,
    `sent_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`review_id`) REFERENCES `review` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;