
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
