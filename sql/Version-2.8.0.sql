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

  -- CRM-474 START 
CREATE TABLE IF NOT EXISTS `carbon`.`product_model_cost` (
  `pmc_id` INT NOT NULL AUTO_INCREMENT,
  `prm_id` INT NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `total_value` DECIMAL(13,2) NOT NULL,
  PRIMARY KEY (`pmc_id`),
  INDEX `fk_product_model_cost_to_product_model_idx` (`prm_id` ASC) VISIBLE,
  CONSTRAINT `fk_product_model_cost_to_product_model`
    FOREIGN KEY (`prm_id`)
    REFERENCES `carbon`.`product_model` (`prm_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB

INSERT INTO carbon.checkpoint (name, description) VALUES ('PRODUCT.MODEL.COST.CREATE', 'Criação de Custo por Produto e Modelo');

INSERT INTO carbon.menu (name, url, description, icon, type_cla) VALUES ('Custo por Modelo e Produto', 'product-model-cost-form', 'Cadastro de Custo por Modelo e Produto', 'fa fa-fw fa-dollar', 8);
-- CRM-474 END 

-- CRM-744 START
ALTER TABLE `carbon`.`proposal` 
ADD COLUMN `contract` TEXT NULL DEFAULT NULL AFTER `immediate_delivery`;
-- CRM-744 END

-- CRM-743 START

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('PROPOSAL_PAYMENT_CHECKIN_DAYS','3','Dias de pagamento para o evento Checkin');
INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('PROPOSAL_PAYMENT_CHECKOUT_DAYS','3','Dias de pagamento para o evento Checkout');
INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('PROPOSAL_PAYMENT_INVOICE_DAYS','3','Dias de pagamento para o evento autorização de faturamento');

ALTER TABLE `carbon`.`proposal_payment`
    ADD COLUMN `quantity_days` INT NULL AFTER `position`;

ALTER TABLE `carbon`.`proposal_payment`
    ADD COLUMN `carbon_billing` TINYINT NOT NULL DEFAULT '0' AFTER `quantity_days`;


-- CRM-743 END

-- CRM-473 START
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Custo por Modelo e Item', 'model-item-cost-form', 'Tela para cadastro de Custo de fabrição por modelo e item', 'FA FA-FW FA FA-USD', 8, null, 1);

CREATE TABLE IF NOT EXISTS `carbon`.`model_item_cost` (
  `mic_id` INT(11) NOT NULL AUTO_INCREMENT,
  `price` DECIMAL(13,2) NOT NULL DEFAULT 0,
  `all_models` TINYINT(1) NOT NULL DEFAULT 0,
  `all_brands` TINYINT(1) NOT NULL DEFAULT 0,
  `imd_id` INT(11) NULL,
  `brd_id` INT(11) NULL,
  `itm_id` INT(11) NOT NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  PRIMARY KEY (`mic_id`),
  INDEX `fk_item_model_idx` (`imd_id` ASC) VISIBLE,
  INDEX `fk_brand_idx` (`brd_id` ASC) VISIBLE,
  INDEX `fk_item_idx` (`itm_id` ASC) VISIBLE,
  CONSTRAINT `fk_brand`
    FOREIGN KEY (`brd_id`)
    REFERENCES `carbon`.`brand` (`brd_id`),
  CONSTRAINT `fk_item`
    FOREIGN KEY (`itm_id`)
    REFERENCES `carbon`.`item` (`itm_id`),
  CONSTRAINT `fk_item_model`
    FOREIGN KEY (`imd_id`)
    REFERENCES `carbon`.`item_model` (`imd_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3
-- CRM-473 END

-- CRM-706 START

ALTER TABLE `carbon`.`partner` 
ADD COLUMN `is_assistance` BOOLEAN NULL AFTER `additional_term`;

-- CRM-706 END
