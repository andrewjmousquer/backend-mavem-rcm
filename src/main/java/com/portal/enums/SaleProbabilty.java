package com.portal.enums;

import com.portal.model.Classifier;

public enum SaleProbabilty {
	LOW( new Classifier( 235, "ALTA", "LEAD_PROBABILITY" ) ),
	MEDIUM( new Classifier( 236, "MEDIA", "LEAD_PROBABILITY" ) ),
	HIGH( new Classifier( 237, "BAIXA", "LEAD_PROBABILITY" ) );

	private Classifier type;
	
	SaleProbabilty( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static SaleProbabilty getById( Integer id ) {
		if( id != null ) {
			for( SaleProbabilty type : SaleProbabilty.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "SaleProbabilty - Não foi encontrado o ENUM com o ID " + id );
	}
}
