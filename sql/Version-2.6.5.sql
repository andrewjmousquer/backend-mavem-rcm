-- WELSON START
ALTER TABLE `carbon`.`proposal_detail`
ADD COLUMN `purchase_order_service` VARCHAR(50) NULL DEFAULT NULL AFTER `usr_id`;

ALTER TABLE `carbon`.`proposal_detail`
ADD COLUMN `purchase_order_product` VARCHAR(50) NULL DEFAULT NULL AFTER `purchase_order_service`;

ALTER TABLE `carbon`.`proposal_detail`
ADD COLUMN `purchase_order_documentation` VARCHAR(50) NULL DEFAULT NULL AFTER `purchase_order_product`;
-- WELSON END