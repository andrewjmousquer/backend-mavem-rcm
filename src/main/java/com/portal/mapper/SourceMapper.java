package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Source;
import com.portal.utils.PortalNumberUtils;

public class SourceMapper implements RowMapper<Source> {

	@Override
	public Source mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return Source.builder()
						.id( rs.getInt( "src_id" ) )
						.name( rs.getString( "name" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.build();
	}
}
