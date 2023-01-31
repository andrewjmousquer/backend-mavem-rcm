package com.portal.enums;

import com.portal.model.Classifier;

public enum ProposalPersonType {

	CLIENTE( new Classifier( 180, "CLIENTE", "PROPOSAL_PERSON" ) ),
	FINANCIADOR( new Classifier( 181, "FINANCIADOR", "PROPOSAL_PERSON" ) );

	private Classifier type;
	
	ProposalPersonType( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static ProposalPersonType getById( Integer id ) {
		if( id != null ) {
			for( ProposalPersonType type : ProposalPersonType.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "ProposalPerson - Não foi encontrado o ENUM com o ID " + id );
	}
}
