package com.portal.enums;

import com.portal.model.Classifier;

public enum MenuType {

	PORTAL_CONFIG( new Classifier( 7, "PORTAL_CONFIG", "MENU_TYPE" ) ),
	PORTAL_PRODUCT( new Classifier( 8, "PORTAL_PRODUCT", "MENU_TYPE" ) ),
	APP_HOME( new Classifier( 9, "APP_HOME", "MENU_TYPE" ) ),
	APP_CONFIG( new Classifier( 10, "APP_CONFIG", "MENU_TYPE" ) );
	
	private Classifier type;
	
	MenuType( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static MenuType getById( Integer id ) {
		if( id != null ) {
			for( MenuType type : MenuType.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "MenuType - Não foi encontrado o ENUM com o ID " + id );
	}
}
