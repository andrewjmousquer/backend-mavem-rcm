package com.portal.enums;

import com.portal.model.Classifier;

public enum ProposalState {
	
	IN_PROGRESS( new Classifier( 61, "IN_PROGRESS", "PROPOSAL_STATUS", "EM ANDAMENTO" ) ),
	IN_COMMERCIAL_APPROVAL( new Classifier( 62, "IN_COMMERCIAL_APPROVAL", "PROPOSAL_STATUS", "EM APROVAÇÃO COMERCIAL") ),
	COMMERCIAL_DISAPPROVED( new Classifier( 63, "COMMERCIAL_DISAPPROVED", "PROPOSAL_STATUS" , "REPROVADO COMERCIAL") ),
	COMMERCIAL_APPROVED( new Classifier( 64, "COMMERCIAL_APPROVED", "PROPOSAL_STATUS", "APROVADO COMERCIAL" ) ),
	ON_CUSTOMER_APPROVAL( new Classifier( 65, "ON_CUSTOMER_APPROVAL", "PROPOSAL_STATUS", "EM APROVAÇÃO CLIENTE" ) ),
	FINISHED_WITHOUT_SALE( new Classifier( 66, "FINISHED_WITHOUT_SALE", "PROPOSAL_STATUS", "FINALIZADA SEM VENDA") ),
	FINISHED_WITH_SALE( new Classifier( 67, "FINISHED_WITH_SALE", "PROPOSAL_STATUS", "FINALIZADA COM VENDA") ),
	CANCELED( new Classifier( 68, "CANCELED", "PROPOSAL_STATUS", "CANCELADO") );
	
	private Classifier type;
	
	ProposalState( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static ProposalState getById( Integer id ) {
		if( id != null ) {
			for( ProposalState type : ProposalState.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "ProposalState - Não foi encontrado o ENUM com o ID " + id );
	}
	
	public static Classifier getStatusById( Integer id ) {
		if( id != null ) {
			for( ProposalState type : ProposalState.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type.getType();
				}
			}
		}
		
		throw new IllegalArgumentException( "ProposalState - Não foi encontrado o ENUM com o ID " + id );
	}

	public static ProposalState getByValue( String value ) {
		if( value != null ) {
			for( ProposalState type : ProposalState.values() ) {
				if( type.getType().getValue().equals( value ) ) {
					return type;
				}
			}
		}

		throw new IllegalArgumentException( "ProposalState - Não foi encontrado o ENUM com o Value " + value );
	}
	
	
}
