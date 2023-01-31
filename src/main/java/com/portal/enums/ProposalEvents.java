package com.portal.enums;

public enum ProposalEvents {
	NEW, // Nova Proposta 
	REQUEST_COMMECIAL_APPROVAL, // Solicita Aprovação Comercial
	SEND_TO_CUSTOMER, // Envia ao Cliente
	DISAPPROVED_COMMERCIAL, // Comercial Reprovado
	APPROVED_COMMERCIAL, // Comercial Aprovado
	RENEGOTIATION, // Renegociação
	CUSTOMER_APPROVED, // Aprovado Cliente
	CUSTOMER_DISAPPROVED, // Reprovado Cliente
	FINISHED_WITH_SALE, // Finalizada com Venda
	CANCEL // Cancelar
}
