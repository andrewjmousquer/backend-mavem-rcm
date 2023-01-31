package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Model;
import com.portal.model.PriceList;
import com.portal.model.PriceProduct;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.VehicleModel;

public class ProposalDetailVehicleMapper implements RowMapper<ProposalDetailVehicle> {

	@Override
	public ProposalDetailVehicle mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		VehicleModel vehicle = VehicleModel.builder().id( rs.getInt( "vhe_id" ) ).build();
		if( vehicle == null || vehicle.getId().equals( 0 ) ) {
			vehicle = null;
		}

		PriceList priceList = PriceList.builder().id( rs.getInt("prl_id" )).name( rs.getString("price_list")).build();

		PriceProduct priceProduct = PriceProduct.builder().id( rs.getInt( "ppr_id" ) ).priceList( priceList ).build();
		
		ProposalDetail proposalDetail = ProposalDetail.builder().id( rs.getInt( "ppd_id" ) ).build();
									
		return ProposalDetailVehicle.builder()
										.id( rs.getInt( "pdv_id" ) )
										.proposalDetail( proposalDetail )
										.vehicle(vehicle)
										.model(Model.builder().id( rs.getInt( "mdl_id" ) ).build())
										.modelYear(rs.getInt("model_year"))
										.version(rs.getString("version"))
										.priceProduct(priceProduct)
										.productAmountDiscount( rs.getDouble( "product_amount_discount" ) )
										.productPercentDiscount( rs.getDouble( "product_percent_discount" ) )
										.productFinalPrice( rs.getDouble( "product_final_price" ) )
										.overPrice( rs.getDouble( "over_price" ) )
										.overPricePartnerDiscountAmount( rs.getDouble( "over_price_partner_discount_amount" ) )
										.overPricePartnerDiscountPercent( rs.getDouble( "over_price_partner_discount_percent" ) )
										.priceDiscountAmount( rs.getDouble( "price_discount_amount" ) )
										.priceDiscountPercent( rs.getDouble( "price_discount_percent" ) )
										.totalAmount( rs.getDouble( "total_amount" ) )
										.totalTaxAmount( rs.getDouble( "total_tax_amount" ) )
										.totalTaxPercent( rs.getDouble( "total_tax_percent" ) )
										.standardTermDays( rs.getInt("standard_term_days") )
										.agreedTermDays( rs.getInt("agreed_term_days") )
										.build();
	}
}
