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
