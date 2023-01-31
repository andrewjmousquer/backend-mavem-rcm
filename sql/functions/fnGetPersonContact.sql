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
