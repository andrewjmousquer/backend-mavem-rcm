package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.PartnerGroup;
import com.portal.utils.PortalNumberUtils;

public class PartnerGroupMapper implements RowMapper<PartnerGroup> {

	@Override
	public PartnerGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return PartnerGroup.builder()
						.id( rs.getInt( "ptg_id" ) )
						.name( rs.getString( "name" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.build();
	}
}
