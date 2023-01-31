package com.portal.enums;

import com.portal.model.Classifier;

public enum PersonClassification {

	PJ( new Classifier( 29, "PJ", "PERSON_CLASSIFICATION", "PESSOA FISÍCA" ) ),
	PF( new Classifier( 30, "PF", "PERSON_CLASSIFICATION", "PESSOA JURÍDICA" ) ),
	ESTRANGEIRO( new Classifier( 31, "ESTRANGEIRO", "PERSON_CLASSIFICATION", "ESTRANGEIRO" ) );

	private Classifier type;
	
	PersonClassification( Classifier type ) {
		this.type = type;
	}
	
	public Classifier getType() {
		return type;
	}
	
	public static PersonClassification getById( Integer id ) {
		if( id != null ) {
			for( PersonClassification type : PersonClassification.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "PersonClassificationType - Não foi encontrado o ENUM com o ID " + id );
	}
}
