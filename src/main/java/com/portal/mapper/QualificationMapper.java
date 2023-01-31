package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Qualification;
import com.portal.utils.PortalNumberUtils;

public class QualificationMapper implements RowMapper<Qualification> {

	@Override
	public Qualification mapRow(ResultSet rs, int rowNum) throws SQLException {
		return Qualification.builder()
								.id( rs.getInt("qlf_id" ) )
								.name( rs.getString("name" ) )
								.seq( rs.getInt("seq" ) )
								.required( PortalNumberUtils.intToBoolean( rs.getInt( "required" ) ) )
								.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
								.build();
	}
}
