package com.portal.enums;

import com.portal.model.Classifier;

public enum ModelSize {

	COMPACT( new Classifier( 95, "COMPACT", "COMPACT" ) ),
	SMALL( new Classifier( 96, "SMALL", "MODEL_SIZE" ) ),
	MEDIUM( new Classifier( 97, "MEDIUM", "MODEL_SIZE" ) ),
	LARGE( new Classifier( 98, "LARGE", "MODEL_SIZE" ) ),
	SPECIAL( new Classifier( 99, "LARGE", "MODEL_SIZE" ) );

	private Classifier type;
	
	ModelSize( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static ModelSize getById( Integer id ) {
		if( id != null ) {
			for( ModelSize type : ModelSize.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "ModelSizeType - Não foi encontrado o ENUM com o ID " + id );
	}
}
