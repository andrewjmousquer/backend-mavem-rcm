
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
	on`itm`.`itm_id` = coalesce(`pci`.`itm_id`,`pim`.`itm_id`)
    ;
