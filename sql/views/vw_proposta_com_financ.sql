
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
    
    ;
