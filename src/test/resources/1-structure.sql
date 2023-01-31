-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema carbon
-- -----------------------------------------------------
SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `classifier`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `classifier` (
  `cla_id` INT(11) NOT NULL,
  `value` VARCHAR(50) NOT NULL,
  `type` VARCHAR(50) NOT NULL,
  `label` VARCHAR(45) NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`cla_id`),
  INDEX `uq_classifier` (`value` ASC, `type` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `menu`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `menu` (
  `mnu_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `url` VARCHAR(255) NULL DEFAULT NULL,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  `icon` VARCHAR(45) NULL DEFAULT NULL,
  `type_cla` INT(11) NOT NULL,
  `root_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`mnu_id`),
  INDEX `idx_menu_root` (`root_id` ASC),
  INDEX `idx_menu_type` (`type_cla` ASC),
  CONSTRAINT `fk_menu_root`
    FOREIGN KEY (`root_id`)
    REFERENCES `menu` (`mnu_id`),
  CONSTRAINT `fk_menu_type`
    FOREIGN KEY (`type_cla`)
    REFERENCES `classifier` (`cla_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `access_list`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `access_list` (
  `acl_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `mnu_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`acl_id`),
  UNIQUE INDEX `uk_access_list` (`name` ASC),
  INDEX `fk_access_list_default_route_idx` (`mnu_id` ASC),
  CONSTRAINT `fk_access_list_default_route`
    FOREIGN KEY (`mnu_id`)
    REFERENCES `menu` (`mnu_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `checkpoint`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `checkpoint` (
  `ckp_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`ckp_id`),
  UNIQUE INDEX `uq_checkpoint` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `access_list_checkpoint`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `access_list_checkpoint` (
  `ckp_id` INT(11) NOT NULL,
  `acl_id` INT(11) NOT NULL,
  PRIMARY KEY (`ckp_id`, `acl_id`),
  INDEX `fk_access_list_checkpoint_access_list1_idx` (`acl_id` ASC),
  CONSTRAINT `fk_access_list_checkpoint_access_list`
    FOREIGN KEY (`acl_id`)
    REFERENCES `access_list` (`acl_id`),
  CONSTRAINT `fk_access_list_checkpoint_checkpoint`
    FOREIGN KEY (`ckp_id`)
    REFERENCES `checkpoint` (`ckp_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `access_list_menu`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `access_list_menu` (
  `acl_id` INT(11) NOT NULL,
  `mnu_id` INT(11) NOT NULL,
  `mnu_order` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`acl_id`, `mnu_id`),
  INDEX `idx_access_list_menu` (`mnu_id` ASC),
  INDEX `idx_access_list` (`acl_id` ASC),
  CONSTRAINT `fk_access_list`
    FOREIGN KEY (`acl_id`)
    REFERENCES `access_list` (`acl_id`),
  CONSTRAINT `fk_access_list_menu`
    FOREIGN KEY (`mnu_id`)
    REFERENCES `menu` (`mnu_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `country`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `country` (
  `cou_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `abbreviation` VARCHAR(10) NULL DEFAULT NULL,
  PRIMARY KEY (`cou_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `state`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `state` (
  `ste_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `abbreviation` VARCHAR(2) NOT NULL,
  `cou_id` INT(11) NOT NULL,
  PRIMARY KEY (`ste_id`),
  INDEX `idx_state_country` (`cou_id` ASC),
  CONSTRAINT `fk_state_country`
    FOREIGN KEY (`cou_id`)
    REFERENCES `country` (`cou_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `city`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `city` (
  `cit_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `ste_id` INT(11) NOT NULL,
  PRIMARY KEY (`cit_id`),
  INDEX `idx_city_state` (`ste_id` ASC),
  CONSTRAINT `fk_city_state`
    FOREIGN KEY (`ste_id`)
    REFERENCES `state` (`ste_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `address`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `address` (
  `add_id` INT(11) NOT NULL AUTO_INCREMENT,
  `street` VARCHAR(255) NULL DEFAULT NULL,
  `number` VARCHAR(50) NULL DEFAULT NULL,
  `district` VARCHAR(255) NULL DEFAULT NULL,
  `complement` VARCHAR(255) NULL DEFAULT NULL,
  `zip_code` VARCHAR(45) NULL DEFAULT NULL,
  `latitude` VARCHAR(255) NULL DEFAULT NULL,
  `longitude` VARCHAR(255) NULL DEFAULT NULL,
  `cit_id` INT(11) NOT NULL,
  PRIMARY KEY (`add_id`),
  INDEX `idx_city` (`cit_id` ASC),
  CONSTRAINT `fk_address_city`
    FOREIGN KEY (`cit_id`)
    REFERENCES `city` (`cit_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `audit`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `audit` (
  `log_id` INT(11) NOT NULL AUTO_INCREMENT,
  `log_date` DATETIME NOT NULL,
  `ip` VARCHAR(45) NOT NULL,
  `hostname` VARCHAR(100) NOT NULL,
  `username` VARCHAR(100) NULL DEFAULT NULL,
  `operation` VARCHAR(255) NOT NULL,
  `details` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`log_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `bank`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bank` (
  `bnk_id` INT(11) NOT NULL AUTO_INCREMENT,
  `code` CHAR(10) NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `active` TINYINT(1) NOT NULL,
  PRIMARY KEY (`bnk_id`),
  UNIQUE INDEX `ukCode` (`code` ASC),
  UNIQUE INDEX `ukName` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `person`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `person` (
  `per_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `job_title` VARCHAR(255) NULL DEFAULT NULL,
  `cpf` VARCHAR(11) NULL DEFAULT NULL,
  `rg` VARCHAR(20) NULL DEFAULT NULL,
  `cnpj` VARCHAR(14) NULL DEFAULT NULL,
  `rne` VARCHAR(45) NULL DEFAULT NULL,
  `birthdate` DATE NULL,
  `add_id` INT(11) NULL,
  `classification_cla_id` INT(11) NOT NULL COMMENT 'classification_cla_id = Classificação da pessoa ( PJ / PF / Estrangeiro ) ( PERSON_CLASSIFICATION )',
  PRIMARY KEY (`per_id`),
  INDEX `fk_person_address1_idx` (`add_id` ASC),
  INDEX `fk_person_classifier1_idx` (`classification_cla_id` ASC),
  CONSTRAINT `fk_person_address1`
    FOREIGN KEY (`add_id`)
    REFERENCES `address` (`add_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_person_classifier1`
    FOREIGN KEY (`classification_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `bank_account`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bank_account` (
  `act_id` INT(11) NOT NULL AUTO_INCREMENT,
  `agency` VARCHAR(45) NULL DEFAULT NULL,
  `account_number` VARCHAR(45) NULL DEFAULT NULL,
  `type_cla_id` INT(11) NOT NULL COMMENT 'Tipo de conta ( ACCOUNT_TYPE )\\\\n\\\\nCorrente \\\\nPoupança',
  `pix_key` VARCHAR(45) NULL DEFAULT NULL,
  `bnk_id` INT(11) NOT NULL,
  `per_id` INT(11) NOT NULL,
  PRIMARY KEY (`act_id`),
  INDEX `fk_account_details_bank1_idx` (`bnk_id` ASC),
  INDEX `fk_bank_account_person1_idx` (`per_id` ASC),
  CONSTRAINT `fk_account_details_bank1`
    FOREIGN KEY (`bnk_id`)
    REFERENCES `bank` (`bnk_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_bank_account_person1`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `brand`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `brand` (
  `brd_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`brd_id`),
  UNIQUE INDEX `ukName` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `channel`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `channel` (
  `chn_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  `has_partner` TINYINT(1) NOT NULL DEFAULT 1,
  `has_internal_sale` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`chn_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COMMENT = 'Lista de canais de atendimento \\\\n\\\\nEx. Concessionária, Intermediário, etc';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `contact`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `contact` (
  `cot_id` INT(11) NOT NULL AUTO_INCREMENT,
  `value` VARCHAR(150) NOT NULL,
  `complement` VARCHAR(150) NULL DEFAULT NULL,
  `type_cla` INT(11) NOT NULL,
  `per_id` INT(11) NOT NULL,
  PRIMARY KEY (`cot_id`),
  INDEX `idx_contact_type` (`type_cla` ASC),
  INDEX `fk_contact_person1_idx` (`per_id` ASC),
  CONSTRAINT `fk_contact_person1`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_contact_type`
    FOREIGN KEY (`type_cla`)
    REFERENCES `classifier` (`cla_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `holding`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `holding` (
  `hol_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `logo` LONGBLOB NULL DEFAULT NULL,
  `cnpj` VARCHAR(100) NULL DEFAULT NULL,
  `social_name` VARCHAR(100) NULL DEFAULT NULL,
  `state_registration` VARCHAR(100) NULL DEFAULT NULL,
  `municipal_registration` VARCHAR(100) NULL DEFAULT NULL,
  `add_id` INT(11) NULL DEFAULT NULL,
  `per_id` INT(11) NOT NULL,
  `type_cla` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`hol_id`),
  INDEX `idxAddress` (`add_id` ASC),
  INDEX `idx_holding_person` (`per_id` ASC),
  INDEX `fk_holding_classifier_idx_idx` (`type_cla` ASC),
  CONSTRAINT `fkAddress`
    FOREIGN KEY (`add_id`)
    REFERENCES `address` (`add_id`),
  CONSTRAINT `fk_holding_classifier_idx`
    FOREIGN KEY (`type_cla`)
    REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_holding_person`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `customer`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `customer` (
  `cus_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `cnpj` VARCHAR(100) NOT NULL,
  `hol_id` INT(11) NOT NULL,
  `type_cla` INT(11) NOT NULL,
  PRIMARY KEY (`cus_id`),
  INDEX `fk_customer_holding_idx` (`hol_id` ASC),
  INDEX `idx_customer` (`cnpj` ASC),
  INDEX `fk_customer_type_idx` (`type_cla` ASC),
  CONSTRAINT `fk_customer_holding`
    FOREIGN KEY (`hol_id`)
    REFERENCES `holding` (`hol_id`),
  CONSTRAINT `fk_customer_type`
    FOREIGN KEY (`type_cla`)
    REFERENCES `classifier` (`cla_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `usr_id` INT(11) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(60) NULL DEFAULT NULL,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `per_id` INT(11) NOT NULL,
  `acl_id` INT(11) NULL DEFAULT NULL,
  `change_pass` TINYINT(1) NOT NULL DEFAULT 0,
  `expire_pass` TINYINT(1) NOT NULL DEFAULT 0,
  `pass_error_count` INT(11) NOT NULL DEFAULT 0,
  `forgot_key` VARCHAR(50) NULL DEFAULT NULL,
  `forgot_key_created` DATETIME NULL DEFAULT NULL,
  `last_pass_change` DATETIME NULL DEFAULT NULL,
  `blocked` TINYINT(1) NOT NULL DEFAULT 0,
  `type_cla` INT(11) NOT NULL,
  `last_login` DATETIME NULL DEFAULT NULL,
  `last_error_count` INT(11) NULL DEFAULT NULL,
  `config` TEXT NULL DEFAULT NULL,
  `cus_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`usr_id`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC),
  INDEX `idx_user_person` (`per_id` ASC),
  INDEX `idx_user_access_list` (`acl_id` ASC),
  INDEX `idx_user_type` (`type_cla` ASC),
  INDEX `fk_user_customer_idx` (`cus_id` ASC),
  CONSTRAINT `fk_user_access_list`
    FOREIGN KEY (`acl_id`)
    REFERENCES `access_list` (`acl_id`),
  CONSTRAINT `fk_user_customer`
    FOREIGN KEY (`cus_id`)
    REFERENCES `customer` (`cus_id`),
  CONSTRAINT `fk_user_person`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_user_type`
    FOREIGN KEY (`type_cla`)
    REFERENCES `classifier` (`cla_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `document`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `document` (
  `doc_id` INT(11) NOT NULL AUTO_INCREMENT,
  `file_name` VARCHAR(150) NOT NULL,
  `content_type` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) NULL,
  `file_path` VARCHAR(255) NOT NULL,
  `create_date` DATETIME NOT NULL,
  `usr_id` INT(11) NOT NULL,
  `type_cla_id` INT(11) NOT NULL,
  PRIMARY KEY (`doc_id`),
  INDEX `fk_document_user_idx` (`usr_id` ASC),
  INDEX `fk_document_classifier1_idx` (`type_cla_id` ASC),
  CONSTRAINT `fk_document_classifier1`
    FOREIGN KEY (`type_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_document_user`
    FOREIGN KEY (`usr_id`)
    REFERENCES `user` (`usr_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `item_type`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `item_type` (
  `itt_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `mandatory` TINYINT(1) NOT NULL DEFAULT 0,
  `multi` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'multi - determina se pode ser adicionar mais do que 1 item deste tipo. Ex. teto solar',
  `seq` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`itt_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `item`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `item` (
  `itm_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL COMMENT 'Nome do item',
  `cod` VARCHAR(45) NULL DEFAULT NULL COMMENT 'Campo para possivelmente guardar o código que referência o item no ERP',
  `seq` INT(11) NOT NULL DEFAULT 0 COMMENT 'Número que será usado para ordenar o item nas telas',
  `for_free` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Flag que marca o item como sendo cortesia',
  `generic` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Flag que marca o item como sendo item genérico',
  `mandatory_cla_id` INT(11) NOT NULL COMMENT 'Classificação do item que determina qual o tipo de obrigatoriedade',
  `itt_id` INT(11) NOT NULL,
  `icon` VARCHAR(255) NULL,
  `label` VARCHAR(45) NULL COMMENT 'Campo para armazenar LABEL que represente o item de forma simples, Ex. TSA para Teto Solar Opaco',
  `description` TEXT NULL,
  `hyperlink` TEXT NULL,
  `photo_url` VARCHAR(45) NULL COMMENT 'Imagem do item',
  PRIMARY KEY (`itm_id`),
  INDEX `fk_item_item_type_idx` (`itt_id` ASC),
  INDEX `fk_item_classifier1_idx` (`mandatory_cla_id` ASC),
  CONSTRAINT `fk_item_classifier1`
    FOREIGN KEY (`mandatory_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_item_item_type`
    FOREIGN KEY (`itt_id`)
    REFERENCES `item_type` (`itt_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COMMENT = 'generic = define se é um item que tem modelo ou não';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `model`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `model` (
  `mdl_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  `brd_id` INT(11) NOT NULL,
  `body_type_cla_id` INT(11) NOT NULL COMMENT 'SUV / HATCH / SEDAN',
  `category_cla_id` INT(11) NOT NULL COMMENT 'category_cla_id = STANDARD / PREMIUM',
  `size_cla_id` INT(11) NOT NULL,
  PRIMARY KEY (`mdl_id`),
  UNIQUE INDEX `ukNameBrand` (`name` ASC, `brd_id` ASC),
  INDEX `fk_model_brand1_idx` (`brd_id` ASC),
  INDEX `fk_cla_body_type_idx` (`body_type_cla_id` ASC),
  INDEX `fk_cla_category_idx` (`category_cla_id` ASC),
  INDEX `fk_model_classifier1_idx` (`size_cla_id` ASC),
  CONSTRAINT `fk_cla_body_type`
    FOREIGN KEY (`body_type_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_cla_category`
    FOREIGN KEY (`category_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_model_brand1`
    FOREIGN KEY (`brd_id`)
    REFERENCES `brand` (`brd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_model_classifier1`
    FOREIGN KEY (`size_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `item_model`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `item_model` (
  `imd_id` INT(11) NOT NULL AUTO_INCREMENT,
  `model_year_start` INT(4) NOT NULL DEFAULT 0,
  `model_year_end` INT(4) NOT NULL DEFAULT 9999,
  `itm_id` INT(11) NOT NULL,
  `mdl_id` INT(11) NOT NULL,
  PRIMARY KEY (`imd_id`),
  INDEX `fk_item_vehicle_model_item1_idx` (`itm_id` ASC),
  INDEX `fk_item_vehicle_model_model1_idx` (`mdl_id` ASC),
  CONSTRAINT `fk_item_vehicle_model_item1`
    FOREIGN KEY (`itm_id`)
    REFERENCES `item` (`itm_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_item_vehicle_model_model1`
    FOREIGN KEY (`mdl_id`)
    REFERENCES `model` (`mdl_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `source`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `source` (
  `src_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`src_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `lead`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `lead` (
  `led_id` INT(11) NOT NULL AUTO_INCREMENT,
  `create_date` DATETIME NOT NULL,
  `end_date` DATETIME NULL DEFAULT NULL,
  `notes` TEXT NULL DEFAULT NULL,
  `client_per_id` INT(11) NOT NULL,
  `seller_per_id` INT(11) NOT NULL,
  `src_id` INT(11) NOT NULL,
  `status_cla_id` INT(11) NOT NULL,
  `mdl_id` INT(11) NULL,
  `brd_id` INT(11) NULL,
  `sale_probability_cla_id` INT(11) NOT NULL,
  `subject` VARCHAR(45) NULL,
  PRIMARY KEY (`led_id`),
  INDEX `fk_lead_person1_idx` (`client_per_id` ASC),
  INDEX `fk_lead_person2_idx` (`seller_per_id` ASC),
  INDEX `fk_lead_source1_idx` (`src_id` ASC),
  INDEX `fk_lead_model1_idx` (`mdl_id` ASC),
  INDEX `fk_lead_brand1_idx` (`brd_id` ASC),
  INDEX `fk_lead_classifier1_idx` (`status_cla_id` ASC),
  INDEX `fk_lead_classitier2_idx` (`sale_probability_cla_id` ASC),
  CONSTRAINT `fk_lead_brand1`
    FOREIGN KEY (`brd_id`)
    REFERENCES `brand` (`brd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_lead_person1`
    FOREIGN KEY (`client_per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_lead_classifier1`
    FOREIGN KEY (`status_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_lead_model1`
    FOREIGN KEY (`mdl_id`)
    REFERENCES `model` (`mdl_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_lead_person2`
    FOREIGN KEY (`seller_per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_lead_classitier2`
    FOREIGN KEY (`sale_probability_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_lead_source1`
    FOREIGN KEY (`src_id`)
    REFERENCES `source` (`src_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `parameter`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `parameter` (
  `prm_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `value` TEXT NOT NULL,
  `description` TEXT NOT NULL,
  PRIMARY KEY (`prm_id`),
  UNIQUE INDEX `uk_parameter` (`name` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `partner_group`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `partner_group` (
  `ptg_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `active` TINYINT(1) NOT NULL COMMENT 'Ao desativar um grupo, desativar todos os parceiros relacionados',
  PRIMARY KEY (`ptg_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `partner`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `partner` (
  `ptn_id` INT(11) NOT NULL AUTO_INCREMENT,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  `entity_per_id` INT(11) NOT NULL COMMENT 'Dado de cadastro do parceiro, seja PF ou PJ',
  `ptg_id` INT(11) NULL DEFAULT NULL,
  `chn_id` INT(11) NOT NULL,
  PRIMARY KEY (`ptn_id`),
  INDEX `fk_partner_person1_idx` (`entity_per_id` ASC),
  INDEX `fk_partner_partner_group1_idx` (`ptg_id` ASC),
  INDEX `fk_partner_channel1_idx` (`chn_id` ASC),
  CONSTRAINT `fk_partner_channel1`
    FOREIGN KEY (`chn_id`)
    REFERENCES `channel` (`chn_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_partner_partner_group1`
    FOREIGN KEY (`ptg_id`)
    REFERENCES `partner_group` (`ptg_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_partner_person1`
    FOREIGN KEY (`entity_per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `partner_brand`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `partner_brand` (
  `ptn_id` INT(11) NOT NULL,
  `brd_id` INT(11) NOT NULL,
  PRIMARY KEY (`ptn_id`, `brd_id`),
  INDEX `fk_partner_brand_brand1_idx` (`brd_id` ASC),
  INDEX `fk_partner_brand_partner1_idx` (`ptn_id` ASC),
  CONSTRAINT `fk_partner_brand_brand1`
    FOREIGN KEY (`brd_id`)
    REFERENCES `brand` (`brd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_partner_brand_partner1`
    FOREIGN KEY (`ptn_id`)
    REFERENCES `partner` (`ptn_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `pass_hist`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `pass_hist` (
  `pas_id` INT(11) NOT NULL AUTO_INCREMENT,
  `password` VARCHAR(60) NOT NULL,
  `change_date` DATETIME NOT NULL,
  `usr_id` INT(11) NOT NULL,
  PRIMARY KEY (`pas_id`),
  INDEX `idx_pass_hist_user` (`usr_id` ASC),
  CONSTRAINT `fk_pass_hist_user`
    FOREIGN KEY (`usr_id`)
    REFERENCES `user` (`usr_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `partner_person`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `partner_person` (
  `ptn_id` INT(11) NOT NULL,
  `per_id` INT(11) NOT NULL,
  `person_type_cla_id` INT(11) NOT NULL,
  PRIMARY KEY (`ptn_id`, `per_id`),
  INDEX `fk_partner_person_person1_idx` (`per_id` ASC),
  INDEX `fk_partner_person_partner1_idx` (`ptn_id` ASC),
  INDEX `fk_partner_person_classifier1_idx` (`person_type_cla_id` ASC),
  CONSTRAINT `fk_partner_person_classifier1`
    FOREIGN KEY (`person_type_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_partner_person_partner1`
    FOREIGN KEY (`ptn_id`)
    REFERENCES `partner` (`ptn_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_partner_person_person1`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_general_ci;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `partner_person_commission`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `partner_person_commission` (
  `ppc_id` INT NOT NULL,
  `commission_type_cla_id` VARCHAR(45) CHARACTER SET 'latin1' NOT NULL COMMENT 'COMISSÂO / BONUS / PAGA PREMIO',
  `commission_default_value` DECIMAL(13,2) NOT NULL,
  `ptn_id` INT(11) NOT NULL,
  `per_id` INT(11) NOT NULL,
  PRIMARY KEY (`ppc_id`),
  INDEX `fk_partner_person_commission_partner_person1_idx` (`ptn_id` ASC, `per_id` ASC),
  CONSTRAINT `fk_partner_person_commission_partner_person1`
    FOREIGN KEY (`ptn_id` , `per_id`)
    REFERENCES `partner_person` (`ptn_id` , `per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `payment_method`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `payment_method` (
  `pym_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL COMMENT 'Nome do métodos de pagamentos\\\\n\\\\nEx. Boleto, TED, Cartão de Crédito',
  `active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Ativa ou desativa a forma de pagamento para aparecer na tela',
  PRIMARY KEY (`pym_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COMMENT = 'Determina os tipos de pagamentos\\\\n\\\\nEx. Boleto, Cartão de Crédito, Transferência\\\\n';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `payment_rule`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `payment_rule` (
  `pyr_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `installments` INT(11) NOT NULL,
  `tax` DECIMAL(8,4) NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  `pre_approved` TINYINT(1) NOT NULL DEFAULT 0,
  `pym_id` INT(11) NOT NULL,
  PRIMARY KEY (`pyr_id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC),
  INDEX `fk_payment_rule_payment_method1_idx` (`pym_id` ASC),
  CONSTRAINT `fk_payment_rule_payment_method1`
    FOREIGN KEY (`pym_id`)
    REFERENCES `payment_method` (`pym_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COMMENT = 'Regras relacionadas ao método de pagamento, determina quantidade de parcelas para cada uma, se existe uma taxa que será cobrada';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `qualification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `qualification` (
  `qlf_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `seq` INT NOT NULL DEFAULT 1,
  PRIMARY KEY (`qlf_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `person_qualification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `person_qualification` (
  `per_id` INT(11) NOT NULL,
  `qlf_id` INT(11) NOT NULL,
  `comments` TEXT NULL,
  PRIMARY KEY (`per_id`, `qlf_id`),
  INDEX `fk_person_qualification_qualification1_idx` (`qlf_id` ASC),
  INDEX `fk_person_qualification_person1_idx` (`per_id` ASC),
  CONSTRAINT `fk_person_qualification_person1`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_person_qualification_qualification1`
    FOREIGN KEY (`qlf_id`)
    REFERENCES `qualification` (`qlf_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `person_related`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `person_related` (
  `psr_id` INT NOT NULL,
  `name` VARCHAR(45) CHARACTER SET 'latin1' NOT NULL,
  `birthdate` DATE NULL,
  `type_cla_id` INT(11) NOT NULL AUTO_INCREMENT,
  `per_id` INT(11) NOT NULL,
  PRIMARY KEY (`psr_id`),
  UNIQUE INDEX `ukRelated` (`name` ASC, `type_cla_id` ASC, `per_id` ASC),
  INDEX `fk_person_related_classifier1_idx` (`type_cla_id` ASC),
  INDEX `fk_person_related_person1_idx` (`per_id` ASC),
  CONSTRAINT `fk_person_related_classifier1`
    FOREIGN KEY (`type_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_person_related_person1`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `portion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `portion` (
  `por_id` INT(11) NOT NULL AUTO_INCREMENT,
  `tax` DECIMAL(19,7) NOT NULL,
  `name` INT(11) NOT NULL,
  `payment_type` INT(11) NOT NULL,
  PRIMARY KEY (`por_id`),
  INDEX `fk_payment_type_idx` (`payment_type` ASC),
  CONSTRAINT `fk_payment_type`
    FOREIGN KEY (`payment_type`)
    REFERENCES `classifier` (`cla_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `price_list`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `price_list` (
  `prl_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL COMMENT 'Nome para a lista de preço\\\\n\\\\nEx. Especial de Natal, Padrão, etc',
  `start_date` DATETIME NOT NULL COMMENT 'Data de início da vigência da lista de preço',
  `end_date` DATETIME NOT NULL COMMENT 'Data de fim da vigência da lista de preço',
  `chn_id` INT(11) NOT NULL,
  PRIMARY KEY (`prl_id`),
  INDEX `fk_price_list_channel1_idx` (`chn_id` ASC),
  CONSTRAINT `fk_price_list_channel1`
    FOREIGN KEY (`chn_id`)
    REFERENCES `channel` (`chn_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `product`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `product` (
  `prd_id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL COMMENT 'Nome do produto Carbon\\\\n\\\\nEx. Carbon Black',
  `active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1 - Ativa o produto para visualização\\\\n0 - Desativa o produto para visualização\\\\n\\\\n',
  `proposal_expiration_days` INT NOT NULL DEFAULT 10 COMMENT 'Prazo padrão de validade na proposta',
  PRIMARY KEY (`prd_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `product_model`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `product_model` (
  `prm_id` INT(11) NOT NULL AUTO_INCREMENT,
  `has_project` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0 - Não existe projeto pronto para o modelo / ano\\\\n1 - Existe projeto pronto para o modelo / ano',
  `model_year_start` INT(4) NOT NULL COMMENT 'Início do range de ano que o produto atende ao modelo\\\\\\\\n\\\\\\\\nEx: 2019 até 2020',
  `model_year_end` INT(4) NOT NULL COMMENT 'Término do range de ano que o produto atende ao modelo\\\\\\\\n\\\\\\\\nEx: 2019 até 2020',
  `manufacture_days` INT(11) NOT NULL DEFAULT 30 COMMENT 'Quantidade de dias de fabricação do produto',
  `prd_id` INT(11) NOT NULL,
  `mdl_id` INT(11) NOT NULL,
  PRIMARY KEY (`prm_id`),
  INDEX `fk_product_vehicle_model_product1_idx` (`prd_id` ASC),
  INDEX `fk_product_vehicle_model_model1_idx` (`mdl_id` ASC),
  CONSTRAINT `fk_product_vehicle_model_model1`
    FOREIGN KEY (`mdl_id`)
    REFERENCES `model` (`mdl_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_product_vehicle_model_product1`
    FOREIGN KEY (`prd_id`)
    REFERENCES `product` (`prd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `price_product`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `price_product` (
  `ppr_id` INT(11) NOT NULL AUTO_INCREMENT,
  `price` DECIMAL(13,2) NOT NULL COMMENT 'Valor do produto praticado',
  `prl_id` INT(11) NOT NULL,
  `prm_id` INT(11) NOT NULL,
  PRIMARY KEY (`ppr_id`),
  INDEX `fk_product_price_price_list1_idx` (`prl_id` ASC),
  INDEX `fk_product_price_product_model1_idx` (`prm_id` ASC),
  CONSTRAINT `fk_product_price_price_list1`
    FOREIGN KEY (`prl_id`)
    REFERENCES `price_list` (`prl_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_product_price_product_model1`
    FOREIGN KEY (`prm_id`)
    REFERENCES `product_model` (`prm_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_general_ci;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `price_item`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `price_item` (
  `pci_id` INT(11) NOT NULL AUTO_INCREMENT,
  `price` DECIMAL(10,0) NOT NULL,
  `itm_id` INT(11) NOT NULL,
  `prl_id` INT(11) NOT NULL,
  PRIMARY KEY (`pci_id`),
  UNIQUE INDEX `ukPriceItem` (`itm_id` ASC, `prl_id` ASC),
  INDEX `fk_item_price_list_item1_idx` (`itm_id` ASC),
  INDEX `fk_item_price_price_list1_idx` (`prl_id` ASC),
  CONSTRAINT `fk_item_price_list_item1`
    FOREIGN KEY (`itm_id`)
    REFERENCES `item` (`itm_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_item_price_price_list1`
    FOREIGN KEY (`prl_id`)
    REFERENCES `price_list` (`prl_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `price_item_model`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `price_item_model` (
  `pim_id` INT(11) NOT NULL AUTO_INCREMENT,
  `price` DECIMAL(10,0) NOT NULL DEFAULT 0,
  `all_models` TINYINT(1) NOT NULL DEFAULT 0,
  `all_brands` TINYINT(1) NOT NULL DEFAULT 0,
  `prl_id` INT(11) NOT NULL,
  `imd_id` INT(11) NULL,
  `brd_id` INT(11) NULL,
  PRIMARY KEY (`pim_id`),
  INDEX `fk_price_list_item_model_item_model1_idx` (`imd_id` ASC),
  UNIQUE INDEX `ukPriceItemModel` (`prl_id` ASC, `imd_id` ASC),
  INDEX `fk_price_list_item_model_price_list1_idx` (`prl_id` ASC),
  INDEX `fk_item_model_price_brand1_idx` (`brd_id` ASC),
  CONSTRAINT `fk_item_model_price_brand1`
    FOREIGN KEY (`brd_id`)
    REFERENCES `brand` (`brd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_price_list_item_model_item_model1`
    FOREIGN KEY (`imd_id`)
    REFERENCES `item_model` (`imd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_price_list_item_model_price_list1`
    FOREIGN KEY (`prl_id`)
    REFERENCES `price_list` (`prl_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `proposal`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proposal` (
  `pps_id` INT(11) NOT NULL AUTO_INCREMENT,
  `num` BIGINT(20) NOT NULL,
  `cod` CHAR(1) NOT NULL,
  `create_date` DATETIME NOT NULL,
  `status_cla_id` INT(11) NOT NULL,
  `led_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`pps_id`),
  UNIQUE INDEX `ukNumCod` (`num` ASC, `cod` ASC),
  INDEX `fk_proposal_lead1_idx` (`led_id` ASC),
  INDEX `fk_proposal_classifier_idx` (`status_cla_id` ASC),
  CONSTRAINT `fk_proposal_classifier`
    FOREIGN KEY (`status_cla_id`)
    REFERENCES `classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_lead1`
    FOREIGN KEY (`led_id`)
    REFERENCES `lead` (`led_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `proposal_detail`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proposal_detail` (
  `ppd_id` INT(11) NOT NULL AUTO_INCREMENT,
  `pps_id` INT(11) NOT NULL,
  `seller_per_id` INT(11) NOT NULL,
  `intern_sale_per_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`ppd_id`),
  UNIQUE INDEX `pps_id_UNIQUE` (`pps_id` ASC),
  INDEX `fk_proposal_detail_proposal1_idx` (`pps_id` ASC),
  INDEX `fk_proposal_detail_person1_idx` (`seller_per_id` ASC),
  INDEX `fk_proposal_detail_person2_idx` (`intern_sale_per_id` ASC),
  CONSTRAINT `fk_proposal_detail_person1`
    FOREIGN KEY (`seller_per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_detail_person2`
    FOREIGN KEY (`intern_sale_per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_detail_proposal1`
    FOREIGN KEY (`pps_id`)
    REFERENCES `proposal` (`pps_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `price_list_partner`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `price_list_partner` (
  `ptn_id` INT(11) NOT NULL,
  `prl_id` INT(11) NOT NULL,
  PRIMARY KEY (`ptn_id`, `prl_id`),
  INDEX `fk_partner_price_list_price_list1_idx` (`prl_id` ASC),
  INDEX `fk_partner_price_list_partner1_idx` (`ptn_id` ASC),
  CONSTRAINT `fk_partner_price_list_partner1`
    FOREIGN KEY (`ptn_id`)
    REFERENCES `partner` (`ptn_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_partner_price_list_price_list1`
    FOREIGN KEY (`prl_id`)
    REFERENCES `price_list` (`prl_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `vehicle`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `vehicle` (
  `vhe_id` INT(11) NOT NULL AUTO_INCREMENT,
  `chassi` VARCHAR(45) NULL DEFAULT NULL,
  `plate` VARCHAR(45) NOT NULL,
  `model_year` INT NOT NULL,
  `purchase_date` DATE NULL,
  `purchase_value` DECIMAL(13,2) NULL,
  `mdl_id` INT(11) NOT NULL,
  PRIMARY KEY (`vhe_id`),
  UNIQUE INDEX `plate_UNIQUE` (`plate` ASC),
  INDEX `fk_vehicle_model1_idx` (`mdl_id` ASC),
  CONSTRAINT `fk_vehicle_model1`
    FOREIGN KEY (`mdl_id`)
    REFERENCES `model` (`mdl_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `proposal_detail_vehicle`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proposal_detail_vehicle` (
  `pdv_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'product_* = Valor copiado referente ao produto selecionado na proposta\\\\nover_* = Valor de desconto por parceiro\\\\nprice_* = Desconto dado na proposta direto\\\\ntotal_* = Total final da proposta e seus descontos',
  `ppd_id` INT(11) NOT NULL,
  `vhe_id` INT(11) NULL,
  `ppr_id` INT(11) NOT NULL,
  `product_amount_discount` DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  `product_percent_discount` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `product_final_price` DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  `over_price` DECIMAL(13,2) NOT NULL,
  `over_price_partner_discount_amount` DECIMAL(13,2) NOT NULL,
  `over_price_partner_discount_percent` DECIMAL(5,2) NOT NULL,
  `price_discount_amount` DECIMAL(13,2) NOT NULL,
  `price_discount_percent` DECIMAL(5,2) NOT NULL,
  `total_amount` DECIMAL(13,2) NOT NULL,
  `total_tax_amount` DECIMAL(13,2) NOT NULL,
  `total_tax_percent` DECIMAL(5,2) NOT NULL,
  PRIMARY KEY (`pdv_id`),
  INDEX `fk_proposal_detail_vehicle_proposal_detail1_idx` (`ppd_id` ASC),
  INDEX `fk_proposal_detail_vehicle_vehicle1_idx` (`vhe_id` ASC),
  INDEX `fk_proposal_detail_vehicle_product_price_list1_idx` (`ppr_id` ASC),
  CONSTRAINT `fk_proposal_detail_vehicle_product_price_list1`
    FOREIGN KEY (`ppr_id`)
    REFERENCES `price_product` (`ppr_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_detail_vehicle_proposal_detail1`
    FOREIGN KEY (`ppd_id`)
    REFERENCES `proposal_detail` (`ppd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_detail_vehicle_vehicle1`
    FOREIGN KEY (`vhe_id`)
    REFERENCES `vehicle` (`vhe_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `proposal_detail_vehicle_item`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proposal_detail_vehicle_item` (
  `pdvi_id` INT(11) NOT NULL AUTO_INCREMENT,
  `amount_discount` DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  `percent_discount` DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  `final_price` DECIMAL(13,2) NOT NULL DEFAULT 0.00,
  `for_free` TINYINT(1) NOT NULL DEFAULT 0,
  `pdv_id` INT(11) NOT NULL,
  `seller_per_id` INT(11) NOT NULL,
  `pci_id` INT(11) NULL,
  `pim_id` INT(11) NULL,
  PRIMARY KEY (`pdvi_id`),
  INDEX `fk_proposal_detail_vehicle_item_price_list_proposal_detail__idx` (`pdv_id` ASC),
  INDEX `fk_proposal_detail_vehicle_item_item_price1_idx` (`pci_id` ASC),
  INDEX `fk_proposal_detail_vehicle_item_item_model_price1_idx` (`pim_id` ASC),
  INDEX `fk_proposal_detail_vehicle_item_person1_idx` (`seller_per_id` ASC),
  CONSTRAINT `fk_proposal_detail_vehicle_item_item_model_price1`
    FOREIGN KEY (`pim_id`)
    REFERENCES `price_item_model` (`pim_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_detail_vehicle_item_item_price1`
    FOREIGN KEY (`pci_id`)
    REFERENCES `price_item` (`pci_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_detail_vehicle_item_person1`
    FOREIGN KEY (`seller_per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_detail_vehicle_item_price_list_proposal_detail_ve1`
    FOREIGN KEY (`pdv_id`)
    REFERENCES `proposal_detail_vehicle` (`pdv_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `proposal_person_client`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proposal_person_client` (
  `pps_id` INT(11) NOT NULL,
  `per_id` INT(11) NOT NULL,
  PRIMARY KEY (`pps_id`, `per_id`),
  INDEX `fk_proposal_person_person1_idx` (`per_id` ASC),
  INDEX `fk_proposal_person_proposal1_idx` (`pps_id` ASC),
  CONSTRAINT `fk_proposal_person_person1`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_person_proposal1`
    FOREIGN KEY (`pps_id`)
    REFERENCES `proposal` (`pps_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `proposal_commission`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proposal_commission` (
  `per_id` INT(11) NOT NULL,
  `due_date` DATETIME NOT NULL,
  `value` DECIMAL(13,2) NOT NULL,
  `notes` TEXT NULL DEFAULT NULL,
  `type_cla_id` INT(11) NOT NULL,
  `pym_id` INT(11) NOT NULL DEFAULT 0,
  `ppd_id` INT(11) NOT NULL,
  `pcm_id` INT NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`pcm_id`),
  INDEX `fk_comission_payment_method1_idx` (`pym_id` ASC),
  INDEX `fk_comission_person1_idx` (`per_id` ASC),
  INDEX `fk_comission_person_proposal_detail1_idx` (`ppd_id` ASC),
  CONSTRAINT `fk_comission_payment_method1`
    FOREIGN KEY (`pym_id`)
    REFERENCES `payment_method` (`pym_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_comission_person1`
    FOREIGN KEY (`per_id`)
    REFERENCES `person` (`per_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_comission_person_proposal_detail1`
    FOREIGN KEY (`ppd_id`)
    REFERENCES `proposal_detail` (`ppd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `proposal_document`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proposal_document` (
  `pps_id` INT(11) NOT NULL,
  `doc_id` INT(11) NOT NULL,
  PRIMARY KEY (`pps_id`, `doc_id`),
  INDEX `fk_proposal_document_document1_idx` (`doc_id` ASC),
  INDEX `fk_proposal_document_proposal1_idx` (`pps_id` ASC),
  CONSTRAINT `fk_proposal_document_document1`
    FOREIGN KEY (`doc_id`)
    REFERENCES `document` (`doc_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_document_proposal1`
    FOREIGN KEY (`pps_id`)
    REFERENCES `proposal` (`pps_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `qualification_tree`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `qualification_tree` (
  `parent_qlf_id` INT(11) NOT NULL,
  `child_qlf_id` INT(11) NOT NULL,
  `level` INT(11) NOT NULL,
  PRIMARY KEY (`parent_qlf_id`, `child_qlf_id`),
  INDEX `fk_qualification_tree_qualification1_idx` (`parent_qlf_id` ASC),
  INDEX `fk_qualification_tree_qualification2_idx` (`child_qlf_id` ASC),
  CONSTRAINT `fk_qualification_tree_qualification1`
    FOREIGN KEY (`parent_qlf_id`)
    REFERENCES `qualification` (`qlf_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_qualification_tree_qualification2`
    FOREIGN KEY (`child_qlf_id`)
    REFERENCES `qualification` (`qlf_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COMMENT = 'Tabela do tipo closure table';

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `sale`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sale` (
  `sal_id` INT(11) NOT NULL AUTO_INCREMENT,
  `customer` VARCHAR(255) NOT NULL,
  `contact` VARCHAR(255) NULL DEFAULT NULL,
  `comments` VARCHAR(255) NULL DEFAULT NULL,
  `date` DATETIME NOT NULL,
  `value` DECIMAL(19,2) NOT NULL,
  `first_payment` DECIMAL(19,2) NOT NULL,
  `tax` DECIMAL(19,7) NOT NULL,
  `portion` INT(11) NOT NULL,
  `payment_type` VARCHAR(45) NOT NULL,
  `usr_id` INT(11) NOT NULL,
  PRIMARY KEY (`sal_id`),
  INDEX `fk_user_sale_idx` (`usr_id` ASC),
  CONSTRAINT `fk_user_sale`
    FOREIGN KEY (`usr_id`)
    REFERENCES `user` (`usr_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `proposal_payment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `proposal_payment` (
  `ppy_id` INT(11) NOT NULL AUTO_INCREMENT,
  `payment_amount` DECIMAL(13,2) NOT NULL DEFAULT 0.00 COMMENT 'Valor para ser pago na parcela',
  `due_date` DATETIME NOT NULL COMMENT 'Data de vencimento do pagamento',
  `total_installments` INT(11) NOT NULL DEFAULT 1 COMMENT 'Quantidade total de parcelas para o método e escolhido',
  `installments` INT(11) NOT NULL DEFAULT 1,
  `ppd_id` INT(11) NOT NULL,
  `pym_id` INT(11) NOT NULL,
  PRIMARY KEY (`ppy_id`),
  UNIQUE INDEX `ukProposalPaymentMethod` (`pym_id` ASC, `ppd_id` ASC),
  INDEX `fk_payment_detail_proposal_detail1_idx` (`ppd_id` ASC),
  INDEX `fk_payment_detail_payment_method1_idx` (`pym_id` ASC),
  CONSTRAINT `fk_payment_detail_payment_method1`
    FOREIGN KEY (`pym_id`)
    REFERENCES `payment_method` (`pym_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_payment_detail_proposal_detail1`
    FOREIGN KEY (`ppd_id`)
    REFERENCES `proposal_detail` (`ppd_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `sale_order`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sale_order` (
  `idpedidos` INT NOT NULL,
  `pps_id` INT(11) NOT NULL,
  PRIMARY KEY (`idpedidos`),
  INDEX `fk_pedidos_proposal1_idx` (`pps_id` ASC),
  CONSTRAINT `fk_pedidos_proposal1`
    FOREIGN KEY (`pps_id`)
    REFERENCES `proposal` (`pps_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `user_customer`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_customer` (
  `usr_id` INT(11) NOT NULL,
  `cus_id` INT(11) NOT NULL,
  PRIMARY KEY (`usr_id`, `cus_id`),
  INDEX `idx_user_has_customer_customer` (`cus_id` ASC),
  INDEX `idx_user_has_customer_user` (`usr_id` ASC),
  CONSTRAINT `fk_user_has_customer_customer`
    FOREIGN KEY (`cus_id`)
    REFERENCES `customer` (`cus_id`),
  CONSTRAINT `fk_user_has_customer_user`
    FOREIGN KEY (`usr_id`)
    REFERENCES `user` (`usr_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

SHOW WARNINGS;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
