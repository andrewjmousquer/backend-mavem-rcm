-- MySQL dump 10.13  Distrib 8.0.29, for Linux (x86_64)
--
-- Host: localhost    Database: carbon
-- ------------------------------------------------------
-- Server version	8.0.29-0ubuntu0.20.04.3

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `access_list`
--

DROP TABLE IF EXISTS `access_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `access_list` (
  `acl_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `mnu_id` int DEFAULT NULL,
  PRIMARY KEY (`acl_id`),
  UNIQUE KEY `uk_access_list` (`name`),
  KEY `fk_access_list_default_route_idx` (`mnu_id`),
  CONSTRAINT `fk_access_list_default_route` FOREIGN KEY (`mnu_id`) REFERENCES `menu` (`mnu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `access_list_checkpoint`
--

DROP TABLE IF EXISTS `access_list_checkpoint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `access_list_checkpoint` (
  `ckp_id` int NOT NULL,
  `acl_id` int NOT NULL,
  PRIMARY KEY (`ckp_id`,`acl_id`),
  KEY `fk_access_list_checkpoint_access_list1_idx` (`acl_id`),
  CONSTRAINT `fk_access_list_checkpoint_access_list` FOREIGN KEY (`acl_id`) REFERENCES `access_list` (`acl_id`),
  CONSTRAINT `fk_access_list_checkpoint_checkpoint` FOREIGN KEY (`ckp_id`) REFERENCES `checkpoint` (`ckp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `access_list_menu`
--

DROP TABLE IF EXISTS `access_list_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `access_list_menu` (
  `acl_id` int NOT NULL,
  `mnu_id` int NOT NULL,
  `mnu_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`acl_id`,`mnu_id`),
  KEY `idx_access_list_menu` (`mnu_id`),
  KEY `idx_access_list` (`acl_id`),
  CONSTRAINT `fk_access_list` FOREIGN KEY (`acl_id`) REFERENCES `access_list` (`acl_id`),
  CONSTRAINT `fk_access_list_menu` FOREIGN KEY (`mnu_id`) REFERENCES `menu` (`mnu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `add_id` int NOT NULL AUTO_INCREMENT,
  `street` varchar(255) DEFAULT NULL,
  `number` varchar(50) DEFAULT NULL,
  `district` varchar(255) DEFAULT NULL,
  `complement` varchar(255) DEFAULT NULL,
  `zip_code` varchar(45) DEFAULT NULL,
  `latitude` varchar(255) DEFAULT NULL,
  `longitude` varchar(255) DEFAULT NULL,
  `cit_id` int NOT NULL,
  PRIMARY KEY (`add_id`),
  KEY `idx_city` (`cit_id`),
  CONSTRAINT `fk_address_city` FOREIGN KEY (`cit_id`) REFERENCES `city` (`cit_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audit`
--

DROP TABLE IF EXISTS `audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `log_date` datetime NOT NULL,
  `ip` varchar(45) NOT NULL,
  `hostname` varchar(100) NOT NULL,
  `username` varchar(100) DEFAULT NULL,
  `operation` varchar(255) NOT NULL,
  `details` text,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1167 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bank`
--

DROP TABLE IF EXISTS `bank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank` (
  `bnk_id` int NOT NULL AUTO_INCREMENT,
  `code` char(10) NOT NULL,
  `name` varchar(45) NOT NULL,
  `active` tinyint(1) NOT NULL,
  PRIMARY KEY (`bnk_id`),
  UNIQUE KEY `ukCode` (`code`),
  UNIQUE KEY `ukName` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bank_account`
--

DROP TABLE IF EXISTS `bank_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank_account` (
  `act_id` int NOT NULL AUTO_INCREMENT,
  `agency` varchar(45) DEFAULT NULL,
  `account_number` varchar(45) DEFAULT NULL,
  `pix_key` varchar(45) DEFAULT NULL,
  `bnk_id` int NOT NULL,
  `per_id` int NOT NULL,
  `type_cla_id` int NOT NULL,
  PRIMARY KEY (`act_id`),
  KEY `fk_account_details_bank1_idx` (`bnk_id`),
  KEY `fk_bank_account_person1_idx` (`per_id`),
  KEY `fk_bank_account_classifier1_idx` (`type_cla_id`),
  CONSTRAINT `fk_account_details_bank1` FOREIGN KEY (`bnk_id`) REFERENCES `bank` (`bnk_id`),
  CONSTRAINT `fk_bank_account_classifier1` FOREIGN KEY (`type_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_bank_account_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `brand`
--

DROP TABLE IF EXISTS `brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brand` (
  `brd_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `cod_fipe` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`brd_id`),
  UNIQUE KEY `ukName` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `channel`
--

DROP TABLE IF EXISTS `channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `channel` (
  `chn_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `has_partner` tinyint(1) NOT NULL DEFAULT '1',
  `has_internal_sale` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`chn_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `checkpoint`
--

DROP TABLE IF EXISTS `checkpoint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `checkpoint` (
  `ckp_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ckp_id`),
  UNIQUE KEY `uq_checkpoint` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `city`
--

DROP TABLE IF EXISTS `city`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `city` (
  `cit_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `ste_id` int NOT NULL,
  PRIMARY KEY (`cit_id`),
  KEY `idx_city_state` (`ste_id`),
  CONSTRAINT `fk_city_state` FOREIGN KEY (`ste_id`) REFERENCES `state` (`ste_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9718 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `classifier`
--

DROP TABLE IF EXISTS `classifier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classifier` (
  `cla_id` int NOT NULL,
  `value` varchar(50) NOT NULL,
  `type` varchar(50) NOT NULL,
  `label` varchar(45) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`cla_id`),
  KEY `uq_classifier` (`value`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contact`
--

DROP TABLE IF EXISTS `contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact` (
  `cot_id` int NOT NULL AUTO_INCREMENT,
  `value` varchar(150) NOT NULL,
  `complement` varchar(150) DEFAULT NULL,
  `type_cla` int NOT NULL,
  `per_id` int NOT NULL,
  PRIMARY KEY (`cot_id`),
  KEY `idx_contact_type` (`type_cla`),
  KEY `fk_contact_person1_idx` (`per_id`),
  CONSTRAINT `fk_contact_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_contact_type` FOREIGN KEY (`type_cla`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `country`
--

DROP TABLE IF EXISTS `country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `country` (
  `cou_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `abbreviation` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`cou_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `cus_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `cnpj` varchar(100) NOT NULL,
  `hol_id` int NOT NULL,
  `type_cla` int NOT NULL,
  PRIMARY KEY (`cus_id`),
  KEY `fk_customer_holding_idx` (`hol_id`),
  KEY `idx_customer` (`cnpj`),
  KEY `fk_customer_type_idx` (`type_cla`),
  CONSTRAINT `fk_customer_holding` FOREIGN KEY (`hol_id`) REFERENCES `holding` (`hol_id`),
  CONSTRAINT `fk_customer_type` FOREIGN KEY (`type_cla`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document`
--

DROP TABLE IF EXISTS `document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `document` (
  `doc_id` int NOT NULL AUTO_INCREMENT,
  `file_name` varchar(150) NOT NULL,
  `content_type` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `file_path` varchar(255) NOT NULL,
  `create_date` datetime NOT NULL,
  `usr_id` int NOT NULL,
  `type_cla_id` int NOT NULL,
  PRIMARY KEY (`doc_id`),
  KEY `fk_document_user_idx` (`usr_id`),
  KEY `fk_document_classifier1_idx` (`type_cla_id`),
  CONSTRAINT `fk_document_classifier1` FOREIGN KEY (`type_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_document_user` FOREIGN KEY (`usr_id`) REFERENCES `user` (`usr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `holding`
--

DROP TABLE IF EXISTS `holding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `holding` (
  `hol_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `logo` longblob,
  `cnpj` varchar(100) DEFAULT NULL,
  `social_name` varchar(100) DEFAULT NULL,
  `state_registration` varchar(100) DEFAULT NULL,
  `municipal_registration` varchar(100) DEFAULT NULL,
  `add_id` int DEFAULT NULL,
  `per_id` int NOT NULL,
  `type_cla` int DEFAULT NULL,
  PRIMARY KEY (`hol_id`),
  KEY `idxAddress` (`add_id`),
  KEY `idx_holding_person` (`per_id`),
  KEY `fk_holding_classifier_idx_idx` (`type_cla`),
  CONSTRAINT `fk_holding_classifier_idx` FOREIGN KEY (`type_cla`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_holding_person` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fkAddress` FOREIGN KEY (`add_id`) REFERENCES `address` (`add_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item` (
  `itm_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT 'Nome do item',
  `cod` varchar(45) DEFAULT NULL COMMENT 'Campo para possivelmente guardar o código que referência o item no ERP',
  `seq` int NOT NULL DEFAULT '0' COMMENT 'Número que será usado para ordenar o item nas telas',
  `for_free` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Flag que marca o item como sendo cortesia',
  `generic` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Flag que marca o item como sendo item genérico',
  `mandatory_cla_id` int NOT NULL COMMENT 'Classificação do item que determina qual o tipo de obrigatoriedade',
  `itt_id` int NOT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `label` varchar(45) DEFAULT NULL COMMENT 'Campo para armazenar LABEL que represente o item de forma simples, Ex. TSA para Teto Solar Opaco',
  `description` text,
  `hyperlink` text,
  `photo_url` varchar(45) DEFAULT NULL COMMENT 'Imagem do item',
  PRIMARY KEY (`itm_id`),
  KEY `fk_item_item_type_idx` (`itt_id`),
  KEY `fk_item_classifier1_idx` (`mandatory_cla_id`),
  CONSTRAINT `fk_item_classifier1` FOREIGN KEY (`mandatory_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_item_item_type` FOREIGN KEY (`itt_id`) REFERENCES `item_type` (`itt_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item_model`
--

DROP TABLE IF EXISTS `item_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_model` (
  `imd_id` int NOT NULL AUTO_INCREMENT,
  `model_year_start` int NOT NULL DEFAULT '0',
  `model_year_end` int NOT NULL DEFAULT '9999',
  `itm_id` int NOT NULL,
  `mdl_id` int NOT NULL,
  PRIMARY KEY (`imd_id`),
  KEY `fk_item_vehicle_model_item1_idx` (`itm_id`),
  KEY `fk_item_vehicle_model_model1_idx` (`mdl_id`),
  CONSTRAINT `fk_item_vehicle_model_item1` FOREIGN KEY (`itm_id`) REFERENCES `item` (`itm_id`),
  CONSTRAINT `fk_item_vehicle_model_model1` FOREIGN KEY (`mdl_id`) REFERENCES `model` (`mdl_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `item_type`
--

DROP TABLE IF EXISTS `item_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item_type` (
  `itt_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `mandatory` tinyint(1) NOT NULL DEFAULT '0',
  `multi` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'multi - determina se pode ser adicionar mais do que 1 item deste tipo. Ex. teto solar',
  `seq` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`itt_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job` (
  `job_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `level` tinyint NOT NULL,
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lead`
--

DROP TABLE IF EXISTS `lead`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lead` (
  `led_id` int NOT NULL AUTO_INCREMENT,
  `create_date` datetime NOT NULL,
  `end_date` datetime DEFAULT NULL,
  `notes` text,
  `client_per_id` int NOT NULL,
  `seller_per_id` int NOT NULL,
  `src_id` int NOT NULL,
  `status_cla_id` int NOT NULL,
  `mdl_id` int DEFAULT NULL,
  `brd_id` int DEFAULT NULL,
  `sale_probability_cla_id` int NOT NULL,
  `subject` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`led_id`),
  KEY `fk_lead_person1_idx` (`client_per_id`),
  KEY `fk_lead_person2_idx` (`seller_per_id`),
  KEY `fk_lead_source1_idx` (`src_id`),
  KEY `fk_lead_model1_idx` (`mdl_id`),
  KEY `fk_lead_brand1_idx` (`brd_id`),
  KEY `fk_lead_classifier1_idx` (`status_cla_id`),
  KEY `fk_lead_classitier2_idx` (`sale_probability_cla_id`),
  CONSTRAINT `fk_lead_brand1` FOREIGN KEY (`brd_id`) REFERENCES `brand` (`brd_id`),
  CONSTRAINT `fk_lead_classifier1` FOREIGN KEY (`status_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_lead_classitier2` FOREIGN KEY (`sale_probability_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_lead_model1` FOREIGN KEY (`mdl_id`) REFERENCES `model` (`mdl_id`),
  CONSTRAINT `fk_lead_person1` FOREIGN KEY (`client_per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_lead_person2` FOREIGN KEY (`seller_per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_lead_source1` FOREIGN KEY (`src_id`) REFERENCES `source` (`src_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lead_fup`
--

DROP TABLE IF EXISTS `lead_fup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lead_fup` (
  `lfp_id` int NOT NULL AUTO_INCREMENT,
  `led_id` int NOT NULL,
  `date` datetime NOT NULL,
  `media_cla_id` int NOT NULL,
  `person` varchar(150) NOT NULL,
  `comment` varchar(255) NOT NULL,
  PRIMARY KEY (`lfp_id`),
  KEY `fk_lead_fup_media_idx` (`media_cla_id`),
  KEY `fk_lead_fup_lead1_idx` (`led_id`),
  CONSTRAINT `fk_lead_fup_lead1` FOREIGN KEY (`led_id`) REFERENCES `lead` (`led_id`),
  CONSTRAINT `fk_lead_fup_media` FOREIGN KEY (`media_cla_id`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `menu`
--

DROP TABLE IF EXISTS `menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu` (
  `mnu_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `icon` varchar(45) DEFAULT NULL,
  `type_cla` int NOT NULL,
  `root_id` int DEFAULT NULL,
  `show` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`mnu_id`),
  KEY `idx_menu_root` (`root_id`),
  KEY `idx_menu_type` (`type_cla`),
  CONSTRAINT `fk_menu_root` FOREIGN KEY (`root_id`) REFERENCES `menu` (`mnu_id`),
  CONSTRAINT `fk_menu_type` FOREIGN KEY (`type_cla`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `model`
--

DROP TABLE IF EXISTS `model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `model` (
  `mdl_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `brd_id` int NOT NULL,
  `body_type_cla_id` int NOT NULL COMMENT 'SUV / HATCH / SEDAN',
  `category_cla_id` int NOT NULL COMMENT 'category_cla_id = STANDARD / PREMIUM',
  `type_cla_id` int NOT NULL,
  `cod_fipe` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`mdl_id`),
  UNIQUE KEY `ukNameBrand` (`name`,`brd_id`),
  KEY `fk_model_brand1_idx` (`brd_id`),
  KEY `fk_cla_body_type_idx` (`body_type_cla_id`),
  KEY `fk_cla_category_idx` (`category_cla_id`),
  KEY `fk_model_classifier1_idx` (`type_cla_id`),
  CONSTRAINT `fk_cla_body_type` FOREIGN KEY (`body_type_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_cla_category` FOREIGN KEY (`category_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_model_brand1` FOREIGN KEY (`brd_id`) REFERENCES `brand` (`brd_id`),
  CONSTRAINT `fk_model_classifier1` FOREIGN KEY (`type_cla_id`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `parameter`
--

DROP TABLE IF EXISTS `parameter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parameter` (
  `prm_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `value` text NOT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`prm_id`),
  UNIQUE KEY `uk_parameter` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `partner`
--

DROP TABLE IF EXISTS `partner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `partner` (
  `ptn_id` int NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `entity_per_id` int NOT NULL COMMENT 'Dado de cadastro do parceiro, seja PF ou PJ',
  `ptg_id` int DEFAULT NULL,
  `chn_id` int NOT NULL,
  PRIMARY KEY (`ptn_id`),
  KEY `fk_partner_person1_idx` (`entity_per_id`),
  KEY `fk_partner_partner_group1_idx` (`ptg_id`),
  KEY `fk_partner_channel1_idx` (`chn_id`),
  CONSTRAINT `fk_partner_channel1` FOREIGN KEY (`chn_id`) REFERENCES `channel` (`chn_id`),
  CONSTRAINT `fk_partner_partner_group1` FOREIGN KEY (`ptg_id`) REFERENCES `partner_group` (`ptg_id`),
  CONSTRAINT `fk_partner_person1` FOREIGN KEY (`entity_per_id`) REFERENCES `person` (`per_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `partner_brand`
--

DROP TABLE IF EXISTS `partner_brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `partner_brand` (
  `ptn_id` int NOT NULL,
  `brd_id` int NOT NULL,
  PRIMARY KEY (`ptn_id`,`brd_id`),
  KEY `fk_partner_brand_brand1_idx` (`brd_id`),
  KEY `fk_partner_brand_partner1_idx` (`ptn_id`),
  CONSTRAINT `fk_partner_brand_brand1` FOREIGN KEY (`brd_id`) REFERENCES `brand` (`brd_id`),
  CONSTRAINT `fk_partner_brand_partner1` FOREIGN KEY (`ptn_id`) REFERENCES `partner` (`ptn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `partner_group`
--

DROP TABLE IF EXISTS `partner_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `partner_group` (
  `ptg_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `active` tinyint(1) NOT NULL COMMENT 'Ao desativar um grupo, desativar todos os parceiros relacionados',
  PRIMARY KEY (`ptg_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `partner_person`
--

DROP TABLE IF EXISTS `partner_person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `partner_person` (
  `ptn_id` int NOT NULL,
  `per_id` int NOT NULL,
  `person_type_cla_id` int NOT NULL,
  PRIMARY KEY (`ptn_id`,`per_id`),
  KEY `fk_partner_person_person1_idx` (`per_id`),
  KEY `fk_partner_person_partner1_idx` (`ptn_id`),
  KEY `fk_partner_person_classifier1_idx` (`person_type_cla_id`),
  CONSTRAINT `fk_partner_person_classifier1` FOREIGN KEY (`person_type_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_partner_person_partner1` FOREIGN KEY (`ptn_id`) REFERENCES `partner` (`ptn_id`),
  CONSTRAINT `fk_partner_person_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `partner_person_commission`
--

DROP TABLE IF EXISTS `partner_person_commission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `partner_person_commission` (
  `commission_default_value` decimal(13,2) NOT NULL,
  `commission_type_cla_id` int NOT NULL,
  `ptn_id` int NOT NULL,
  `per_id` int NOT NULL,
  UNIQUE KEY `partner_person_commission_UNIQUE` (`commission_type_cla_id`,`ptn_id`,`per_id`),
  KEY `fk_partner_person_commission_partner_idx` (`ptn_id`),
  KEY `fk_partner_person_commission_classifier_idx` (`commission_type_cla_id`),
  KEY `fk_partner_person_commission_person_idx` (`per_id`),
  CONSTRAINT `fk_partner_person_commission_classifier` FOREIGN KEY (`commission_type_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_partner_person_commission_partner` FOREIGN KEY (`ptn_id`) REFERENCES `partner` (`ptn_id`),
  CONSTRAINT `fk_partner_person_commission_person` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pass_hist`
--

DROP TABLE IF EXISTS `pass_hist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pass_hist` (
  `pas_id` int NOT NULL AUTO_INCREMENT,
  `password` varchar(60) NOT NULL,
  `change_date` datetime NOT NULL,
  `usr_id` int NOT NULL,
  PRIMARY KEY (`pas_id`),
  KEY `idx_pass_hist_user` (`usr_id`),
  CONSTRAINT `fk_pass_hist_user` FOREIGN KEY (`usr_id`) REFERENCES `user` (`usr_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment_method`
--

DROP TABLE IF EXISTS `payment_method`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_method` (
  `pym_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Ativa ou desativa a forma de pagamento para aparecer na tela',
  PRIMARY KEY (`pym_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment_rule`
--

DROP TABLE IF EXISTS `payment_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_rule` (
  `pyr_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `installments` int NOT NULL,
  `tax` decimal(8,4) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `pre_approved` tinyint(1) NOT NULL DEFAULT '0',
  `pym_id` int NOT NULL,
  PRIMARY KEY (`pyr_id`),
  UNIQUE KEY `name_UNIQUE` (`name`,`pym_id`),
  KEY `fk_payment_rule_payment_method1_idx` (`pym_id`),
  CONSTRAINT `fk_payment_rule_payment_method1` FOREIGN KEY (`pym_id`) REFERENCES `payment_method` (`pym_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `person` (
  `per_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `job_title` varchar(255) DEFAULT NULL,
  `cpf` varchar(11) DEFAULT NULL,
  `rg` varchar(20) DEFAULT NULL,
  `cnpj` varchar(14) DEFAULT NULL,
  `rne` varchar(45) DEFAULT NULL,
  `birthdate` date DEFAULT NULL,
  `add_id` int DEFAULT NULL,
  `classification_cla_id` int NOT NULL COMMENT 'classification_cla_id = Classificação da pessoa ( PJ / PF / Estrangeiro ) ( PERSON_CLASSIFICATION )',
  PRIMARY KEY (`per_id`),
  KEY `fk_person_address1_idx` (`add_id`),
  KEY `fk_person_classifier1_idx` (`classification_cla_id`),
  CONSTRAINT `fk_person_address1` FOREIGN KEY (`add_id`) REFERENCES `address` (`add_id`),
  CONSTRAINT `fk_person_classifier1` FOREIGN KEY (`classification_cla_id`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person_qualification`
--

DROP TABLE IF EXISTS `person_qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `person_qualification` (
  `per_id` int NOT NULL,
  `qlf_id` int NOT NULL,
  `comments` text,
  PRIMARY KEY (`per_id`,`qlf_id`),
  KEY `fk_person_qualification_qualification1_idx` (`qlf_id`),
  KEY `fk_person_qualification_person1_idx` (`per_id`),
  CONSTRAINT `fk_person_qualification_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_person_qualification_qualification1` FOREIGN KEY (`qlf_id`) REFERENCES `qualification` (`qlf_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person_related`
--

DROP TABLE IF EXISTS `person_related`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `person_related` (
  `psr_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `birthdate` date NOT NULL,
  `type_cla_id` int NOT NULL,
  `per_id` int NOT NULL,
  PRIMARY KEY (`psr_id`),
  UNIQUE KEY `ukRelated` (`type_cla_id`,`per_id`,`name`),
  KEY `fk_person_related_classifier1_idx` (`type_cla_id`),
  KEY `fk_person_related_person1_idx` (`per_id`),
  CONSTRAINT `fk_person_related_classifier1` FOREIGN KEY (`type_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_person_related_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `portion`
--

DROP TABLE IF EXISTS `portion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `portion` (
  `por_id` int NOT NULL AUTO_INCREMENT,
  `tax` decimal(19,7) NOT NULL,
  `name` int NOT NULL,
  `payment_type` int NOT NULL,
  PRIMARY KEY (`por_id`),
  KEY `fk_payment_type_idx` (`payment_type`),
  CONSTRAINT `fk_payment_type` FOREIGN KEY (`payment_type`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `price_item`
--

DROP TABLE IF EXISTS `price_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_item` (
  `pci_id` int NOT NULL AUTO_INCREMENT,
  `price` decimal(13,2) NOT NULL,
  `itm_id` int NOT NULL,
  `prl_id` int NOT NULL,
  PRIMARY KEY (`pci_id`),
  UNIQUE KEY `ukPriceItem` (`itm_id`,`prl_id`),
  KEY `fk_item_price_list_item1_idx` (`itm_id`),
  KEY `fk_item_price_price_list1_idx` (`prl_id`),
  CONSTRAINT `fk_item_price_list_item1` FOREIGN KEY (`itm_id`) REFERENCES `item` (`itm_id`),
  CONSTRAINT `fk_item_price_price_list1` FOREIGN KEY (`prl_id`) REFERENCES `price_list` (`prl_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `price_item_model`
--

DROP TABLE IF EXISTS `price_item_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_item_model` (
  `pim_id` int NOT NULL AUTO_INCREMENT,
  `price` decimal(13,2) NOT NULL DEFAULT '0.00',
  `all_models` tinyint(1) NOT NULL DEFAULT '0',
  `all_brands` tinyint(1) NOT NULL DEFAULT '0',
  `prl_id` int NOT NULL,
  `imd_id` int DEFAULT NULL,
  `brd_id` int DEFAULT NULL,
  `itm_id` int NOT NULL,
  PRIMARY KEY (`pim_id`),
  UNIQUE KEY `ukPriceItemModel` (`prl_id`,`imd_id`),
  KEY `fk_price_list_item_model_item_model1_idx` (`imd_id`),
  KEY `fk_price_list_item_model_price_list1_idx` (`prl_id`),
  KEY `fk_item_model_price_brand1_idx` (`brd_id`),
  KEY `fk_price_item_model_item1_idx` (`itm_id`),
  CONSTRAINT `fk_item_model_price_brand1` FOREIGN KEY (`brd_id`) REFERENCES `brand` (`brd_id`),
  CONSTRAINT `fk_price_item_model_item1` FOREIGN KEY (`itm_id`) REFERENCES `item` (`itm_id`),
  CONSTRAINT `fk_price_list_item_model_item_model1` FOREIGN KEY (`imd_id`) REFERENCES `item_model` (`imd_id`),
  CONSTRAINT `fk_price_list_item_model_price_list1` FOREIGN KEY (`prl_id`) REFERENCES `price_list` (`prl_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `price_list`
--

DROP TABLE IF EXISTS `price_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_list` (
  `prl_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `start_date` datetime NOT NULL COMMENT 'Data de início da vigência da lista de preço',
  `end_date` datetime NOT NULL COMMENT 'Data de fim da vigência da lista de preço',
  `chn_id` int NOT NULL,
  `all_partners` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`prl_id`),
  KEY `fk_price_list_channel1_idx` (`chn_id`),
  CONSTRAINT `fk_price_list_channel1` FOREIGN KEY (`chn_id`) REFERENCES `channel` (`chn_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `price_list_partner`
--

DROP TABLE IF EXISTS `price_list_partner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_list_partner` (
  `ptn_id` int NOT NULL,
  `prl_id` int NOT NULL,
  PRIMARY KEY (`ptn_id`,`prl_id`),
  KEY `fk_partner_price_list_price_list1_idx` (`prl_id`),
  KEY `fk_partner_price_list_partner1_idx` (`ptn_id`),
  CONSTRAINT `fk_partner_price_list_partner1` FOREIGN KEY (`ptn_id`) REFERENCES `partner` (`ptn_id`),
  CONSTRAINT `fk_partner_price_list_price_list1` FOREIGN KEY (`prl_id`) REFERENCES `price_list` (`prl_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `price_product`
--

DROP TABLE IF EXISTS `price_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_product` (
  `ppr_id` int NOT NULL AUTO_INCREMENT,
  `price` decimal(13,2) NOT NULL COMMENT 'Valor do produto praticado',
  `prl_id` int NOT NULL,
  `prm_id` int NOT NULL,
  PRIMARY KEY (`ppr_id`),
  KEY `fk_product_price_price_list1_idx` (`prl_id`),
  KEY `fk_product_price_product_model1_idx` (`prm_id`),
  CONSTRAINT `fk_product_price_price_list1` FOREIGN KEY (`prl_id`) REFERENCES `price_list` (`prl_id`),
  CONSTRAINT `fk_product_price_product_model1` FOREIGN KEY (`prm_id`) REFERENCES `product_model` (`prm_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `prd_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `proposal_expiration_days` int NOT NULL DEFAULT '10',
  PRIMARY KEY (`prd_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_model`
--

DROP TABLE IF EXISTS `product_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_model` (
  `prm_id` int NOT NULL AUTO_INCREMENT,
  `has_project` tinyint(1) NOT NULL DEFAULT '1',
  `model_year_start` int NOT NULL,
  `model_year_end` int NOT NULL,
  `manufacture_days` int NOT NULL DEFAULT '30' COMMENT '\n',
  `prd_id` int NOT NULL,
  `mdl_id` int NOT NULL,
  PRIMARY KEY (`prm_id`),
  KEY `fk_product_vehicle_model_product1_idx` (`prd_id`),
  KEY `fk_product_vehicle_model_model1_idx` (`mdl_id`),
  CONSTRAINT `fk_product_vehicle_model_model1` FOREIGN KEY (`mdl_id`) REFERENCES `model` (`mdl_id`),
  CONSTRAINT `fk_product_vehicle_model_product1` FOREIGN KEY (`prd_id`) REFERENCES `product` (`prd_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal`
--

DROP TABLE IF EXISTS `proposal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal` (
  `pps_id` int NOT NULL AUTO_INCREMENT,
  `num` bigint NOT NULL,
  `cod` char(2) NOT NULL,
  `order_number` int NOT NULL,
  `create_date` datetime NOT NULL,
  `status_cla_id` int NOT NULL,
  `led_id` int DEFAULT NULL,
  `finantial_contact` tinyint NOT NULL DEFAULT '0',
  `finantial_contact_name` varchar(150) DEFAULT NULL,
  `finantial_contact_email` varchar(100) DEFAULT NULL,
  `finantial_contact_phone` varchar(45) DEFAULT NULL,
  `document_contact` tinyint NOT NULL DEFAULT '0',
  `document_contact_name` varchar(150) DEFAULT NULL,
  `document_contact_email` varchar(100) DEFAULT NULL,
  `document_contact_phone` varchar(45) DEFAULT NULL,
  `risk_cla_id` int NOT NULL,
  `immediate_delivery` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`pps_id`),
  UNIQUE KEY `ukNumCod` (`num`,`cod`),
  KEY `fk_proposal_lead1_idx` (`led_id`),
  KEY `fk_proposal_classifier_idx` (`status_cla_id`),
  KEY `fk_proposal_classifier_risk_idx` (`risk_cla_id`),
  CONSTRAINT `fk_proposal_classifier` FOREIGN KEY (`status_cla_id`) REFERENCES `classifier` (`cla_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_proposal_classifier_risk` FOREIGN KEY (`risk_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_proposal_lead1` FOREIGN KEY (`led_id`) REFERENCES `lead` (`led_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_approval`
--

DROP TABLE IF EXISTS `proposal_approval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_approval` (
  `pps_id` int NOT NULL,
  `per_id` int NOT NULL,
  `date` datetime NOT NULL,
  `status_cla_id` int NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  KEY `fk_proposal_approval_proposal1_idx` (`pps_id`),
  KEY `fk_proposal_approval_person1_idx` (`per_id`),
  KEY `fk_proposal_approval_classifier1_idx` (`status_cla_id`),
  CONSTRAINT `fk_proposal_approval_classifier1` FOREIGN KEY (`status_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_proposal_approval_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_proposal_approval_proposal1` FOREIGN KEY (`pps_id`) REFERENCES `proposal` (`pps_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_approval_rule`
--

DROP TABLE IF EXISTS `proposal_approval_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_approval_rule` (
  `par_id` int NOT NULL AUTO_INCREMENT,
  `value_start` decimal(13,2) NOT NULL,
  `value_end` decimal(13,2) NOT NULL,
  `job_id` int NOT NULL,
  PRIMARY KEY (`par_id`,`job_id`),
  KEY `fk_proposal_approval_rule_job_idx` (`job_id`),
  CONSTRAINT `fk_proposal_approval_rule_job` FOREIGN KEY (`job_id`) REFERENCES `job` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_commission`
--

DROP TABLE IF EXISTS `proposal_commission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_commission` (
  `per_id` int NOT NULL,
  `due_date` datetime NOT NULL,
  `value` decimal(13,2) NOT NULL,
  `notes` text,
  `type_cla_id` int NOT NULL,
  `pym_id` int NOT NULL DEFAULT '0',
  `ppd_id` int NOT NULL,
  `pcm_id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`pcm_id`),
  KEY `fk_comission_payment_method1_idx` (`pym_id`),
  KEY `fk_comission_person1_idx` (`per_id`),
  KEY `fk_comission_person_proposal_detail1_idx` (`ppd_id`),
  CONSTRAINT `fk_comission_payment_method1` FOREIGN KEY (`pym_id`) REFERENCES `payment_method` (`pym_id`),
  CONSTRAINT `fk_comission_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_comission_person_proposal_detail1` FOREIGN KEY (`ppd_id`) REFERENCES `proposal_detail` (`ppd_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_detail`
--

DROP TABLE IF EXISTS `proposal_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_detail` (
  `ppd_id` int NOT NULL AUTO_INCREMENT,
  `pps_id` int NOT NULL,
  `seller_per_id` int NOT NULL,
  `intern_sale_per_id` int DEFAULT NULL,
  `chn_id` int NOT NULL,
  `ptn_id` int DEFAULT NULL,
  PRIMARY KEY (`ppd_id`),
  UNIQUE KEY `pps_id_UNIQUE` (`pps_id`),
  KEY `fk_proposal_detail_proposal1_idx` (`pps_id`),
  KEY `fk_proposal_detail_person2_idx` (`intern_sale_per_id`),
  KEY `fk_proposal_detail_person1_idx` (`seller_per_id`),
  KEY `fk_proposal_detail_channel1_idx` (`chn_id`),
  KEY `fk_proposal_detail_partner1_idx` (`ptn_id`),
  CONSTRAINT `fk_proposal_detail_channel1` FOREIGN KEY (`chn_id`) REFERENCES `channel` (`chn_id`),
  CONSTRAINT `fk_proposal_detail_partner1` FOREIGN KEY (`ptn_id`) REFERENCES `partner` (`ptn_id`),
  CONSTRAINT `fk_proposal_detail_person1` FOREIGN KEY (`seller_per_id`) REFERENCES `person` (`per_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_proposal_detail_person2` FOREIGN KEY (`intern_sale_per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_proposal_detail_proposal1` FOREIGN KEY (`pps_id`) REFERENCES `proposal` (`pps_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_detail_vehicle`
--

DROP TABLE IF EXISTS `proposal_detail_vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_detail_vehicle` (
  `pdv_id` int NOT NULL AUTO_INCREMENT,
  `ppd_id` int NOT NULL,
  `vhe_id` int DEFAULT NULL,
  `ppr_id` int NOT NULL,
  `product_amount_discount` decimal(13,2) NOT NULL DEFAULT '0.00',
  `product_percent_discount` decimal(5,2) NOT NULL DEFAULT '0.00',
  `product_final_price` decimal(13,2) NOT NULL DEFAULT '0.00',
  `over_price` decimal(13,2) NOT NULL,
  `over_price_partner_discount_amount` decimal(13,2) NOT NULL,
  `over_price_partner_discount_percent` decimal(5,2) NOT NULL,
  `price_discount_amount` decimal(13,2) NOT NULL,
  `price_discount_percent` decimal(5,2) NOT NULL,
  `total_amount` decimal(13,2) NOT NULL,
  `total_tax_amount` decimal(13,2) NOT NULL,
  `total_tax_percent` decimal(5,2) NOT NULL,
  `standard_term_days` int NOT NULL,
  `agreed_term_days` int NOT NULL,
  PRIMARY KEY (`pdv_id`),
  KEY `fk_proposal_detail_vehicle_proposal_detail1_idx` (`ppd_id`),
  KEY `fk_proposal_detail_vehicle_vehicle1_idx` (`vhe_id`),
  KEY `fk_proposal_detail_vehicle_product_price_list1_idx` (`ppr_id`),
  CONSTRAINT `fk_proposal_detail_vehicle_product_price_list1` FOREIGN KEY (`ppr_id`) REFERENCES `price_product` (`ppr_id`),
  CONSTRAINT `fk_proposal_detail_vehicle_proposal_detail1` FOREIGN KEY (`ppd_id`) REFERENCES `proposal_detail` (`ppd_id`),
  CONSTRAINT `fk_proposal_detail_vehicle_vehicle1` FOREIGN KEY (`vhe_id`) REFERENCES `vehicle` (`vhe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_detail_vehicle_item`
--

DROP TABLE IF EXISTS `proposal_detail_vehicle_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_detail_vehicle_item` (
  `pdvi_id` int NOT NULL AUTO_INCREMENT,
  `amount_discount` decimal(13,2) NOT NULL DEFAULT '0.00',
  `percent_discount` decimal(3,2) NOT NULL DEFAULT '0.00',
  `final_price` decimal(13,2) NOT NULL DEFAULT '0.00',
  `for_free` tinyint(1) NOT NULL DEFAULT '0',
  `pdv_id` int NOT NULL,
  `seller_per_id` int NOT NULL,
  `pci_id` int DEFAULT NULL,
  `pim_id` int DEFAULT NULL,
  PRIMARY KEY (`pdvi_id`),
  KEY `fk_proposal_detail_vehicle_item_price_list_proposal_detail__idx` (`pdv_id`),
  KEY `fk_proposal_detail_vehicle_item_person1_idx` (`seller_per_id`),
  KEY `fk_proposal_detail_vehicle_item_item_price1_idx` (`pci_id`),
  KEY `fk_proposal_detail_vehicle_item_item_model_price1_idx` (`pim_id`),
  CONSTRAINT `fk_proposal_detail_vehicle_item_item_model_price1` FOREIGN KEY (`pim_id`) REFERENCES `price_item_model` (`pim_id`),
  CONSTRAINT `fk_proposal_detail_vehicle_item_item_price1` FOREIGN KEY (`pci_id`) REFERENCES `price_item` (`pci_id`),
  CONSTRAINT `fk_proposal_detail_vehicle_item_person1` FOREIGN KEY (`seller_per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_proposal_detail_vehicle_item_price_list_proposal_detail_ve1` FOREIGN KEY (`pdv_id`) REFERENCES `proposal_detail_vehicle` (`pdv_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_document`
--

DROP TABLE IF EXISTS `proposal_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_document` (
  `pps_id` int NOT NULL,
  `doc_id` int NOT NULL,
  PRIMARY KEY (`pps_id`,`doc_id`),
  KEY `fk_proposal_document_document1_idx` (`doc_id`),
  KEY `fk_proposal_document_proposal1_idx` (`pps_id`),
  CONSTRAINT `fk_proposal_document_document1` FOREIGN KEY (`doc_id`) REFERENCES `document` (`doc_id`),
  CONSTRAINT `fk_proposal_document_proposal1` FOREIGN KEY (`pps_id`) REFERENCES `proposal` (`pps_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_fup`
--

DROP TABLE IF EXISTS `proposal_fup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_fup` (
  `pfp_id` int NOT NULL AUTO_INCREMENT,
  `pps_id` int NOT NULL,
  `date` datetime NOT NULL,
  `media_cla_id` int NOT NULL,
  `person` varchar(150) NOT NULL,
  `comment` varchar(255) NOT NULL,
  PRIMARY KEY (`pfp_id`),
  KEY `fk_proposal_fup_proposal1_idx` (`pps_id`),
  KEY `fk_proposal_fup_media_idx` (`media_cla_id`),
  CONSTRAINT `fk_proposal_fup_media` FOREIGN KEY (`media_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_proposal_fup_proposal1` FOREIGN KEY (`pps_id`) REFERENCES `proposal` (`pps_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_payment`
--

DROP TABLE IF EXISTS `proposal_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_payment` (
  `ppy_id` int NOT NULL AUTO_INCREMENT,
  `payment_amount` decimal(13,2) NOT NULL DEFAULT '0.00' COMMENT 'Valor para ser pago na parcela',
  `due_date` datetime NOT NULL COMMENT 'Data de vencimento do pagamento',
  `installments` int NOT NULL DEFAULT '1',
  `installment_amount` decimal(13,2) DEFAULT NULL,
  `interest` decimal(13,2) NOT NULL,
  `ppd_id` int NOT NULL COMMENT 'Quantidade total de parcelas para o método e escolhido',
  `pym_id` int NOT NULL,
  `payer_cla_id` int NOT NULL,
  `event_cla_id` int NOT NULL,
  `days` int DEFAULT NULL,
  `pre_approved` tinyint NOT NULL,
  `antecipated_billing` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`ppy_id`),
  UNIQUE KEY `ukProposalPaymentMethod` (`pym_id`,`ppd_id`),
  KEY `fk_payment_detail_proposal_detail1_idx` (`ppd_id`),
  KEY `fk_payment_detail_payment_method1_idx` (`pym_id`),
  KEY `fk_proposal_payment_classifier1_idx` (`payer_cla_id`),
  KEY `fk_proposal_payment_classifier2_idx` (`event_cla_id`),
  CONSTRAINT `fk_payment_detail_payment_method1` FOREIGN KEY (`pym_id`) REFERENCES `payment_method` (`pym_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_payment_detail_proposal_detail1` FOREIGN KEY (`ppd_id`) REFERENCES `proposal_detail` (`ppd_id`),
  CONSTRAINT `fk_proposal_payment_classifier1` FOREIGN KEY (`payer_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_proposal_payment_classifier2` FOREIGN KEY (`event_cla_id`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proposal_person_client`
--

DROP TABLE IF EXISTS `proposal_person_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal_person_client` (
  `pps_id` int NOT NULL,
  `per_id` int NOT NULL,
  `customer_cla_id` int NOT NULL,
  PRIMARY KEY (`pps_id`,`per_id`,`customer_cla_id`),
  KEY `fk_proposal_person_person1_idx` (`per_id`),
  KEY `fk_proposal_person_proposal1_idx` (`pps_id`),
  KEY `fk_proposal_person_client_classifier1_idx` (`customer_cla_id`),
  CONSTRAINT `fk_proposal_person_client_classifier1` FOREIGN KEY (`customer_cla_id`) REFERENCES `classifier` (`cla_id`),
  CONSTRAINT `fk_proposal_person_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_proposal_person_proposal1` FOREIGN KEY (`pps_id`) REFERENCES `proposal` (`pps_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qualification`
--

DROP TABLE IF EXISTS `qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qualification` (
  `qlf_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `seq` int NOT NULL DEFAULT '1',
  PRIMARY KEY (`qlf_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qualification_tree`
--

DROP TABLE IF EXISTS `qualification_tree`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qualification_tree` (
  `parent_qlf_id` int NOT NULL,
  `child_qlf_id` int NOT NULL,
  `level` int NOT NULL,
  PRIMARY KEY (`parent_qlf_id`,`child_qlf_id`),
  KEY `fk_qualification_tree_qualification1_idx` (`parent_qlf_id`),
  KEY `fk_qualification_tree_qualification2_idx` (`child_qlf_id`),
  CONSTRAINT `fk_qualification_tree_qualification1` FOREIGN KEY (`parent_qlf_id`) REFERENCES `qualification` (`qlf_id`),
  CONSTRAINT `fk_qualification_tree_qualification2` FOREIGN KEY (`child_qlf_id`) REFERENCES `qualification` (`qlf_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sale`
--

DROP TABLE IF EXISTS `sale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sale` (
  `sal_id` int NOT NULL AUTO_INCREMENT,
  `customer` varchar(255) NOT NULL,
  `contact` varchar(255) DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `date` datetime NOT NULL,
  `value` decimal(19,2) NOT NULL,
  `first_payment` decimal(19,2) NOT NULL,
  `tax` decimal(19,7) NOT NULL,
  `portion` int NOT NULL,
  `payment_type` varchar(45) NOT NULL,
  `usr_id` int NOT NULL,
  PRIMARY KEY (`sal_id`),
  KEY `fk_user_sale_idx` (`usr_id`),
  CONSTRAINT `fk_user_sale` FOREIGN KEY (`usr_id`) REFERENCES `user` (`usr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sales_team`
--

DROP TABLE IF EXISTS `sales_team`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_team` (
  `slt_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`slt_id`),
  UNIQUE KEY `slt_id_UNIQUE` (`slt_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sales_team_seller`
--

DROP TABLE IF EXISTS `sales_team_seller`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_team_seller` (
  `slt_id` int NOT NULL,
  `sel_id` int NOT NULL,
  PRIMARY KEY (`slt_id`,`sel_id`),
  KEY `fk_sales_team_seller_seller` (`sel_id`),
  CONSTRAINT `fk_sales_team_seller_sales_team` FOREIGN KEY (`slt_id`) REFERENCES `sales_team` (`slt_id`),
  CONSTRAINT `fk_sales_team_seller_seller` FOREIGN KEY (`sel_id`) REFERENCES `seller` (`sel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seller`
--

DROP TABLE IF EXISTS `seller`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller` (
  `sel_id` int NOT NULL AUTO_INCREMENT,
  `per_id` int NOT NULL,
  `job_id` int NOT NULL,
  PRIMARY KEY (`sel_id`),
  KEY `fk_seller_person1_idx` (`per_id`),
  KEY `fk_seller_job_idx` (`job_id`),
  CONSTRAINT `fk_seller_job` FOREIGN KEY (`job_id`) REFERENCES `job` (`job_id`),
  CONSTRAINT `fk_seller_person1` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seller_agent`
--

DROP TABLE IF EXISTS `seller_agent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller_agent` (
  `sel_id` int NOT NULL,
  `agent_sel_id` int NOT NULL,
  PRIMARY KEY (`sel_id`,`agent_sel_id`),
  KEY `fk_seller_has_seller_seller2_idx` (`agent_sel_id`),
  KEY `fk_seller_has_seller_seller1_idx` (`sel_id`),
  CONSTRAINT `fk_seller_has_seller_seller1` FOREIGN KEY (`sel_id`) REFERENCES `seller` (`sel_id`),
  CONSTRAINT `fk_seller_has_seller_seller2` FOREIGN KEY (`agent_sel_id`) REFERENCES `seller` (`sel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seller_partner`
--

DROP TABLE IF EXISTS `seller_partner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller_partner` (
  `sel_id` int NOT NULL,
  `ptn_id` int NOT NULL,
  PRIMARY KEY (`sel_id`,`ptn_id`),
  KEY `fk_seller_has_partner_partner1_idx` (`ptn_id`),
  KEY `fk_seller_has_partner_seller1_idx` (`sel_id`),
  CONSTRAINT `fk_seller_has_partner_partner1` FOREIGN KEY (`ptn_id`) REFERENCES `partner` (`ptn_id`),
  CONSTRAINT `fk_seller_has_partner_seller1` FOREIGN KEY (`sel_id`) REFERENCES `seller` (`sel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `source`
--

DROP TABLE IF EXISTS `source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `source` (
  `src_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`src_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `state`
--

DROP TABLE IF EXISTS `state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `state` (
  `ste_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `abbreviation` varchar(2) NOT NULL,
  `cou_id` int NOT NULL,
  PRIMARY KEY (`ste_id`),
  KEY `idx_state_country` (`cou_id`),
  CONSTRAINT `fk_state_country` FOREIGN KEY (`cou_id`) REFERENCES `country` (`cou_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `usr_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(60) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `per_id` int NOT NULL,
  `acl_id` int DEFAULT NULL,
  `change_pass` tinyint(1) NOT NULL DEFAULT '0',
  `expire_pass` tinyint(1) NOT NULL DEFAULT '0',
  `pass_error_count` int NOT NULL DEFAULT '0',
  `forgot_key` varchar(50) DEFAULT NULL,
  `forgot_key_created` datetime DEFAULT NULL,
  `last_pass_change` datetime DEFAULT NULL,
  `blocked` tinyint(1) NOT NULL DEFAULT '0',
  `type_cla` int NOT NULL,
  `last_login` datetime DEFAULT NULL,
  `last_error_count` int DEFAULT NULL,
  `config` text,
  `cus_id` int DEFAULT NULL,
  PRIMARY KEY (`usr_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  KEY `idx_user_person` (`per_id`),
  KEY `idx_user_access_list` (`acl_id`),
  KEY `idx_user_type` (`type_cla`),
  KEY `fk_user_customer_idx` (`cus_id`),
  CONSTRAINT `fk_user_access_list` FOREIGN KEY (`acl_id`) REFERENCES `access_list` (`acl_id`),
  CONSTRAINT `fk_user_customer` FOREIGN KEY (`cus_id`) REFERENCES `customer` (`cus_id`),
  CONSTRAINT `fk_user_person` FOREIGN KEY (`per_id`) REFERENCES `person` (`per_id`),
  CONSTRAINT `fk_user_type` FOREIGN KEY (`type_cla`) REFERENCES `classifier` (`cla_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_customer`
--

DROP TABLE IF EXISTS `user_customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_customer` (
  `usr_id` int NOT NULL,
  `cus_id` int NOT NULL,
  PRIMARY KEY (`usr_id`,`cus_id`),
  KEY `idx_user_has_customer_customer` (`cus_id`),
  KEY `idx_user_has_customer_user` (`usr_id`),
  CONSTRAINT `fk_user_has_customer_customer` FOREIGN KEY (`cus_id`) REFERENCES `customer` (`cus_id`),
  CONSTRAINT `fk_user_has_customer_user` FOREIGN KEY (`usr_id`) REFERENCES `user` (`usr_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vehicle`
--

DROP TABLE IF EXISTS `vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle` (
  `vhe_id` int NOT NULL AUTO_INCREMENT,
  `chassi` varchar(45) DEFAULT NULL,
  `plate` varchar(45) NOT NULL,
  `model_year` int NOT NULL,
  `purchase_date` date DEFAULT NULL,
  `purchase_value` decimal(13,2) DEFAULT NULL,
  `mdl_id` int NOT NULL,
  PRIMARY KEY (`vhe_id`),
  UNIQUE KEY `plate_UNIQUE` (`plate`),
  KEY `fk_vehicle_model1_idx` (`mdl_id`),
  CONSTRAINT `fk_vehicle_model1` FOREIGN KEY (`mdl_id`) REFERENCES `model` (`mdl_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'carbon'
--

--
-- Dumping routines for database 'carbon'
--
/*!50003 DROP FUNCTION IF EXISTS `fnGetParentMenu` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fnGetParentMenu`(p_mnu_id INT) RETURNS text CHARSET latin1
    READS SQL DATA
BEGIN
DECLARE response TEXT;
SELECT base.menuPath into response FROM (
SELECT GROUP_CONCAT(
IF(
ISNULL(mnu.root_id), mnu.name, CONCAT(parent.name , ' > ' ,mnu.name) 
)
SEPARATOR '>') as menuPath
FROM menu mnu
LEFT JOIN menu parent FORCE INDEX (PRIMARY) ON (mnu.root_id = parent.mnu_id)
        WHERE mnu.mnu_id = p_mnu_id
) base;
    
RETURN response;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP FUNCTION IF EXISTS `fnMenu` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `fnMenu`(p_mnu_id INT) RETURNS text CHARSET latin1
    READS SQL DATA
BEGIN
DECLARE response TEXT;
SELECT base.menuPath into response FROM (
SELECT GROUP_CONCAT(
IF(
ISNULL(mnu.root_id), mnu.name, CONCAT(fnGetParentMenu(parent.mnu_id) , ' > ' ,mnu.name) 
)
            SEPARATOR '>') as menuPath
FROM menu mnu
LEFT JOIN menu parent FORCE INDEX (PRIMARY) ON (mnu.root_id = parent.mnu_id)
        WHERE mnu.mnu_id = p_mnu_id
) base;
    
RETURN response;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-07-12 10:17:19