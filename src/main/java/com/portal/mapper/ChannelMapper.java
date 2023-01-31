package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Channel;
import com.portal.utils.PortalNumberUtils;

public class ChannelMapper implements RowMapper<Channel> {

	@Override
	public Channel mapRow(ResultSet rs, int rowNum) throws SQLException {
		return Channel.builder()
						.id( rs.getInt( "chn_id" ) )
						.name( rs.getString( "name" ) )
						.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
						.hasPartner( PortalNumberUtils.intToBoolean( rs.getInt( "has_partner" ) ) )
						.hasInternalSale( PortalNumberUtils.intToBoolean( rs.getInt( "has_internal_sale" ) ) )
						.build();
	}
}
