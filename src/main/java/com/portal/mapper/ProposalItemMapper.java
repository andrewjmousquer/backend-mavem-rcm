package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.ProposalItem;

public class ProposalItemMapper implements RowMapper<ProposalItem>{
	
	@Override
	public ProposalItem mapRow(ResultSet rs, int rowNum) throws SQLException {
				
		return ProposalItem.builder()
				.itmId(rs.getInt("itm_id"))
				.nameItem(rs.getString("name_item"))
				.cod(rs.getString("cod"))
				.forFree(rs.getBoolean("for_free"))
				.price(rs.getDouble("price"))
				.build();
	}
	
}
