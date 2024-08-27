CREATE TABLE mopat.predefined_slider_icon (
	id BIGINT auto_increment NOT NULL,
	icon_name VARCHAR(255) NOT NULL,
	CONSTRAINT predefined_slider_icon_pk PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb3
COLLATE=utf8mb3_general_ci;

INSERT INTO predefined_slider_icons (icon_name) VALUES
('bi-emoji-angry'),
('bi-emoji-angry-fill'),
('bi-emoji-dizzy'),
('bi-emoji-dizzy-fill'),
('bi-emoji-expressionless'),
('bi-emoji-expressionless-fill'),
('bi-emoji-frown'),
('bi-emoji-frown-fill'),
('bi-emoji-heart-eyes'),
('bi-emoji-heart-eyes-fill'),
('bi-emoji-laughing'),
('bi-emoji-laughing-fill'),
('bi-emoji-neutral'),
('bi-emoji-neutral-fill'),
('bi-emoji-smile'),
('bi-emoji-smile-fill'),
('bi-emoji-smile-upside-down'),
('bi-emoji-smile-upside-down-fill'),
('bi-emoji-sunglasses'),
('bi-emoji-sunglasses-fill'),
('bi-emoji-wink'),
('bi-emoji-wink-fill'),
('bi-hand-thumbs-down'),
('bi-hand-thumbs-down-fill'),
('bi-hand-thumbs-up'),
('bi-hand-thumbs-up-fill'),
('bi-brightness-high-fill'),
('bi-brightness-low-fill'),
('bi-dash-square'),
('bi-dash-lg'),
('bi-plus-square'),
('bi-plus-lg'),
('bi-x-square'),
('bi-x-lg'),
('bi-droplet'),
('bi-droplet-half'),
('bi-droplet-fill'),
('bi-ear'),
('bi-ear-fill'),
('bi-eye'),
('bi-eye-fill'),
('bi-eye-slash'),
('bi-eye-slash-fill'),
('bi-graph-down-arrow'),
('bi-graph-up-arrow'),
('bi-heart-pulse'),
('bi-heart-pulse-fill'),
('bi-lightning-charge'),
('bi-lightning-charge-fill'),
('bi-lungs'),
('bi-lungs-fill'),
('bi-0-square'),
('bi-1-square'),
('bi-2-square'),
('bi-3-square'),
('bi-4-square'),
('bi-5-square'),
('bi-6-square'),
('bi-7-square'),
('bi-8-square'),
('bi-9-square');

CREATE TABLE mopat.user_slider_icon (
	id BIGINT auto_increment NOT NULL,
	icon_path VARCHAR(255) NOT NULL,
	CONSTRAINT user_slider_icon_pk PRIMARY KEY (id)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb3
COLLATE=utf8mb3_general_ci;


CREATE TABLE `slider_icon_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `number_of_icons` int(11) DEFAULT NULL,
  `config_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slider_icon_config_name_unique` (`config_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3057 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;


CREATE TABLE mopat.slider_icon_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    icon_position INTEGER,
    slider_icon_config_id BIGINT,
    predefined_slider_icon_id BIGINT,
    user_slider_icon_id BIGINT,
    FOREIGN KEY (slider_icon_config_id) REFERENCES slider_icon_config(id),
    FOREIGN KEY (predefined_slider_icon_id) REFERENCES predefined_slider_icon(id),
    FOREIGN KEY (user_slider_icon_id) REFERENCES user_slider_icon(id)
);

ALTER TABLE mopat.slider_icons CHANGE icon icon_id BIGINT DEFAULT NULL NULL;

ALTER TABLE answer
ADD COLUMN slider_icon_config_id BIGINT;

ALTER TABLE answer
ADD CONSTRAINT fk_slider_icon_config
FOREIGN KEY (slider_icon_config_id)
REFERENCES slider_icon_config(id)
ON DELETE SET NULL;