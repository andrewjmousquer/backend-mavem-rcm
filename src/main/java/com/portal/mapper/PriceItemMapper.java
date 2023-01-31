package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Item;
import com.portal.model.PriceItem;
import com.portal.model.PriceList;

public class PriceItemMapper implements RowMapper<PriceItem> {

	@Override
	public PriceItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		return PriceItem.builder()
							.id( rs.getInt( "pci_id" ) )
							.price( rs.getDouble( "price" ) )
							.item( Item.builder().id( rs.getInt( "itm_id" ) ).build() )
							.priceList( PriceList.builder().id( rs.getInt( "prl_id" ) ).build() )
							.build();
	}
}
