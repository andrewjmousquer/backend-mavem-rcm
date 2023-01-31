-- CRM-645 START
ALTER TABLE `carbon`.`proposal_detail_vehicle`
ADD COLUMN `mdl_id` INT(11) NOT NULL AFTER `vhe_id`,
ADD COLUMN `version` VARCHAR(100) NULL DEFAULT NULL AFTER `mdl_id`,
ADD COLUMN `model_year` INT(11) NOT NULL AFTER `version`,
ADD INDEX `fk_proposal_detail_vehicle_model_idx` (`mdl_id` ASC);

ALTER TABLE `carbon`.`proposal_detail_vehicle` 
ADD CONSTRAINT `fk_proposal_detail_vehicle_model`
  FOREIGN KEY (`mdl_id`)
  REFERENCES `carbon`.`model` (`mdl_id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `carbon`.`vehicle` 
ADD COLUMN `version` VARCHAR(100) NULL DEFAULT NULL AFTER `plate`;
-- CRM-645 END


ALTER TABLE `carbon`.`product`
ADD COLUMN `product_description` VARCHAR(255) NULL AFTER `proposal_expiration_days`;



drop function if exists fnFormatMonthBr;
DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `fnFormatMonthBr`(p_date datetime) RETURNS tinytext CHARSET latin1
    READS SQL DATA
BEGIN

DECLARE p_month_fomated tinytext DEFAULT "";

case month(p_date) 
	when 1 then set p_month_fomated = 'janeiro';
    when 2 then set p_month_fomated = 'fevereiro';
    when 3 then set p_month_fomated = 'março';
    when 4 then set p_month_fomated = 'abril';
    when 5 then set p_month_fomated = 'maio';
    when 6 then set p_month_fomated = 'junho';
    when 7 then set p_month_fomated = 'julho';
    when 8 then set p_month_fomated = 'agosto';
    when 9 then set p_month_fomated = 'setembro';
    when 10 then set p_month_fomated = 'outubro';
    when 11 then set p_month_fomated = 'novembro';
    when 12 then set p_month_fomated = 'dezembro';
end case;

RETURN p_month_fomated;

END$$
DELIMITER ;

drop function if exists fnFormatPhone;
DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `fnFormatPhone`(p_phone varchar(25)) RETURNS tinytext CHARSET latin1
    READS SQL DATA
BEGIN

DECLARE p_phone_fomated tinytext DEFAULT "";

set p_phone_fomated = p_phone;

if length(p_phone) > 4 and length(p_phone) < 10  then set p_phone_fomated = INSERT( p_phone, length(p_phone) - 3, 0, '-' ); end if;
if length(p_phone) = 10 then set p_phone_fomated = INSERT( INSERT( INSERT( p_phone, 7, 0, '-' ), 3, 0, ') ' ), 1, 0, '(' ); end if;
if length(p_phone) = 11 then set p_phone_fomated = INSERT( INSERT( INSERT( p_phone, 8, 0, '-' ), 3, 0, ') ' ), 1, 0, '(' ); end if;

RETURN p_phone_fomated;

END$$
DELIMITER ;



DROP FUNCTION IF EXISTS fnGetCustomerPriceList;
DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `fnGetCustomerPriceList`(p_mdl_id int, p_prd_id int, p_model_year int ) RETURNS decimal(10,2)
    READS SQL DATA
BEGIN

DECLARE customerPriceList decimal(10,2) DEFAULT 0;

set customerPriceList := (
select max(ppr.price)
from price_list prl
inner join price_product ppr
	on ppr.prl_id = prl.prl_id
inner join product_model prm
	on prm.prm_id = ppr.prm_id

where 	prm.mdl_id = p_mdl_id
	and prm.prd_id = p_prd_id
    and prm.model_year_start <= p_model_year 
    and prm.model_year_end >= p_model_year 
	and prl.chn_id = 3
	and prl.start_date <= now() 
	and prl.end_date >= now());


RETURN customerPriceList;
END$$
DELIMITER ;


drop function if exists fnGetPersonContact;
DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `fnGetPersonContact`(p_per_id INT, p_contact_cla_id varchar(21)) RETURNS text CHARSET latin1
    READS SQL DATA
BEGIN

DECLARE finished INTEGER DEFAULT 0;
DECLARE contactlList TEXT DEFAULT "";
DECLARE contact TEXT DEFAULT "";

	-- declare cursor for person email
	DEClARE curContact 
		CURSOR FOR 
			select if(c.type_cla in (13,14),fnFormatPhone(c.`value`),lower(c.`value`))
			from contact c 
			where 	p_contact_cla_id like concat("%",c.type_cla,"%")-- type_cla = email
				and c.per_id = p_per_id;

	-- declare NOT FOUND handler
	DECLARE CONTINUE HANDLER 
        FOR NOT FOUND SET finished = 1;

	OPEN curContact;

	getContact: LOOP
		FETCH curContact INTO contact;
		IF finished = 1 THEN 
			LEAVE getContact;
		END IF;
		-- build email list
		SET contactlList = CONCAT(contactlList, if(contactlList="","",";"), contact);
	END LOOP getContact;
	CLOSE curContact;

RETURN contactlList;
END$$
DELIMITER ;

DELIMITER $$
DROP VIEW IF EXISTS vw_proposta_com;
CREATE ALGORITHM=UNDEFINED DEFINER=`portal`@`localhost` SQL SECURITY DEFINER 

VIEW `vw_proposta_com` AS 

select 
`pps`.`proposal_number` AS `proposal_number`,
`pps`.`pps_id` as `pps_id`,
`pps`.`create_date` AS `create_date`,
fnFormatMonthBr(`pps`.`create_date`) AS `create_month_br`,
`per_cus`.`name` AS `cus_name`,
fnGetPersonContact(per_cus.per_id, '13,14') as `cus_phone`,
fnGetPersonContact(per_cus.per_id, '15') as `cus_email`,
`per_sel`.`name` AS `sel_name`,
fnGetPersonContact(per_sel.per_id, '13,14') as `sel_phone`,
fnGetPersonContact(per_sel.per_id, '15') as `sel_email`,

`prd`.`name` AS `product_name`,
`mdl`.`name` AS `model_name`,
`brd`.`name` AS `brand_name`,
`vhe`.`model_year` AS `model_year`,
`pdv`.`agreed_term_days` AS `term_days`,

`pdv`.`product_final_price` + `pdv`.`product_amount_discount` AS `armour_price`,
`pdv`.`product_amount_discount` AS `armour_discount`,
`pdv`.`over_price` AS `over_price`,
`pdv`.`over_price_partner_discount_amount` AS `over_discount`,
`pdv`.`price_discount_amount` AS `carbon_discount`,
`pdv`.`total_amount` AS `subtotal_price`,
`pdv`.`total_tax_amount` AS `tax_price`,
`pdv`.`total_amount` + `pdv`.`total_tax_amount` as `total_price`,
(	select sum(ppi_p.item_price) 
	from vw_proposta_com_item ppi_p
    where ppi_p.pps_id = pps.pps_id) AS `item_price`,
(	select sum(ppi_d.item_discount) 
	from vw_proposta_com_item ppi_d
    where ppi_d.pps_id = pps.pps_id) AS `item_discount`

from

`proposal` `pps` 

left join `proposal_person_client` `ppc` 
	on	`ppc`.`pps_id` = `pps`.`pps_id`
    and `ppc`.`customer_cla_id` = 181
    
left join `person` `per_cus` 
	on`per_cus`.`per_id` = `ppc`.`per_id`
    
left join `proposal_detail` `ppd` 
	on`ppd`.`pps_id` = `pps`.`pps_id`
    
left join `seller` `sel`
	on `sel`.`sel_id` = `ppd`.`sel_id`
    
left join `person` `per_sel` 
	on`per_sel`.`per_id` = `sel`.`per_id`
    
left join `proposal_detail_vehicle` `pdv` 
	on`pdv`.`ppd_id` = `ppd`.`ppd_id` 
    
left join `price_product` `ppr` 
	on`ppr`.`ppr_id` = `pdv`.`ppr_id`
    
left join `product_model` `prm` 
	on`prm`.`prm_id` = `ppr`.`prm_id`
    
left join `product` `prd` 
	on`prd`.`prd_id` = `prm`.`prd_id`
    
left join `model` `mdl` 
	on`mdl`.`mdl_id` = `prm`.`mdl_id`

left join `brand` `brd` 
	on`brd`.`brd_id` = `mdl`.`brd_id`
    
left join `vehicle` `vhe` 
	on`vhe`.`vhe_id` = `pdv`.`vhe_id`;
END$$
DELIMITER ;	
	
	
DELIMITER $$
DROP VIEW IF EXISTS vw_proposta_com_financ;
CREATE ALGORITHM=UNDEFINED DEFINER=`portal`@`localhost` SQL SECURITY DEFINER 

VIEW `vw_proposta_com_financ` AS 

select 
`pps`.`proposal_number` AS `proposal_number`,
`pps`.`pps_id` as `pps_id`,
`pym`.`name` as `method`,
`pyr`.`name` as `condition`,
`ppy`.`payment_amount` as `payment_amount`,
`ppy`.`installment_amount` as `installment_amount`,
`ppy`.`interest` as `interest`,
`ppy`.`due_date` as `due_date`,
`ppy`.`pre_approved` as `pre-approved`,
`ppy`.`payer_cla_id` as `payer_cla_id`,
`cla_pay`.`label` as `payer`,
`ppy`.`event_cla_id` as  `event_cla_id`,
`cla_eve`.`label` as `event`


from

`proposal` `pps` 
    
left join `proposal_detail` `ppd` 
	on`ppd`.`pps_id` = `pps`.`pps_id`
    
left join `proposal_payment` `ppy` 
	on`ppy`.`ppd_id` = `ppd`.`ppd_id` 
  
left join `payment_method` `pym` 
	on`pym`.`pym_id` = `ppy`.`pym_id` 
  
left join `payment_rule` `pyr` 
	on`pyr`.`pyr_id` = `ppy`.`pyr_id`
    
left join `classifier` cla_eve
	on `cla_eve`.`cla_id` = `ppy`.`event_cla_id`
    
left join `classifier` cla_pay
	on `cla_pay`.`cla_id` = `ppy`.`payer_cla_id`

END$$
DELIMITER ;
	

DELIMITER $$

DROP VIEW IF EXISTS vw_proposta_com_item;
CREATE ALGORITHM=UNDEFINED DEFINER=`portal`@`localhost` SQL SECURITY DEFINER 

VIEW `vw_proposta_com_item` AS 

select 
`pps`.`proposal_number` AS `proposal_number`,
`pps`.`pps_id` as `pps_id`,
`itm`.`name` as `item_name`,
`pdvi`.`final_price` + `pdvi`.`amount_discount` as `item_price`,
`pdvi`.`amount_discount` as `item_discount`

from

`proposal` `pps` 
    
left join `proposal_detail` `ppd` 
	on`ppd`.`pps_id` = `pps`.`pps_id`
    
left join `proposal_detail_vehicle` `pdv` 
	on`pdv`.`ppd_id` = `ppd`.`ppd_id` 
  
left join `proposal_detail_vehicle_item` `pdvi` 
	on`pdvi`.`pdv_id` = `pdv`.`pdv_id` 
  
left join `price_item` `pci` 
	on`pci`.`pci_id` = `pdvi`.`pci_id`
    
left join `price_item_model` `pim` 
	on`pim`.`pim_id` = `pdvi`.`pim_id`
    
left join `item` `itm` 
	on`itm`.`itm_id` = coalesce(`pci`.`itm_id`,`pim`.`itm_id`);
END$$
DELIMITER ;

-- CRM-647 START
ALTER TABLE `carbon`.`person` 
ADD COLUMN `ie` VARCHAR(45) NULL AFTER `negative_list_cla_id`;
-- CRM-647 END

ALTER TABLE `carbon`.`proposal` DROP COLUMN `order_number`;

-- CRM-679 START

DROP FUNCTION IF EXISTS fnGetUserHasCheckpoint;
DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `fnGetUserHasCheckpoint`(p_usr_id int, p_checkpoint varchar(150) ) RETURNS BOOLEAN
    READS SQL DATA
BEGIN

DECLARE p_hasCheckpoint BOOLEAN;


set p_hasCheckpoint := (select true
						from `user` usr
						inner join access_list acl
							on usr.acl_id = acl.acl_id
						inner join access_list_checkpoint acc
							on acc.acl_id = acl.acl_id
						inner join checkpoint ckp
							on ckp.ckp_id = acc.ckp_id
						where ckp.`name` = p_checkpoint
                        and usr.usr_id = p_usr_id);

set p_hasCheckpoint := coalesce(p_hasCheckpoint,false) ;

RETURN p_hasCheckpoint;

END$$
DELIMITER ;

-- CRM-679 END