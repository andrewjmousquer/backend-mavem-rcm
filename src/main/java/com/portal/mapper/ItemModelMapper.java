package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.Model;

public class ItemModelMapper implements RowMapper<ItemModel> {

	@Override
	public ItemModel mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		
		return ItemModel.builder()
								.id( rs.getInt( "imd_id" ) )
								.modelYearStart( rs.getInt( "model_year_start" ) )
								.modelYearEnd( rs.getInt( "model_year_end" ) )
								.model( Model.builder().id( rs.getInt( "mdl_id" ) ).build() )
								.item( Item.builder().id( rs.getInt( "itm_id" ) ).build() )
								.build();
	}
}
