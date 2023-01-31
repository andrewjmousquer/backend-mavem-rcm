package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;
import com.portal.utils.PortalNumberUtils;

public class PaymentRuleMapper implements RowMapper<PaymentRule> {

	@Override
	public PaymentRule mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		PaymentMethod pym = PaymentMethod.builder()
											.id( rs.getInt( "pym_id" ) )
											.name( rs.getString( "pym_name" ) )
											.active( PortalNumberUtils.intToBoolean( rs.getInt( "pym_active" ) ) )
											.build();
		
		return PaymentRule.builder()
						.id( rs.getInt( "pyr_id" ) )
						.installments( rs.getInt( "installments" ) )
						.tax( rs.getDouble( "tax" ) )
						.paymentMethod( pym )
						.name( rs.getString( "name" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.preApproved( PortalNumberUtils.intToBoolean( rs.getInt( "pre_approved" ) ) )
						.build();
	}
}
