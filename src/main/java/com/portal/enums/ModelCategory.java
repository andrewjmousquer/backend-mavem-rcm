package com.portal.enums;

import com.portal.model.Classifier;

public enum ModelCategory {

	STANDARD( new Classifier( 101, "STANDARD", "MODEL_SIZE" ) ),
	PREMIUM( new Classifier( 102, "PREMIUM", "MODEL_SIZE" ) );

	private Classifier type;
	
	ModelCategory( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static ModelCategory getById( Integer id ) {
		if( id != null ) {
			for( ModelCategory type : ModelCategory.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "ModelCategoryType - Não foi encontrado o ENUM com o ID " + id );
	}
}
