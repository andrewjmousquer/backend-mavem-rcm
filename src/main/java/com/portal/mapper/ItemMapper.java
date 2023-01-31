package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Classifier;
import com.portal.model.Item;
import com.portal.model.ItemType;
import com.portal.utils.PortalNumberUtils;

public class ItemMapper implements RowMapper<Item> {

	@Override
	public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		ItemType itemType = ItemType.builder()
									.id( rs.getInt( "itt_id" ) )
									.name( rs.getString( "itt_name" ) )
									.mandatory( PortalNumberUtils.intToBoolean( rs.getInt( "itt_mandatory" ) ) )
									.multi( PortalNumberUtils.intToBoolean( rs.getInt( "itt_multi" ) ) )
									.seq( rs.getInt( "itt_seq" ) )
									.build();
		
		Classifier mandatory = Classifier.builder()
				.id(rs.getInt("mand_cla_id"))
				.label(rs.getString("mand_label"))
				.value(rs.getString("mand_value"))
				.type(rs.getString("mand_type"))
				.build();
		
		Classifier responsability = Classifier.builder()
				.id(rs.getInt("resp_cla_id"))
				.label(rs.getString("resp_label"))
				.value(rs.getString("resp_value"))
				.type(rs.getString("resp_type"))
				.build();
		
		return Item.builder()
					.id( rs.getInt( "itm_id" ) )
					.name( rs.getString( "name" ) )
					.cod( rs.getString( "cod" ) )
					.seq( rs.getInt( "seq" ) )
					.forFree( PortalNumberUtils.intToBoolean( rs.getInt( "for_free" ) ) )
					.generic( PortalNumberUtils.intToBoolean( rs.getInt( "generic" ) ) )
					.mandatory( mandatory )
					.itemType( itemType )
					.file(rs.getString("file"))
					.icon(rs.getString("icon"))
					.description( rs.getString("description"))
					.hyperlink( rs.getString("hyperlink"))
					.responsability(responsability)
					.term( rs.getInt( "term" ) )
					.termWorkDay( PortalNumberUtils.intToBoolean( rs.getInt( "term_work_day" ) ) )
					.highlight( PortalNumberUtils.intToBoolean( rs.getInt( "highlight" ) ) )

					.build();
	}
}
