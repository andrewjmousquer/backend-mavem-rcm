package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.utils.PortalNumberUtils;

public class ModelMapper implements RowMapper<Model> {

	@Override
	public Model mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Brand brand = Brand.builder()
								.id( rs.getInt( "brd_id" ) )
								.name( rs.getString( "brand_name" ) )
								.active( PortalNumberUtils.intToBoolean( rs.getInt( "brand_active" ) ) )
								.build();
		
		return Model.builder()
						.id( rs.getInt( "mdl_id" ) )
						.name( rs.getString( "name" ) )
						.codFipe( rs.getString( "cod_fipe" ))
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.brand( brand )
						.bodyType( ModelBodyType.getById( rs.getInt( "body_type_cla_id" ) ) )
						.size( ModelSize.getById( rs.getInt( "type_cla_id" ) ) )
						.category( ModelCategory.getById( rs.getInt( "category_cla_id" ) ) )
						.build();
	}
}
