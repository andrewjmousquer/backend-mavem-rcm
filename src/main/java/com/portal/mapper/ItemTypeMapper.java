package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.ItemType;
import com.portal.utils.PortalNumberUtils;

public class ItemTypeMapper implements RowMapper<ItemType> {

	@Override
	public ItemType mapRow( ResultSet rs, int rowNum ) throws SQLException {
		
		return ItemType.builder()
							.id( rs.getInt( "itt_id" ) )
							.name( rs.getString( "name" ) )
							.mandatory( PortalNumberUtils.intToBoolean( rs.getInt( "mandatory" ) ) )
							.multi( PortalNumberUtils.intToBoolean( rs.getInt( "multi" ) ) )
							.seq( rs.getInt( "seq" ) )
							.build();
	}
}
