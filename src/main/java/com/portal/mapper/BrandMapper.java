package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Brand;
import com.portal.utils.PortalNumberUtils;

public class BrandMapper implements RowMapper<Brand> {

	@Override
	public Brand mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return Brand.builder()
						.id( rs.getInt( "brd_id" ) )
						.name( rs.getString( "name" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.build();
	}
}
