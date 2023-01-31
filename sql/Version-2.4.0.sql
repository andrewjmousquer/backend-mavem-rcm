INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (180, 'DECISOR','CUSTOMER_TYPE', 'DECISOR', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (182, 'INFLUENCIADOR','CUSTOMER_TYPE', 'INFLUENCIADOR', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (183, 'USUARIO','CUSTOMER_TYPE', 'USUARIO', NULL );

UPDATE proposal set proposal_number = CONCAT('B', DATE_FORMAT(create_date, "%y%m"), '-', num, cod) WHERE pps_id > 0;

INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Lead', 'lead-form', 'Cadastro de lead', 'fa fa-fw fa-list', 8, null, 1);

ALTER TABLE `carbon`.`proposal_fup` 
CHANGE COLUMN `comment` `comment` VARCHAR(255) NULL ;


-- CRM-530 START
ALTER TABLE `carbon`.`vehicle`
CHANGE COLUMN `plate` `plate` VARCHAR(45) NULL DEFAULT NULL ;
-- CRM-530 END

-- CRM-498 START
INSERT INTO `carbon`.`checkpoint` (`name`, `description`)
VALUES ('PROPOSAL.EDIT.PAYMENT.MAIN', 'PERMITE EDITAR EVENTO DA PRIMEIRA LINHA DE DADOS FINANCEIROS DA PROSPOTA');

INSERT INTO `carbon`.`parameter` (`name`, `value`, `description`)
VALUES ('PROPOSAL_PAYMENT_MAIN_DAYS', '10', 'LIMITE DE DIAS PERMITIDO PARA DATA PACTUADA DA PRIMEIRA LINHA DE DADOS FINANCEIROS DA PROPOSTA');

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`)
VALUES (260, 'CHECKIN', 'EVENT_TYPE', 'CHECK-IN', null);

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`)
VALUES (261, 'CHECKOUT', 'EVENT_TYPE', 'CHECK-OUT', null);

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`)
VALUES (262, 'AUTORIZACAOODEFATURAMENTOBANCARIO', 'EVENT_TYPE', 'AUTORIZAÇÃO DE FATURAMENTO BANCÁRIO', null);

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`)
VALUES (263, 'SEMCONTASARECEBER', 'EVENT_TYPE', 'SEM CONTAS A RECEBER', null);

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`)
VALUES (264, 'PERMUTA', 'EVENT_TYPE', 'PERMUTA', null);

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`)
VALUES (265, 'BONIFICACAO', 'EVENT_TYPE', 'BONIFICAÇÃO', null);

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`)
VALUES (266, 'EXPORTACAO', 'EVENT_TYPE', 'EXPORTAÇÃO ', null);

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`)
VALUES (267, 'DATAFIXA', 'EVENT_TYPE', 'DATA FIXA', null);

UPDATE carbon.proposal_payment SET event_cla_id = 267 WHERE event_cla_id = 150;

DELETE FROM `carbon`.`classifier` where `cla_id` = 150;

ALTER TABLE `carbon`.`proposal_payment` DROP INDEX `ukProposalPaymentMethod`;
ALTER TABLE `carbon`.`proposal_payment` MODIFY due_date DATETIME NULL DEFAULT NULL;
-- CRM-498 END