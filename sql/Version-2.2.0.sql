
-- CRM-161 START
DELETE FROM classifier WHERE cla_id IN (225, 226, 227, 228, 229);

ALTER TABLE `carbon`.`lead` 
DROP FOREIGN KEY `fk_lead_classitier2`,
DROP FOREIGN KEY `fk_lead_brand1`;

ALTER TABLE `carbon`.`lead` 
DROP COLUMN `brd_id`,
DROP COLUMN `notes`,
DROP COLUMN `end_date`,
ADD COLUMN `name` VARCHAR(255) NOT NULL AFTER `led_id`,
ADD COLUMN `email` VARCHAR(150) NULL DEFAULT NULL AFTER `create_date`,
ADD COLUMN `phone` VARCHAR(150) NULL DEFAULT NULL AFTER `email`,
ADD COLUMN `description` TEXT NULL DEFAULT NULL AFTER `subject`,
CHANGE COLUMN `seller_per_id` `seller_per_id` INT(11) NOT NULL AFTER `phone`,
CHANGE COLUMN `sale_probability_cla_id` `sale_probability_cla_id` INT(11) NOT NULL AFTER `status_cla_id`,
CHANGE COLUMN `client_per_id` `client_per_id` INT(11) NULL DEFAULT NULL ,
CHANGE COLUMN `subject` `subject` VARCHAR(150) NULL DEFAULT NULL ,
ADD INDEX `fk_probability_idx` (`sale_probability_cla_id` ASC),
DROP INDEX `fk_lead_classitier2_idx` ,
DROP INDEX `fk_lead_brand1_idx` ;

ALTER TABLE `carbon`.`lead` 
DROP FOREIGN KEY `fk_lead_person1`;

ALTER TABLE `carbon`.`lead` ADD CONSTRAINT `fk_lead_person1`
  FOREIGN KEY (`client_per_id`)
  REFERENCES `carbon`.`person` (`per_id`)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT,
ADD CONSTRAINT `fk_probability`
  FOREIGN KEY (`sale_probability_cla_id`)
  REFERENCES `carbon`.`classifier` (`cla_id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (225, 'ABERTO','LEAD_STATUS', 'ABERTO', 'Lista de status de lead');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (226, 'CONTATADO','LEAD_STATUS', 'CONTATADO', 'Lista de status de lead');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (227, 'CANCELADO','LEAD_STATUS', 'CANCELADO', 'Lista de status de lead');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (228, 'CONVERTIDO','LEAD_STATUS', 'CONVERTIDO', 'Lista de status de lead');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (229, 'NAO_CONVERTIDO','LEAD_STATUS', 'NÃO CONVERTIDO', 'Lista de status de lead');

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (235, 'ALTA','LEAD_PROBABILITY', 'ALTA', 'Lista de probabilidade de conversao de lead');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (236, 'MEDIA','LEAD_PROBABILITY', 'MEDIA', 'Lista de probabilidade de conversao de lead');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (237, 'BAIXA','LEAD_PROBABILITY', 'BAIXA', 'Lista de probabilidade de conversao de lead');

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (240, 'EMAIL','MEDIA_CONTACT', 'EMAIL', 'Lista de contatos de media');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (241, 'FACEBOOK','MEDIA_CONTACT', 'FACEBOOK', 'Lista de contatos de media');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (242, 'INSTAGRAM','MEDIA_CONTACT', 'INSTAGRAM', 'Lista de contatos de media');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (243, 'TELEFONE','MEDIA_CONTACT', 'TELEFONE', 'Lista de contatos de media');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (244, 'WHATS_APP','MEDIA_CONTACT', 'WHATS APP', 'Lista de contatos de media');

-- CRM-161 END


-- CRM-433 START
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (1,'PROPOSAL.CREATE.OWNER','Permite criar uma nova proposta com seu usuário no campo Executivo de Negócio');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (2,'PROPOSAL.CREATE.PREPOSTO','Permite criar uma nova proposta preenchendo o campo Executivo de Negócio com qualquer usuário Executivo de Negócio para o qual o usuário atual está associado como Preposto');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (3,'PROPOSAL.CREATE.TEAM','Permite criar uma nova proposta preenchendo o campo Executivo de Negócio com qualquer usuário Executivo de Negócio para o qual o usuário atual está associado na mesma Célula de Venda');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (4,'PROPOSAL.CREATE.ALL','Permite criar uma nova proposta preenchendo o campo Executivo de Negócio com qualquer usuário Executivo de Negócio');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (5,'PROPOSAL.VIEW.OWNER','Visualiza propostas em que o usuário está associado no campo Executivo de Conta');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (6,'PROPOSAL.VIEW.READY.DELIVERY','Visualiza propostas em que o campo Pronta Entrega esteja habilitado (true) independente do usuário estar associado no campo Executivo de Conta');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (7,'PROPOSAL.VIEW.PREPOSTO','Visualiza propostas em que o usuário está associado como Preposto ao usuário associado no campo Executivo de Conta');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (8,'PROPOSAL.VIEW.TEAM','Visualiza propostas em que o usuário está associado a mesma Célula de Venda que o usuário associado no campo Executivo de Negócio');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (9,'PROPOSAL.VIEW.ALL','Visualiza todas as proposta cadastradas no sistema');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (10,'PROPOSAL.EDIT.OWNER','Permite editar propostas em que o usuário está associado no campo Executivo de Conta');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (11,'PROPOSAL.EDIT.PREPOSTO','Permite editar propostas em que o usuário está associado como Preposto ao usuário associado no campo Executivo de Conta');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (12,'PROPOSAL.EDIT.TEAM','Permite editar propostas em que o usuário está associado a mesma Célula de Venda que o usuário associado no campo Executivo de Negócio');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (13,'PROPOSAL.EDIT.ALL','Permite editar todas as proposta cadastradas no sistema');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (14,'PROPOSAL.EDIT.SELLER.ALL','Permite editar o campo Executivo de Conta para qualquer usuário Executivo de Negócio');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (15,'PROPOSAL.EDIT.SELLER.PREPOSTO','Permite editar o campo Executivo de Conta para qualquer usuário Executivo de Negócio que esteja associado como Preposta ao usuário atual');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (16,'PROPOSAL.EDIT.DELIVERY.DATE','Altera Prazo De Entrega (Produção)');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (17,'PROPOSAL.EDIT.CONTRACT.DATE','Altera Prazo Contratual (Prazo Acordado)');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (18,'PROPOSAL.EDIT.EXTERNAL.COMISSION.DATE','Preenche/Altera Data De Vencimento Das Comissões Externas');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (19,'PROPOSAL.EDIT.EXTERNAL.COMISSION.VALUE','Altera Valor Padrão Da Comissão Carregado Na Proposta');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (21,'PROPOSAL.EDIT.EXPIRATION.DATE','ALTERA DATA DE VALIDADE DA PROPOSTA');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (22,'PROPOSAL.EDIT.EXPIRATION.DATE.EXPIRED.PAYMENT','ALTERA DATA DE VALIDADE DA PROPOSTA MESMO QUE A TABELA DE PREÇOS OU COND. DE PAGAMENTO JÁ SEJAM EXPIRADAS');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (23,'PROPOSAL.COMMERCIAL.OPINION.SALESTEAM','APLICA PARECER COMERCIAL NAS PROPOSTAS DA SUA CÉLULA DE VENDAS');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (24,'PROPOSAL.COMMERCIAL.OPINION.ALL','APLICA PARECER COMERCIAL EM TODAS AS PROPOTAS');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (25,'PROPOSAL.EDIT.PAYMENT-','EDITA PRIMEIRA LINHA DA CONDIÇÃO DE PAGAMENTO');
INSERT INTO `carbon`.`checkpoint` (ckp_id, name, description) VALUES (27,'VEHICLE.CREATE','PERMITE USUÁRIO CRIAR UM NOVO VEÍCULO');
-- CRM-433 END


-- CRM-470 START
ALTER TABLE `carbon`.`proposal_detail` 
ADD COLUMN `usr_id` INT(11) NULL AFTER `ptn_id`,
ADD INDEX `fk_propossal_detail_usr_idx` (`usr_id` ASC);
-- CRM-470 END


-- CRM-417 START

ALTER TABLE `carbon`.`item` ADD COLUMN `file` VARCHAR(255) NULL DEFAULT NULL AFTER `itt_id`;

-- CRM-417 END	


-- CRM-438 START

ALTER TABLE `carbon`.`item` 
CHARACTER SET = utf8 , COLLATE = utf8_general_ci ,
DROP COLUMN `photo_url`,
DROP COLUMN `label`,
ADD COLUMN `responsability_cla_id` INT(11) NOT NULL AFTER `hyperlink`,
ADD COLUMN `term` INT(11) NOT NULL AFTER `responsability_cla_id`,
ADD COLUMN `term_work_day` TINYINT(4) NOT NULL AFTER `term`,
ADD COLUMN `highlight` TINYINT(4) NOT NULL AFTER `term_work_day`,
ADD INDEX `fk_item_responsability_idx` (`responsability_cla_id` ASC);

UPDATE `carbon`.`item` SET  responsability_cla_id = 250;

ALTER TABLE `carbon`.`item` 
ADD CONSTRAINT `fk_item_reponsability`
  FOREIGN KEY (`responsability_cla_id`)
  REFERENCES `carbon`.`classifier` (`cla_id`)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT;

INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (250, 'PRODUCTION','ITEM_RESPONSABILITY', 'PRODUÇÃO', 'Responsável pelo Item');
INSERT INTO `carbon`.`classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (251, 'ASSISTANCE','ITEM_RESPONSABILITY', 'ASSISTÊNCIA', 'Responsável pelo Item');


-- CRM-438 END

UPDATE `carbon`.`classifier` SET `value` = 'OPTIONAL', `label` = 'OPCIONAL' WHERE (`cla_id` = '24');

INSERT INTO `parameter` (`name`,`value`,`description`) VALUES ('MAX_FILE_UPLOAD_SIZE','5242880','Tamanho máximo em bytes para upload de arquivo');

-- CRM-438 END
