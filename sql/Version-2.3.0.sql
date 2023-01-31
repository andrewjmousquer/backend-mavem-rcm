ALTER TABLE `carbon`.`audit` 
CHANGE COLUMN `details` `details` LONGTEXT NULL DEFAULT NULL ;

-- CRM-161 START

DELETE FROM classifier WHERE cla_id IN (225, 226, 227, 228, 229);

-- CRM-161 END


-- CRM-472 START

ALTER TABLE `carbon`.`proposal_approval_rule` 
DROP COLUMN `value_start`,
CHANGE COLUMN `value_end` `value` DECIMAL(13,2) NOT NULL ;

INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Alçada de Aprovação', 'proposal-approval-rule', 'Tela para cadastro de alçada de aprovação de propostas', 'fa fa-fw fa-thumbs-o-up', 8, null, 1);

-- CRM-472 END


-- CRM-499

CREATE TABLE IF NOT EXISTS `carbon`.`sales_order` (
  `sor_id` INT(11) NOT NULL AUTO_INCREMENT,
  `pps_id` INT(11) NOT NULL,
  `order_number` INT(11) NOT NULL,
  `jira_key` VARCHAR(10) NOT NULL,
  `status_cla_id` INT(11) NOT NULL,
  `usr_id` INT(11) NOT NULL,
  PRIMARY KEY (`sor_id`),
  INDEX `fk_sales_order_proposal_idx` (`pps_id` ASC) ,
  INDEX `fk_sales_order_status_idx` (`status_cla_id` ASC),
  INDEX `fk_sales_order_user_idx` (`usr_id` ASC) ,
  CONSTRAINT `fk_sales_order_proposal`
    FOREIGN KEY (`pps_id`)
    REFERENCES `carbon`.`proposal` (`pps_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_sales_order_status`
    FOREIGN KEY (`status_cla_id`)
    REFERENCES `carbon`.`classifier` (`cla_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_sales_order_user`
    FOREIGN KEY (`usr_id`)
    REFERENCES `carbon`.`user` (`usr_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_MARCA','customfield_11069','Marca do veículo');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_MODELO','customfield_11070','Modelo do veículo');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_ANO_MODELO','customfield_11071','Ano do modelo do veículo');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_PLACA','customfield_10253','Placa do veículo');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_CHASSI','customfield_10257','Chassi do veículo');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_DATA_DA_COMPRA','customfield_11072','Data da Compra do veículo');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_MARCA_MODELO','customfield_10032','Marca e Modelo do veículo');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_NUMERO_OS','customfield_10256','Numero da OS');

INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_INTEGRATION_USERNAME','demandasti@carbonblindados.com.br','Usuário para integração JIRA');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_INTEGRATION_PASSWORD','XBRBJSpzDXKE9RYCo4KJ2F93','Senha para integração JIRA');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_INTEGRATION_BASE_URI','https://carbonblindados.atlassian.net/rest/api/3','URI base integração JIRA Carbon');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_INTEGRATION_ISSUE_TYPE','10151','Tipo inicial de Tiket integração JIRA');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_INTEGRATION_PROJECT','10128','Projeto do Tiket integração JIRA');

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (255, 'VALIDATION_BACKOFFICE','SALES_ORDER_STATUS', 'EM VALIDAÇÃO BACKOFFICE', 'Lista de status de pedido de venda');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (256, 'DISAPPROVED_BACKOFFICE','SALES_ORDER_STATUS', 'CONTATADO', 'Lista de status de pedido de venda');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (257, 'APPROVED_BACKOFFICE','SALES_ORDER_STATUS', 'PEDIDO LIBERADO BACKOFFICE', 'Lista de status de pedido de venda');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (258, 'FINALIZED ORDER','SALES_ORDER_STATUS', 'PEDIDO FINALIZADO', 'Lista de status de pedido de venda');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (259, 'CANCELED','SALES_ORDER_STATUS', 'CANCELADO', 'Lista de status de pedido de venda');

DELIMITER || 
CREATE FUNCTION `fnGetOrderNumberSalesOrder`() RETURNS int READS SQL DATA
BEGIN
DECLARE response int;
SELECT IFNULL(ctrl.seq ,1)  into response  FROM (SELECT ( MAX( order_number ) + 1) AS seq FROM sales_order) AS ctrl;
RETURN response;
RETURN 1;
END ||
DELIMITER ;

-- CRM-499 END

-- CRM-540 START

INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('PROPOSAL_INITIAL_CODE_LETTER','A','Letra inicial para versionamento de proposta');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('PROPOSAL_NUMBER_FIXED_LETTER','B','Letra fixa para geração do número de OS');

ALTER TABLE `carbon`.`proposal` ADD COLUMN `proposal_number` VARCHAR(50) NOT NULL AFTER `pps_id`;

UPDATE proposal set cod = 'A' WHERE pps_id > 0;
UPDATE proposal set proposal_number = CONCAT('B', DATE_FORMAT(create_date, "%y%m"), '-', num, cod) WHERE pps_id > 0;

-- CRM-540 END

-- CRM-530 START
ALTER TABLE `carbon`.`vehicle`
CHANGE COLUMN `plate` `plate` VARCHAR(45) NULL DEFAULT NULL ;
-- CRM-530 END