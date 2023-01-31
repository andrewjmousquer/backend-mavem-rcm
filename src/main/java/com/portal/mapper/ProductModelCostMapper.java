package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.model.ProductModelCost;
import com.portal.utils.PortalNumberUtils;

public class ProductModelCostMapper implements RowMapper<ProductModelCost> {

	@Override
	public ProductModelCost mapRow(ResultSet rs, int rowNum) throws SQLException {

		Brand brand = Brand.builder()
				.id(rs.getInt("brd_id"))
				.name(rs.getString("brand_name"))
				.active(PortalNumberUtils.intToBoolean(rs.getInt("brand_active")))
				.build();

		Model model = Model.builder()
				.id(rs.getInt("mdl_id"))
				.name(rs.getString("model_name"))
				.active(PortalNumberUtils.intToBoolean(rs.getInt("model_active")))
				.brand(brand)
				.bodyType(ModelBodyType.getById(rs.getInt("body_type_cla_id")))
				.size(ModelSize.getById(rs.getInt("type_cla_id")))
				.category(ModelCategory.getById(rs.getInt("category_cla_id")))
				.build();

		Product product = Product.builder()
				.id( rs.getInt( "prd_id" ) )
				.name( rs.getString( "name" ) )
				.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
				.proposalExpirationDays( rs.getInt( "proposal_expiration_days" ) )
				.build();

		ProductModel productModel = ProductModel.builder()
				.id(rs.getInt("prm_id"))
				.hasProject(PortalNumberUtils.intToBoolean(rs.getInt("has_project")))
				.modelYearStart(rs.getInt("model_year_start"))
				.modelYearEnd(rs.getInt("model_year_end"))
				.manufactureDays(rs.getInt("manufacture_days"))
				.model(model)
				.product(product)
				.build();

		LocalDate start_date = null; 
		Date start_dateDB = rs.getDate( "start_date" );
		if( start_dateDB != null ) {
			start_date = start_dateDB.toLocalDate();
		}

		LocalDate end_date = null; 
		Date end_dateDB = rs.getDate( "end_date" );
		if( end_dateDB != null ) {
			end_date = end_dateDB.toLocalDate();
		}

		return ProductModelCost.builder()
						.id( rs.getInt( "pmc_id" ) )
						.productModel( productModel )
						.startDate( start_date )
						.endDate( end_date )
						.totalValue( rs.getBigDecimal("total_value") )
						.build();
	}
}