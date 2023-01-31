package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.PaymentMethod;
import com.portal.utils.PortalNumberUtils;

public class PaymentMethodMapper implements RowMapper<PaymentMethod> {

	@Override
	public PaymentMethod mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return PaymentMethod.builder()
						.id( rs.getInt( "pym_id" ) )
						.name( rs.getString( "name" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.build();
	}
}
