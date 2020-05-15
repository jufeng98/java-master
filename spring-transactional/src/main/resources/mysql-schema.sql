CREATE TABLE `country`
(
    `country_id`  smallint(5) unsigned NOT NULL AUTO_INCREMENT,
    `country`     varchar(50)          NOT NULL,
    `last_update` timestamp            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`country_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `city`
(
    `city_id`     smallint(5) unsigned NOT NULL AUTO_INCREMENT,
    `city`        varchar(50)          NOT NULL,
    `country_id`  smallint(5) unsigned NOT NULL,
    `last_update` timestamp            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`city_id`),
    KEY `idx_fk_country_id` (`country_id`),
    CONSTRAINT `fk_city_country` FOREIGN KEY (`country_id`) REFERENCES `country` (`country_id`) ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `category`
(
    `category_id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
    `name`        varchar(25)         NOT NULL,
    `last_update` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;



