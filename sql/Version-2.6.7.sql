-- CRM-643 START

ALTER TABLE `carbon`.`proposal` ADD COLUMN `validity_date` DATETIME NULL DEFAULT NULL AFTER `create_date`;

UPDATE `carbon`.`proposal` set validity_date = DATE_ADD(IFNULL(create_date, NOW()), INTERVAL 10 DAY);

ALTER TABLE `carbon`.`proposal` CHANGE COLUMN `validity_date` `validity_date` DATETIME NOT NULL ;

-- CRM-643 END