

-- CRM-498 START
ALTER TABLE `carbon`.`proposal_payment` ADD COLUMN position INT NULL;
-- CRM-498 END

INSERT INTO `carbon`.`parameter` (prm_id, name, `value`, description ) VALUES (NULL,'PROPOSAL_DAYS_FOLLOW_UP','5','Número de dias sem acompanhamento.');
INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('MIN_FILE_UPLOAD_SIZE','1048576','Tamanho mínimo em bytes para upload de arquivo');

-- CRM-616 START

ALTER TABLE `carbon`.`proposal_detail` 
	DROP FOREIGN KEY `fk_proposal_detail_person1`,
	DROP FOREIGN KEY `fk_proposal_detail_person2`;

ALTER TABLE `carbon`.`proposal_detail` 
    DROP INDEX `fk_proposal_detail_person1_idx`,
	DROP INDEX `fk_proposal_detail_person2_idx`;

ALTER TABLE `carbon`.`proposal_detail`
	CHANGE COLUMN `seller_per_id` `sel_id` INT(11) NOT NULL,
	CHANGE COLUMN `intern_sale_per_id` `intern_sale_sel_id` INT(11) NULL DEFAULT NULL,
	ADD INDEX `fk_proposal_detail_internal_sale_idx` (`intern_sale_sel_id` ASC),
	ADD INDEX `fk_proposal_detail_seller_idx` (`sel_id` ASC);

ALTER TABLE `carbon`.`proposal_detail` 
	ADD CONSTRAINT `fk_proposal_detail_seller`
	  FOREIGN KEY (`sel_id`)
	  REFERENCES `carbon`.`seller` (`sel_id`),
	ADD CONSTRAINT `fk_proposal_detail_interal_sale`
	  FOREIGN KEY (`intern_sale_sel_id`)
	  REFERENCES `carbon`.`seller` (`sel_id`);

UPDATE `carbon`.proposal_detail pd SET pd.sel_id = ifnull((SELECT s.sel_id FROM carbon.seller s WHERE s.per_id = pd.sel_id), 1);
UPDATE `carbon`.proposal_detail pd SET pd.intern_sale_sel_id = (SELECT s.sel_id FROM carbon.seller s WHERE s.per_id = pd.intern_sale_sel_id);

ALTER TABLE `carbon`.`lead` 
	DROP FOREIGN KEY `fk_lead_person2`;

ALTER TABLE `carbon`.`lead` 
	CHANGE COLUMN `seller_per_id` `seller_id` INT(11) NOT NULL ,
	ADD INDEX `fk_lead_person2_idx` (`seller_id` ASC),
	DROP INDEX `fk_lead_person2_idx` ;

ALTER TABLE `carbon`.`lead` 
	ADD CONSTRAINT `fk_lead_person2`
	FOREIGN KEY (`seller_id`)
	REFERENCES `carbon`.`seller` (`sel_id`);

UPDATE `carbon`.`lead` l SET l.seller_id = ifnull((SELECT s.sel_id FROM `carbon`.`seller` s WHERE s.per_id = l.seller_id), 1);

ALTER TABLE `carbon`.`proposal_detail_vehicle_item` 
	DROP FOREIGN KEY `fk_proposal_detail_vehicle_item_person1`;

ALTER TABLE `carbon`.`proposal_detail_vehicle_item` 
	CHANGE COLUMN `seller_per_id` `seller_id` INT(11) NOT NULL ,
	ADD INDEX `fk_proposal_detail_vehicle_item_person1_idx` (`seller_id` ASC),
	DROP INDEX `fk_proposal_detail_vehicle_item_person1_idx` ;

ALTER TABLE `carbon`.`proposal_detail_vehicle_item` 
	ADD CONSTRAINT `fk_proposal_detail_vehicle_item_person1`
	FOREIGN KEY (`seller_id`)
	REFERENCES `carbon`.`seller` (`sel_id`);

UPDATE `carbon`.`proposal_detail_vehicle_item` pdvi SET pdvi.seller_id = ifnull((SELECT s.sel_id FROM `carbon`.`seller` s WHERE s.per_id = pdvi.seller_id), 1);

-- CRM-616 END

-- STARTCRM-629 

ALTER TABLE `carbon`.`proposal_commission` 
	CHANGE COLUMN `due_date` `due_date` DATETIME NULL;
	
-- END CRM-629

-- CRM-634 START

ALTER TABLE `carbon`.`product` 
ADD COLUMN `product_description` VARCHAR(255) NULL AFTER `proposal_expiration_days`;

-- CRM-634 END

-- END CRM-577

-- START CRM-577

INSERT INTO `carbon`.`checkpoint` (`name`, `description`)
VALUES ('PROPOSAL.VIEW.PROMPT.DELIVERY', 'APRESENTAR TODAS AS PROPOSTAS COM O PRONTA ENTREGA TRUE');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('COMMERCIAL_APPROVED','#129C11','Cor do satus da proposta aprovado comercial');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('COMMERCIAL_DISAPPROVED','#E30705','Cor do satus da proposta reprovador comercial');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('FINISHED_WITHOUT_SALE','#E30705','Cor do satus da proposta finalizada sem venda');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('FINISHED_WITH_SALE','#129C11','Cor do satus da proposta finalizada com venda');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('IN_COMMERCIAL_APPROVAL','#FFA700','Cor do satus da proposta em aprovação comercial');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('IN_PROGRESS','#aaaaaa','Cor do satus da proposta em andamento');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('ON_CUSTOMER_APPROVAL','#FFA700','Cor do satus da proposta em aprovação no cliente');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('CANCELED','#333333','Cor do satus da proposta cancelada'); 

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('CANCELED','#333333','Cor do satus da proposta cancelada');

INSERT INTO `carbon`.`parameter` (`name`,`value`,`description`) VALUES ('JIRA_TASK_GENERATION_INTEGRATION_CHASSI_ULTIMOS_NUMEROS','customfield_11293','Ultimos 6 digitos do Chassi do veículo');

-- END CRM-577


-- START CRM-574

UPDATE `checkpoint`
SET name = 'PROPOSAL.COMMERCIAL.APPROVAL.SALESTEAM'
WHERE name = 'PROPOSAL.COMMERCIAL.OPINION.SALESTEAM';

UPDATE `checkpoint`
SET name = 'PROPOSAL.COMMERCIAL.APPROVAL.ALL'
WHERE name = 'PROPOSAL.COMMERCIAL.OPINION.ALL';

-- END CRM-574
