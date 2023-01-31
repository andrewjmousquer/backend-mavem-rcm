
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
