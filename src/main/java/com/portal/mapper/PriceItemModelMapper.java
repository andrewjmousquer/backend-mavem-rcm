package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Brand;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.PriceItemModel;
import com.portal.model.PriceList;
import com.portal.utils.PortalNumberUtils;

public class PriceItemModelMapper implements RowMapper<PriceItemModel> {

	@Override
	public PriceItemModel mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Brand brand = null;
		int brandId = rs.getInt( "brd_id" );
		if( brandId > 0 ) {
			brand = Brand.builder().id( brandId ).build();
		}
		
		ItemModel itemModel = null;
		int itemModelId = rs.getInt( "imd_id" );
		if( itemModelId > 0 ) {
			itemModel = ItemModel.builder().id( itemModelId ).build();
		}
		
		return PriceItemModel.builder()
								.id( rs.getInt( "pim_id" ) )
								.price( rs.getDouble( "price" ) )
								.priceList( PriceList.builder().id( rs.getInt( "prl_id" ) ).build() )
								.allBrands( PortalNumberUtils.intToBoolean( rs.getInt( "all_brands" ) ) )
								.allModels( PortalNumberUtils.intToBoolean( rs.getInt( "all_models" ) ) )
								.item( Item.builder().id( rs.getInt( "itm_id" ) ).build() )
								.brand( brand )
								.itemModel( itemModel )
								.build();
		}
}
