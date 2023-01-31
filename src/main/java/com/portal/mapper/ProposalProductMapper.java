package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.ProposalProduct;

public class ProposalProductMapper implements RowMapper<ProposalProduct>{
	
	@Override
	public ProposalProduct mapRow(ResultSet rs, int rowNum) throws SQLException {
				
		return ProposalProduct.builder()
				.nameItemType(rs.getString("name_item_type"))
				.prdId(rs.getInt("prd_id"))
				.nameItem(rs.getString("name_item"))
				.cod(rs.getString("cod"))
				.forFree(rs.getBoolean("for_free"))
				.price(rs.getDouble("price"))
				.pprId(rs.getInt("ppr_id"))
				.prlId(rs.getInt("prl_id"))
				.ptnId(rs.getInt("ptn_id"))
				.build();
	}
	
}
