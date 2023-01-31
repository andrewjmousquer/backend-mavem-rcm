package com.portal.enums;

import com.portal.model.Classifier;

public enum CommissionType {

	SMALL( new Classifier( 121, "COMMMISSION", "COMMISSION_TYPE" ) ),
	MEDIUM( new Classifier( 122, "BONUS", "COMMISSION_TYPE" ) ),
	LARGE( new Classifier( 123, "PAY_PRIZE", "COMMISSION_TYPE" ) );

	private Classifier type;
	
	CommissionType( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static CommissionType getById( Integer id ) {
		if( id != null ) {
			for( CommissionType type : CommissionType.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "CommissionType - Não foi encontrado o ENUM com o ID " + id );
	}
}
