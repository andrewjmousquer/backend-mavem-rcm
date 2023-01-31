package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Bank;
import com.portal.utils.PortalNumberUtils;

public class BankMapper implements RowMapper<Bank> {

	@Override
	public Bank mapRow(ResultSet rs, int rowNum) throws SQLException {
		return Bank.builder()
						.id( rs.getInt( "bnk_id" ) )
						.name( rs.getString( "name" ) )
						.code( rs.getString( "code" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.build();
	}
}
