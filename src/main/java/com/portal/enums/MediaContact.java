package com.portal.enums;

import com.portal.model.Classifier;

public enum MediaContact {
	EMAIL( new Classifier( 240, "EMAIL", "MEDIA_CONTACT" ) ),
	FACEBOOK( new Classifier( 241, "FACEBOOK", "LEAD_PROBABILITY" ) ),
	INSTAGRAM( new Classifier( 242, "INSTAGRAM", "LEAD_PROBABILITY" ) ),
	TELEFONE( new Classifier( 243, "TELEFONE", "LEAD_PROBABILITY" ) ),
	WHATS_APP( new Classifier( 244, "WHATS_APP", "LEAD_PROBABILITY" ) );

	private Classifier type;
	
	MediaContact( Classifier type ) {
		this.type = type;
	}

	public Classifier getType() {
		return type;
	}
	
	public static MediaContact getById( Integer id ) {
		if( id != null ) {
			for( MediaContact type : MediaContact.values() ) {
				if( type.getType().getId().equals( id ) ) {
					return type;
				}
			}
		}
		
		throw new IllegalArgumentException( "MediaContact - Não foi encontrado o ENUM com o ID " + id );
	}
}
