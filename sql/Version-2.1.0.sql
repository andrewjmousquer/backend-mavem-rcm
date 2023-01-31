--
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Calculadora', null, 'Calculadora', 'fa fa-fw fa-calculator', 8, null, 1);

--

-- CRM-439 START
UPDATE `carbon`.`menu` SET `name` = 'Executivo de Negócio'   WHERE (`mnu_id` = '26');
-- CRM-439 END

-- CRM-442 START
UPDATE `carbon`.`menu` SET `name` = 'Tabela de Preços' WHERE (`mnu_id` = '24');
-- CRM-442 END

-- CRM-440 START
UPDATE `carbon`.`menu` SET `name` = 'Meio de Pagamento' WHERE (`mnu_id` = '22');
-- CRM-440 END

-- CRM-441 START
UPDATE `carbon`.`menu` SET `name` = 'Condição de Pagamento' WHERE (`mnu_id` = '21');
-- CRM-441 END

-- CRM-442 START

-- PARTNER_SITUATION 210 até 215
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (210, 'ATIVO','PARTNER_SITUATION', 'ATIVO', 'Situação do Parceiro Ativo');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (211, 'INATIVO','PARTNER_SITUATION', 'INATIVO', 'Situação do Parceiro Inativo');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (212, 'PROSPECÇÃO','PARTNER_SITUATION', 'PROSPECÇÃO', 'Situação do Parceiro Prospecção');

ALTER TABLE `carbon`.`partner` 
DROP COLUMN `active`,
ADD COLUMN `situation_cla` INT(11) NOT NULL AFTER `chn_id`,
ADD INDEX `fk_partiner_situation_idx` (`situation_cla` ASC) VISIBLE;

UPDATE `carbon`.`partner` set situation_cla = 210 where ptn_id > 0;

-- CRM-442 END


-- CRM-115 START

ALTER TABLE `qualification`
ADD COLUMN `required` TINYINT(1) NOT NULL DEFAULT 0 AFTER `seq`,
ADD COLUMN `active` TINYINT(1) NOT NULL DEFAULT 1 AFTER `required`;

INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Qualificações', 'qualification-register-form', 'Cadastro de Qualificações', 'fa fa-file-text', 8, null, 1);

-- CRM-115 END


-- CRM-467 START

ALTER TABLE `carbon`.`partner` 
CHARACTER SET = utf8 , COLLATE = utf8_general_ci ,
ADD COLUMN `additional_term` INT(11) NOT NULL DEFAULT 0 AFTER `situation_cla`;

-- CRM-467 END



-- CRM-430 START

ALTER TABLE `carbon`.`brand`
DROP COLUMN `cod_fipe`;

ALTER TABLE `carbon`.`model`
DROP COLUMN `cod_fipe`;

-- CRM-430 END

-- CRM-450 START

ALTER TABLE `carbon`.`person` 
ADD COLUMN `corporate_name` VARCHAR(255) NULL DEFAULT NULL AFTER `name`;

-- CRM-450 END


-- CRM-337 START
ALTER TABLE `carbon`.`person` 
ADD COLUMN `negative_list_cla_id` INT(11) NULL AFTER `classification_cla_id`;

ALTER TABLE `carbon`.`person` 
ADD CONSTRAINT `fk_negative_list_classifier`
  FOREIGN KEY (`negative_list_cla_id`)
  REFERENCES `carbon`.`classifier` (`cla_id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
  
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (220, 'SPC','NEGATIVE_LIST', 'ATIVO', 'Lista negativa de pessoa');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (221, 'COFINS','NEGATIVE_LIST', 'ATIVO', 'Lista negativa de pessoa');
  
-- CRM-337 END
 	