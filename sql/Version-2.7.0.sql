-- CRM-675 START
DROP TABLE IF EXISTS `proposal_state_history`;
CREATE TABLE IF NOT EXISTS `proposal_state_history` (
  `psh_id` INT NOT NULL AUTO_INCREMENT,
  `pps_id` INT NOT NULL,
  `cla_id_old` INT NULL,
  `cla_id_new` INT NOT NULL,
  `sor_id` INT NULL,
  `usr_id` INT NOT NULL,
  `status_date` DATETIME NOT NULL,
  PRIMARY KEY (`psh_id`),
  INDEX `fk_proposal_state_proposal_idx` (`pps_id` ASC) VISIBLE,
  INDEX `fk_proposal_state_old_status_idx` (`cla_id_old` ASC) VISIBLE,
  INDEX `fk_proposal_state_new_status_idx` (`cla_id_new` ASC) VISIBLE,
  INDEX `fk_proposal_state_sales_order_idx` (`sor_id` ASC) VISIBLE,
  CONSTRAINT `fk_proposal_state_proposal` FOREIGN KEY (`pps_id`) REFERENCES `proposal` (`pps_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_state_old_status` FOREIGN KEY (`cla_id_old`) REFERENCES `classifier` (`cla_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_state_new_status` FOREIGN KEY (`cla_id_new`) REFERENCES `classifier` (`cla_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_proposal_state_sales_order` FOREIGN KEY (`sor_id`) REFERENCES `sales_order` (`sor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION)
ENGINE = InnoDB;


INSERT INTO parameter (name, `value`, description ) VALUES ('JASPER_URL','https://carbon.sbmtech.com.br/jasperserver','Jasper URL');
INSERT INTO parameter (name, `value`, description ) VALUES ('JASPER_USER','jasperadmin','Jasper user');
INSERT INTO parameter (name, `value`, description ) VALUES ('JASPER_PASSWORD','C@rbon2022','Jasper password');
INSERT INTO parameter (name, `value`, description ) VALUES ('PROPOSAL_REPORT_PATH','/reports/Proposta_Comercial.pdf','Proposal report path');
INSERT INTO parameter (name, `value`, description ) VALUES ('PROPOSAL_REPORT_PARAMS','p_proposal_number=','Parametros do report de proposal para envio ao jasper');
