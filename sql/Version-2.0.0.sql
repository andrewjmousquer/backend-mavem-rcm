START TRANSACTION;

-- MENUS CRM
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Marca', 'brand-form', 'Cadastro de Marcas', 'fa fa-fw fa-object-group', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Modelo', 'model-form', 'Cadastro de Modelos', 'fa fa-fw fa-car', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Banco', 'bank-form', 'Cadastro de Bancos', 'fa fa-fw fa-bank', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Canal', 'channel-form', 'Cadastro de Canais', 'fa fa-fw fa-briefcase', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Produto', 'product-form', 'Cadastro de Produtos', 'fa fa-product-hunt', 8, null, 1); 
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Tipo de Item', 'itemtype-form', 'Tipo de Item', 'fa fa-fw fa-tag', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Origem', 'source-form', 'Cadastro de Origens', 'fa fa-circle-o', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Parcelas', 'portion-form', 'Cadastro de Parcelas', 'fa fa-fw fa-usd', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Histórico', 'history-form', 'Histórico de Parcelas', 'fa fa-fw fa-history', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Qualificação', 'qualification-form', 'Cadastro de Qualificações', 'fa fa-check-square', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Veículos', 'vehicle-form', 'Cadastro de Veículos', 'fa fa-fw fa-car', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Item', 'item-form', 'Cadastro de Item', 'fa fa-barcode', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Meio de Pagamento', 'paymentrule-form', 'Cadastro de Meio de Pagamento', 'fa fa-credit-card', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Método de Pagamento', 'paymentmethod-form', 'Menu para acessar a tela de cadastro de metodo de pagamento', 'fa fa-fw fa-credit-card', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Célula de Vendas', 'salesteam-form', 'Menu para acessar a tela de célula de venda', 'fa fa-fw fa-bell ', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Lista de Preço', 'price-list-form', 'Cadastro de Lista de Preço', 'fa fa-fw fa-list-ul', '8', null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Proposta', 'proposal-search', 'Pesquisa de propostas', 'fa fa-file-text', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Executivo de Negócio', 'seller-form', 'Executivo de Vendas', 'fa fa-briefcase', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Grupo de Parceiros', 'partner-group-form', 'Grupo de Parceiros', 'fa fa-fw fa-users', 8, null, 1); 
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Parceiro', 'partner-form', 'Parceiro', 'fa fa-fw fa-user', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Pessoa', 'person-form', 'Cadastro de pessoa', 'fa fa-fw fa-user-o', 8, null, 1);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Proposta', 'proposal-form', 'Registro de propostas', 'fa fa-file-text', 8, null, 0);
INSERT INTO menu (name, url, description, icon, type_cla, root_id, `show`) VALUES ('Aprovação Comercial', 'proposal-approval', 'Aprovação Comercial da Proposta', 'fa fa-fw fa-handshake-o', 8, null, 0);

-- ITEM_MANDATORY 22 até 24
INSERT INTO `classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (22, 'SUGERIDO', 'ITEM_MANDATORY', 'Sugerido', 'Determina se o item vai aparece como sugerido');
INSERT INTO `classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (23, 'OBRIGATORIO', 'ITEM_MANDATORY', 'Obrigatório', 'Determina se o item é obrigatório para a seleção');
INSERT INTO `classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (24, 'N_OBRIGATORIO', 'ITEM_MANDATORY', 'Não obrigatório', 'Determina se o item será opcional');

-- ACCOUNT_TYPE 25 até 28
INSERT INTO `classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (25, 'POUPANCA','ACCOUNT_TYPE', 'Conta Poupança', 'Tipo de conta bancária, conta poupança');
INSERT INTO `classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (26, 'CORRENTE','ACCOUNT_TYPE', 'Conta Corrente', 'Tipo de conta bancária, conta corrente');

-- PERSON_CLASSIFICATION 29 até 31
INSERT INTO `classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (29, 'PJ','PERSON_CLASSIFICATION', 'Pessoa Jurídica', 'Classificação da pessoa, pessoa jurídica');
INSERT INTO `classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (30, 'PF','PERSON_CLASSIFICATION', 'Pessoa Fisíca', 'Classificação da pessoa, pessoa física');
INSERT INTO `classifier` (`cla_id`, `value`, `type`, `label`, `description`) VALUES (31, 'ESTRANGEIRO','PERSON_CLASSIFICATION', 'Estrangeiro', 'Classificação da pessoa, pessoa estrangeira');

-- LEAD_STATUS 51 até 60
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (51, 'Aberto','LEAD_STATUS', 'OPENED', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (52, 'Cancelado','LEAD_STATUS', 'CANCELED', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (53, 'Contactado','LEAD_STATUS', 'CONTACTED', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (54, 'Convertido','LEAD_STATUS', 'CONVERTED', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (55, 'Não Convertido','LEAD_STATUS', 'UNCONVERTED', NULL );

-- PROPOSAL_STATUS 61 até 80
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (61, 'Em Andamento','PROPOSAL_STATUS', 'IN_PROGRESS', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (62, 'Em Aprovação Comercial','PROPOSAL_STATUS', 'IN_COMMERCIAL_APPROVAL', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (63, 'Reprovado Comercial','PROPOSAL_STATUS', 'COMMERCIAL_DISAPPROVED', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (64, 'Aprovado Comercial','PROPOSAL_STATUS', 'COMMERCIAL_APPROVED', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (65, 'Em Aprovação Cliente','PROPOSAL_STATUS', 'ON_CUSTOMER_APPROVAL', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (66, 'Finalizada Sem Venda','PROPOSAL_STATUS', 'FINISHED_WITHOUT_SALE', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (67, 'Finalizada Com Venda','PROPOSAL_STATUS', 'FINISHED_WITH_SALE', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (68, 'Cancelado','PROPOSAL_STATUS', 'CANCELED', NULL );

-- DOCUMENT_TYPE 81 até 90
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (81, 'RG','DOCUMENT_TYPE', 'RG', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (82, 'CPF','DOCUMENT_TYPE', 'CPF', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (83, 'CONTRATO','DOCUMENT_TYPE', 'CONTRACT', NULL );

-- MODEL_SIZE 96 até 100
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (95, 'COMPACTO','MODEL_SIZE', 'COMPACT', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (96, 'PEQUENO','MODEL_SIZE', 'SMALL', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (97, 'GRANDE','MODEL_SIZE', 'LARGE', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (98, 'MÉDIO','MODEL_SIZE', 'MEDIUM', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (99, 'SPECIAL','MODEL_SIZE', 'ESPECIAL', NULL );

-- MODEL_CATEGORY 101 até 105
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (101, 'PADRÃO','MODEL_CATEGORY', 'STANDARD', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (102, 'PREMIUM','MODEL_CATEGORY', 'PREMIUM', NULL );

-- SALE_PROBABILITY 106 até 111
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (106, 'BAIXO','SALE_PROBABILITY', 'LOW', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (107, 'MÉDIO','SALE_PROBABILITY', 'MEDIUM', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (108, 'ALTO','SALE_PROBABILITY', 'HIGH', NULL );

-- PERSON_RELATED 112 até 120
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (112, 'PAI','PERSON_RELATED', 'FATHER', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (113, 'FILHO','PERSON_RELATED', 'SON', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (114, 'ESPOSA','PERSON_RELATED', 'WIFE', NULL );

-- COMMISSION_TYPE 121 até 125
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (121, 'Comissão','COMMISSION_TYPE', 'COMMMISSION', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (122, 'Bonús','COMMISSION_TYPE', 'BONUS', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (123, 'Paga Prêmio','COMMISSION_TYPE', 'PAY_PRIZE', NULL );

-- PROPOSAL_RISK 140 até 145
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (140, 'NORMAL','PROPOSAL_RISK', 'NORMAL', NULL);
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (141, 'ALTO','PROPOSAL_RISK', 'HIGH', NULL);

-- PROPOSAL_PAYER_TYPE 146 até 150
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (146, 'PARCEIRO','PROPOSAL_PAYER_TYPE', 'PARTNER', NULL);
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (147, 'CONSUMIDOR','PROPOSAL_PAYER_TYPE', 'CONSUMER', NULL);

-- EVENT_TYPE 150 até 155
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (150, 'DATA FIXA','EVENT_TYPE', 'DATAFIXA', NULL);

-- PARTNER_AGENT_TYPE 156 até 160
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (156, 'CLIENTE','PERSON_TYPE', 'Cliente', NULL);
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (156, 'VENDEDOR','PERSON_TYPE', 'Vendedor', NULL);
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (157, 'SUPERVISOR','PERSON_TYPE', 'Supervisor', NULL);
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (158, 'GERENTE','PERSON_TYPE', 'Gerente', NULL);
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (159, 'DIRETOR','PERSON_TYPE', 'Diretor', NULL);

-- DATA_TYPE 165 até 179
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (170, 'DATA DE CRIAÇÃO','DATE_TYPE', 'CREATE_DATE', NULL);
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (171, 'DATA DE VALIDADE','DATE_TYPE', 'VALIDITY_DATE', NULL);

-- MODEL_BODY_TYPE 191 até 205
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (191, 'SUV','MODEL_BODY_TYPE', 'SUV', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (192, 'SEDAN','MODEL_BODY_TYPE', 'SEDAN', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (193, 'HATCH','MODEL_BODY_TYPE', 'HATCH', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (194, 'COUPE','MODEL_BODY_TYPE', 'COUPE', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (195, 'SW','MODEL_BODY_TYPE', 'SW', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (196, 'CABRIOLET','MODEL_BODY_TYPE', 'CABRIOLET', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (197, 'PICKUP','MODEL_BODY_TYPE', 'PICKUP', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (198, 'MINIVAN/VAN','MODEL_BODY_TYPE', 'MINIVAN/VAN', NULL );


-- PARAMETROS
INSERT INTO parameter (prm_id, name, `value`, description ) VALUES (NULL,'DOCUMENT_ALLOWED_EXTENSIONS','pdf,xls,xlsx,doc,png,jpg,jpeg','Lista de extensões permitida para o upload de documentos. Necessário separar por vírgula (,)');
INSERT INTO parameter (prm_id, name, `value`, description ) VALUES (NULL,'DOCUMENT_ALLOWED_MIME_TYPES','application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/msword,text/plain,image/png,image/jpeg','Lista de extensões permitida para o upload de documentos. Necessário separar por vírgula (,)');
INSERT INTO parameter (prm_id, name, `value`, description ) VALUES (NULL,'PROPOSAL_DAYS_LIMIT','10','Número de dias para expiração da proposta');
	

-- INSERTS PARA TIPO DE PAGAMENTO
INSERT INTO payment_method (pym_id, name, active) VALUES (NULL, 'Cartão de Crédito', 1);
INSERT INTO payment_method (pym_id, name, active) VALUES (NULL, 'Boleto', 1);
INSERT INTO payment_method (pym_id, name, active) VALUES (NULL, 'Transferência', 1);

-- INSERTS PARA CANAIS
INSERT INTO channel (chn_id, name) VALUES (1, 'Concessionária');
INSERT INTO channel (chn_id, name) VALUES (2, 'Corporativo');
INSERT INTO channel (chn_id, name) VALUES (3, 'Direto');
INSERT INTO channel (chn_id, name) VALUES (4, 'Intermediário');
INSERT INTO channel (chn_id, name) VALUES (5, 'Locadora');
INSERT INTO channel (chn_id, name) VALUES (6, 'Montadora');
INSERT INTO channel (chn_id, name) VALUES (7, 'Multimarcas');

-- INSERTS TIPOS DE SELLER
INSERT INTO `job` (`job_id`, `name`, `level`) VALUES (1, 'CEO',1);
INSERT INTO `job` (`job_id`, `name`, `level`) VALUES (2, 'Diretor Comercial',2);
INSERT INTO `job` (`job_id`, `name`, `level`) VALUES (3, 'Gerente Comercial',3);
INSERT INTO `job` (`job_id`, `name`, `level`) VALUES (4, 'Executivo de Conta',4);
INSERT INTO `job` (`job_id`, `name`, `level`) VALUES (5, 'Venda Interna',5);
INSERT INTO `job` (`job_id`, `name`, `level`) VALUES (6, 'Preposto',6);

-- INSERTS TIPOS DE ITENS
INSERT INTO `item_type` (`itt_id`, `name`, `mandatory`, `multi`, `seq`) VALUES (1, 'Documentação', 1, 0, 1);
INSERT INTO `item_type` (`itt_id`, `name`, `mandatory`, `multi`, `seq`) VALUES (2, 'Emplacamento', 1, 0, 2);
INSERT INTO `item_type` (`itt_id`, `name`, `mandatory`, `multi`, `seq`) VALUES (3, 'Exército', 1, 0, 3);
INSERT INTO `item_type` (`itt_id`, `name`, `mandatory`, `multi`, `seq`) VALUES (4, 'Opcionais', 0, 0, 4);
INSERT INTO `item_type` (`itt_id`, `name`, `mandatory`, `multi`, `seq`) VALUES (5, 'Tampa traseira', 0, 0, 5);
INSERT INTO `item_type` (`itt_id`, `name`, `mandatory`, `multi`, `seq`) VALUES (6, 'Teto solar', 0, 0, 6);
INSERT INTO `item_type` (`itt_id`, `name`, `mandatory`, `multi`, `seq`) VALUES (7, 'Vidro traseiro', 0, 0, 7);

INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (180, 'DECISOR','CUSTOMER_TYPE', 'DECISOR', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (181, 'FINANCIADOR','CUSTOMER_TYPE', 'FINANCIADOR', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (182, 'INFLUENCIADOR','CUSTOMER_TYPE', 'INFLUENCIADOR', NULL );
INSERT INTO `classifier` (`cla_id`, `label`, `type`, `value`, `description`) VALUES (183, 'USUARIO','CUSTOMER_TYPE', 'USUARIO', NULL );

COMMIT;
