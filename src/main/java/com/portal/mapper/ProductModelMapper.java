package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.utils.PortalNumberUtils;

public class ProductModelMapper implements RowMapper<ProductModel> {

	@Override
	public ProductModel mapRow(ResultSet rs, int rowNum) throws SQLException {

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

		return ProductModel.builder()
				.id(rs.getInt("prm_id"))
				.hasProject(PortalNumberUtils.intToBoolean(rs.getInt("has_project")))
				.modelYearStart(rs.getInt("model_year_start"))
				.modelYearEnd(rs.getInt("model_year_end"))
				.manufactureDays(rs.getInt("manufacture_days"))
				.model(model)
				.product(product)
				.build();
	}
}
