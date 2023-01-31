package com.portal.enums;

import com.portal.model.Classifier;

public enum ProposalRisk {

	NORMAL( new Classifier( 140 , "NORMAL", "PROPOSAL_RISK" ) ),
	HIGH( new Classifier( 141 , "HIGH", "PROPOSAL_RISK" ) );
	
	private Classifier type;
	
	ProposalRisk( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static ProposalRisk getById( Integer id ) {
		if( id != null ) {
			for( ProposalRisk type : ProposalRisk.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "Risk - Não foi encontrado o ENUM com o ID " + id );
	}
}
