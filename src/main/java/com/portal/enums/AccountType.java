package com.portal.enums;

import com.portal.model.Classifier;

public enum AccountType {

	CORRENTE( new Classifier( 26, "CORRENTE", "ACCOUNT_TYPE" ) ),
	POUPANCA( new Classifier( 25, "POUPANCA", "ACCOUNT_TYPE" ) );

	private Classifier type;
	
	AccountType( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static AccountType getById( Integer id ) {
		if( id != null ) {
			for( AccountType type : AccountType.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "AccountType - Não foi encontrado o ENUM com o ID " + id );
	}
}
