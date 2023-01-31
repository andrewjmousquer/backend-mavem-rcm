-- CRM-733 START
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (400, 'BLACK','VEHICLE_COLOR' , 'PRETO','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (401, 'GRAY','VEHICLE_COLOR' , 'CINZA','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (402, 'WHITE','VEHICLE_COLOR' , 'BRANCO','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (403, 'SILVER','VEHICLE_COLOR' , 'PRATA','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (404, 'GOLD','VEHICLE_COLOR' , 'DOURADO','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (405, 'YELLOW','VEHICLE_COLOR' , 'AMARELO','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (406, 'BLUE','VEHICLE_COLOR' , 'AZUL','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (407, 'BEIGE','VEHICLE_COLOR' , 'BEGE','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (408, 'ORANGE','VEHICLE_COLOR' , 'LARANJA','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (409, 'BROWN','VEHICLE_COLOR' , 'MARROM','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (410, 'PURPLE','VEHICLE_COLOR' , 'ROXO','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (411, 'GREEN','VEHICLE_COLOR' , 'VERDE','IDENTIFICA A COR DO VEÍCULO');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (412, 'RED','VEHICLE_COLOR' , 'VERMELHO','IDENTIFICA A COR DO VEÍCULO');

ALTER TABLE `carbon`.`vehicle` 
ADD COLUMN `color_cla` INT(11) NULL DEFAULT NULL AFTER `mdl_id`,
ADD INDEX `fk_vehicle_color_idx` (`color_cla` ASC) VISIBLE;

ALTER TABLE `carbon`.`vehicle` 
ADD CONSTRAINT `fk_vehicle_color`
  FOREIGN KEY (`color_cla`)
  REFERENCES `carbon`.`classifier` (`cla_id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
-- CRM-733 END
  
-- CRM-742 START

ALTER TABLE `carbon`.`model` ADD COLUMN `cod_fipe` VARCHAR(15) NULL AFTER `type_cla_id`;

-- CRM-742 END


-- CRM-740 START
ALTER TABLE `carbon`.`proposal` 
DROP COLUMN `commercial_contact`;

ALTER TABLE `carbon`.`proposal` ADD COLUMN `commercial_contact_name` VARCHAR(150) NULL AFTER `document_contact_phone`;
ALTER TABLE `carbon`.`proposal` ADD COLUMN `commercial_contact_email` VARCHAR(100) NULL AFTER `commercial_contact_name`;
ALTER TABLE `carbon`.`proposal` ADD COLUMN `commercial_contact_phone` VARCHAR(45) NULL AFTER `commercial_contact_email`;
-- CRM-740 END


-- CRM-719 START

ALTER TABLE `carbon`.`proposal_payment` MODIFY COLUMN `pym_id` int NULL;
ALTER TABLE `carbon`.`proposal_payment` MODIFY COLUMN `pyr_id` int NULL;

-- CRM-719  END
