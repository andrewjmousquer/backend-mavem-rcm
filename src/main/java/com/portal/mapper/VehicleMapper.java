package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.portal.model.Classifier;
import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.VehicleModel;

public class VehicleMapper implements RowMapper<VehicleModel> {

	@Override
	public VehicleModel mapRow(ResultSet rs, int rowNum) throws SQLException {

		LocalDate purchaseDate = null; 
		Date purchaseDateDB = rs.getDate( "purchase_date" );
		if( purchaseDateDB != null ) {
			purchaseDate = purchaseDateDB.toLocalDate();
		}
		
		Double purchaseValue = rs.getDouble( "purchase_value" );
		if( rs.wasNull() ) {
			purchaseValue = null;
		}
		Brand brand = Brand.builder()
				.id( rs.getInt( "brd_id" ) )
				.name( rs.getString( "brand_name" ) )
				.build();

		Model model = Model.builder()
				.id( rs.getInt( "mdl_id" ) )
				.name( rs.getString( "name" ) )
				.brand( brand )
				.build();

		Classifier color = null;
		Integer claColorId = rs.getInt( "cla_id" );
		if(claColorId != null && claColorId > 0) {
			color = Classifier.builder()
				.id( claColorId )
				.value( rs.getString( "value" ) )
				.type( rs.getString( "type" ) )
				.label( rs.getString( "label" ) )
				.description( rs.getString( "description" ) )
				.build();
		}

		return VehicleModel.builder()
						.id( rs.getInt( "vhe_id" ) )
						.chassi( rs.getString( "chassi" ) )
						.plate( rs.getString( "plate" ) )
						.model(model)
						.version( rs.getString( "version" ) )
						.modelYear( rs.getInt( "model_year" ) )
						.purchaseDate( purchaseDate )
						.purchaseValue( purchaseValue )
						.color( color )
						.build();
	}
}
