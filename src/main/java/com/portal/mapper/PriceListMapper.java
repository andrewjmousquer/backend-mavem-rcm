package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Channel;
import com.portal.model.PriceList;
import com.portal.utils.PortalNumberUtils;

public class PriceListMapper implements RowMapper<PriceList> {

	@Override
	public PriceList mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Channel channel = Channel.builder()
								.id( rs.getInt( "chn_id" ) )
								.name( rs.getString( "chn_name" ) )
								.active( PortalNumberUtils.intToBoolean( rs.getInt( "chn_active" ) ) )
								.hasPartner( PortalNumberUtils.intToBoolean( rs.getInt( "chn_has_partner" ) ) )
								.build();
		
		return PriceList.builder()
						.id( rs.getInt( "prl_id" ) )
						.name( rs.getString( "name" ) )
						.start( rs.getTimestamp( "start_date" ).toLocalDateTime() )
						.end( rs.getTimestamp( "end_date" ).toLocalDateTime() )
						.channel( channel )
						.allPartners( PortalNumberUtils.intToBoolean( rs.getInt( "all_partners" ) ) )
						.build();
	}
}
