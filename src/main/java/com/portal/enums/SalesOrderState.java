package com.portal.enums;

import com.portal.model.Classifier;

public enum SalesOrderState {
	VALIDATION_BACKOFFICE( new Classifier(255, "VALIDATION_BACKOFFICE","SALES_ORDER_STATUS") ),
	DISAPPROVED_BACKOFFICE( new Classifier(256, "DISAPPROVED_BACKOFFICE","SALES_ORDER_STATUS") ),
	APPROVED_BACKOFFICE( new Classifier(257, "APPROVED_BACKOFFICE","SALES_ORDER_STATUS") ),
	FINALIZED_ORDER( new Classifier(258, "FINALIZED_ORDER","SALES_ORDER_STATUS") ),
	CANCELED( new Classifier(259, "CANCELED","SALES_ORDER_STATUS") );

	private Classifier type;
	
	SalesOrderState( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static SalesOrderState getById( Integer id ) {
		if( id != null ) {
			for( SalesOrderState type : SalesOrderState.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "SalesOrderState - Não foi encontrado o ENUM com o ID " + id );
	}

	public static SalesOrderState getByValue( String value ) {
		if( value != null ) {
			for( SalesOrderState type : SalesOrderState.values() ) {
				if( type.getType().getValue().equals( value ) ) {
					return type;
				}
			}
		}

		throw new IllegalArgumentException( "SalesOrderState - Não foi encontrado o ENUM com o Value " + value );
	}
}
