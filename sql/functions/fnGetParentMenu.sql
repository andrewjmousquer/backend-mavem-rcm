DROP FUNCTION IF EXISTS fnGetParentMenu;

DELIMITER $$

CREATE  FUNCTION `fnGetParentMenu`(p_mnu_id INT) RETURNS text CHARSET latin1
    READS SQL DATA
BEGIN
DECLARE response TEXT;
SELECT base.menuPath into response FROM (
SELECT GROUP_CONCAT(
IF(
ISNULL(mnu.root_id), mnu.name, CONCAT(parent.name , ' > ' ,mnu.name) 
)
SEPARATOR '>') as menuPath
FROM menu mnu
LEFT JOIN menu parent FORCE INDEX (PRIMARY) ON (mnu.root_id = parent.mnu_id)
        WHERE mnu.mnu_id = p_mnu_id
) base;
    
RETURN response;
END$$

DELIMITER ;

