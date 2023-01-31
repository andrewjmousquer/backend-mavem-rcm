package com.portal.enums;

import com.portal.model.Classifier;

public enum ProposalPayer {
	
	PARCEIRO( new Classifier( 160, "PARCEIRO", "PROPOSAL_PAYER_TYPE" ) ),
	CONSUMIDOR( new Classifier( 161, "CONSUMIDOR", "PROPOSAL_PAYER_TYPE" ) );
	
	private Classifier type;
	
	ProposalPayer( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static ProposalPayer getById( Integer id ) {
		if( id != null ) {
			for( ProposalPayer type : ProposalPayer.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "Payer - Não foi encontrado o ENUM com o ID " + id );
	}
}
