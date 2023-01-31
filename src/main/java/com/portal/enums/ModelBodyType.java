package com.portal.enums;

import com.portal.model.Classifier;

public enum ModelBodyType {

	SUV( new Classifier( 191, "SUV", "MODEL_BODY_TYPE" ) ),
	SEDAN( new Classifier( 192, "SEDAN", "MODEL_BODY_TYPE" ) ),
	HATCH( new Classifier( 193, "HATCH", "MODEL_BODY_TYPE" ) ),
	COUPE( new Classifier( 194, "COUPE", "MODEL_BODY_TYPE" ) ),
	SW( new Classifier( 195, "SW", "MODEL_BODY_TYPE" ) ),
	CABRIOLET( new Classifier( 196, "CABRIOLET", "MODEL_BODY_TYPE" ) ),
	PICKUP( new Classifier( 197, "PICKUP", "MODEL_BODY_TYPE" ) ),
	MINIVAN( new Classifier( 198, "MINIVAN", "MODEL_BODY_TYPE" ) );
	
	private Classifier type;
	
	ModelBodyType( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static ModelBodyType getById( Integer id ) {
		if( id != null ) {
			for( ModelBodyType type : ModelBodyType.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "ModelBodyType - Não foi encontrado o ENUM com o ID " + id );
	}
}
