-- ------------------
-- CLASSIFIERS
-- 
-- Para não quebrar as FKs antigas, vamos manter os inserts dos classifiers para a primeira versão 
-- dentro do 2-basic_inserts.sql.
-- ------------------



START TRANSACTION;

INSERT INTO payment_method (pym_id, name, active) VALUES (NULL, 'Cartão de Crédito', 1);
INSERT INTO payment_method (pym_id, name, active) VALUES (NULL, 'Boleto', 1);
INSERT INTO payment_method (pym_id, name, active) VALUES (NULL, 'Transferência', 1);

COMMIT;

START TRANSACTION;

INSERT INTO channel (chn_id, name) VALUES (NULL, 'Concessionária');
INSERT INTO channel (chn_id, name) VALUES (NULL, 'Direto');
INSERT INTO channel (chn_id, name) VALUES (NULL, 'Montadora');
INSERT INTO channel (chn_id, name) VALUES (NULL, 'Intermediário');
INSERT INTO channel (chn_id, name) VALUES (NULL, 'Locadora');
INSERT INTO channel (chn_id, name) VALUES (NULL, 'Montadora');

COMMIT;


INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (126, 'DECISOR','CUSTOMER_ROLE', 'Decisor', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (127, 'FINANCIADOR','CUSTOMER', 'Financiador', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (128, 'INFLUENCIADOR','CUSTOMER', 'Influenciador', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (129, 'USUARIO','CUSTOMER', 'Usuário', NULL );


-- ===============================
-- MENUS
-- ===============================
START TRANSACTION;

INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Marca', 'brand-form', 'Cadastro de Marcas', 'fa fa-fw fa-object-group', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Modelo', 'model-form', 'Cadastro de Modelos', 'fa fa-fw fa-car', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Banco', 'bank-form', 'Cadastro de Bancos', 'fa fa-fw fa-bank', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Canal', 'channel-form', 'Cadastro de Canais', 'fa fa-fw fa-briefcase', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Produto', 'product-form', 'Cadastro de Produtos', 'fa fa-product-hunt', 8, null); 
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Tipo de Item', 'itemtype-form', 'Tipo de Item', 'fa fa-fw fa-tag', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Origem', 'source-form', 'Cadastro de Origens', 'fa fa-circle-o', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Parcelas', 'portion-form', 'Cadastro de Parcelas', 'fa fa-fw fa-usd', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Histórico', 'history-form', 'Histórico de Parcelas', 'fa fa-fw fa-history', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Qualificação', 'qualification-form', 'Cadastro de Qualificações', 'fa fa-check-square', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Veículos', 'vehicle-form', 'Cadastro de Veículos', 'fa fa-fw fa-car', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Item', 'item-form', 'Cadastro de Item', 'fa fa-barcode', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Meio de Pagamento', 'paymentrule-form', 'Cadastro de Meio de Pagamento', 'fa fa-credit-card', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Método de Pagamento', 'paymentmethod-form', 'Menu para acessar a tela de cadastro de metodo de pagamento', 'fa fa-fw fa-credit-card', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Célula de Vendas', 'salesteam-form', 'Menu para acessar a tela de célula de venda', 'fa fa-fw fa-bell ', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Lista de Preço', 'price-list-form', 'Cadastro de Lista de Preço', 'fa fa-fw fa-list-ul', '8', null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Proposta', 'proposal-search', 'Pesquisa de propostas', 'fa fa-file-text', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Executivo de Negócio', 'seller-form', 'Executivo de Negócio', 'fa fa-briefcase', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Grupo de Parceiros', 'partner-group-form', 'Grupo de Parceiros', 'fa fa-fw fa-users', 7, null); 
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Parceiro', 'partner-form', 'Parceiro', 'fa fa-fw fa-user', 8, null);
INSERT INTO menu (name, url, description, icon, type_cla, root_id) VALUES ('Pessoa', 'person-form', 'Pessoa', 'fa fa-fw fa-user-o', 8, null);


COMMIT;


START TRANSACTION;
INSERT INTO `job` (`job_id`, `name`) VALUES (1, 'Cliente',1);
INSERT INTO `job` (`job_id`, `name`) VALUES (2, 'Vendedor',2);
INSERT INTO `job` (`job_id`, `name`) VALUES (3, 'Supervisor',3);
INSERT INTO `job` (`job_id`, `name`) VALUES (4, 'Gerente',4);
INSERT INTO `job` (`job_id`, `name`) VALUES (5, 'Diretor',5);

COMMIT;

START TRANSACTION;

INSERT INTO parameter (prm_id, name, `value`, description ) VALUES (NULL,'DOCUMENT_ALLOWED_EXTENSIONS','pdf,xls,xlsx,doc,png,jpg,jpeg','Lista de extensões permitida para o upload de documentos. Necessário separar por vírgula (,)');
INSERT INTO parameter (prm_id, name, `value`, description ) VALUES (NULL,'DOCUMENT_ALLOWED_MIME_TYPES','application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/msword,text/plain,image/png,image/jpeg','Lista de extensões permitida para o upload de documentos. Necessário separar por vírgula (,)');

COMMIT;

-- ===============================
-- FUNCTIONS
-- ===============================
DELIMITER ;
SOURCE FUNCTIONS/fnGetParentMenu.sql;
DELIMITER ;
SOURCE FUNCTIONS/fnMenu.sql;
DELIMITER ;


-- ===============================
-- PROCEDURES
-- ===============================