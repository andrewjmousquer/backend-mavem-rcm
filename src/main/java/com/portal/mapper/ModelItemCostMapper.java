package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Brand;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.Model;
import com.portal.model.ModelItemCost;
import com.portal.utils.PortalNumberUtils;

public class ModelItemCostMapper implements RowMapper<ModelItemCost> {

	@Override
	public ModelItemCost mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Item item = Item.builder()
				.id(rs.getInt("itm_id"))
				.name(rs.getString("itm_name"))
				.build();
		
		Brand brand = null;
		
		if (rs.getInt("brd_id") > 0) {
			brand = Brand.builder()
					.id(rs.getInt("brd_id"))
					.name(rs.getString("brd_name"))
					.build();
		}
		
		ItemModel itemModel = null;
		
		if (rs.getInt("imd_id") > 0) {
			Item itemModelItem = Item.builder()
					.id(rs.getInt("imd_itm_id"))
					.name(rs.getString("imd_itm_name"))
					.build();
			
			Model itemModelModel = Model.builder()
					.id(rs.getInt("imd_mdl_id"))
					.name(rs.getString("imd_mdl_name"))
					.build();
			
			itemModel = ItemModel.builder()
					.id(rs.getInt("imd_id"))
					.modelYearStart( rs.getInt( "model_year_start" ) )
					.modelYearEnd( rs.getInt( "model_year_end" ) )
					.model( itemModelModel )
					.item( itemModelItem )
					.build();
		}
		
		return ModelItemCost.builder()
						.id( rs.getInt( "mic_id" ) )
						.price( rs.getDouble( "price" ) )
						.allBrands( PortalNumberUtils.intToBoolean( rs.getInt( "all_brands" ) ) )
						.allModels( PortalNumberUtils.intToBoolean( rs.getInt( "all_models" ) ) )
						.item( item )
						.itemModel( itemModel != null ? itemModel : null )
						.brand( brand != null ? brand : null )
						.startDate( rs.getTimestamp( "start_date" ).toLocalDateTime() )
						.endDate( rs.getTimestamp( "end_date" ).toLocalDateTime() )
						.build();
	}
}
