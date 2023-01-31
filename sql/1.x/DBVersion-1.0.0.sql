INSERT INTO `classifier` (`cla_id`, `value`, `type`) VALUES (13, 'Boleto','PAYMENT_TYPE');
INSERT INTO `classifier` (`cla_id`, `value`, `type`) VALUES (14, 'Cartão','PAYMENT_TYPE');

insert into menu (name, url, description, icon, type_cla, root_id) values ('Parcelas', 'portion-form', 'Cadastro de Parcelas', 'fa fa-fw fa-usd', 5, null);
insert into menu (name, url, description, icon, type_cla, root_id) values ('Histórico', 'history-form', 'Histórico de Parcelas', 'fa fa-fw fa-history', 5, null);
