package com.portal.mapper;

import com.portal.dto.ProductWithPriceListIdDTO;
import com.portal.utils.PortalNumberUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductWithPriceListMapper implements RowMapper<ProductWithPriceListIdDTO> {

	@Override
	public ProductWithPriceListIdDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return ProductWithPriceListIdDTO.builder()
						.id( rs.getInt( "prd_id" ) )
						.name( rs.getString( "name" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.proposalExpirationDays( rs.getInt( "proposal_expiration_days" ) )
						.prlId(rs.getInt("prl_id"))
				        .over_parceiro(rs.getBigDecimal("over_parceiro"))
						.build();
	}
}
