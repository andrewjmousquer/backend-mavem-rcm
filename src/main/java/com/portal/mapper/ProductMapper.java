package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Product;
import com.portal.utils.PortalNumberUtils;

public class ProductMapper implements RowMapper<Product> {

	@Override
	public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return Product.builder()
						.id( rs.getInt( "prd_id" ) )
						.name( rs.getString( "name" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.proposalExpirationDays( rs.getInt( "proposal_expiration_days" ) )
						.productDescription(rs.getString("product_description"))
						.build();
	}
}
