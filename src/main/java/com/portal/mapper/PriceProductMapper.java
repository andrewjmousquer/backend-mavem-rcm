package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.PriceList;
import com.portal.model.PriceProduct;
import com.portal.model.ProductModel;

public class PriceProductMapper implements RowMapper<PriceProduct> {

	@Override
	public PriceProduct mapRow(ResultSet rs, int rowNum) throws SQLException {
		return PriceProduct.builder()
								.id( rs.getInt( "ppr_id" ) )
								.price( rs.getDouble( "price" ) )
								.priceList( PriceList.builder().id( rs.getInt( "prl_id" ) ).name( rs.getString( "price_list" )).build() )
								.productModel( ProductModel.builder().id( rs.getInt( "prm_id" ) ).build() )
								.build();
		}
}
