package com.portal.enums;

import com.portal.model.Classifier;

public enum LeadState {
	
	OPENED( new Classifier( 51, "OPENED", "LEAD_STATUS", "ABERTO" ) ),
	CANCELED( new Classifier( 52, "CANCELED", "LEAD_STATUS", "CANCELADO" ) ),
	CONTACTED( new Classifier( 53, "CONTACTED", "LEAD_STATUS", "CONTACTADO" ) ),
	CONVERTED( new Classifier( 54, "CONVERTED", "LEAD_STATUS", "CONVERTIDO" ) ),
	UNCONVERTED( new Classifier( 55, "UNCONVERTED", "LEAD_STATUS", "NÃO CONVERTIDO" ) );

	private Classifier type;
	
	LeadState( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static LeadState getById( Integer id ) {
		if( id != null ) {
			for( LeadState type : LeadState.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "LeadStatusType - Não foi encontrado o ENUM com o ID " + id );
	}
}
