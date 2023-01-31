package com.portal.enums;

import com.portal.model.Classifier;

public enum PersonType {
	DIRETOR( new Classifier( 33, "DIRETOR", "PERSON_TYPE" ) ),
	GERENTE( new Classifier( 34, "GERENTE", "PERSON_TYPE" ) ),
	VENDEDOR( new Classifier( 35, "VENDEDOR", "PERSON_TYPE" ) ),
	SUPERVISOR( new Classifier( 36, "SUPERVISOR", "PERSON_TYPE" ) );

	private Classifier type;
	
	PersonType( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static PersonType getById( Integer id ) {
		if( id != null ) {
			for( PersonType type : PersonType.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "PersonType - Não foi encontrado o ENUM com o ID " + id );
	}
}
