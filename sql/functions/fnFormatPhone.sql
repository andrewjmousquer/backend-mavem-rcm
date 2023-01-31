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
