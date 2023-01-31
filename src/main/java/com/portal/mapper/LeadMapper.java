package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.LeadState;
import com.portal.enums.SaleProbabilty;
import com.portal.model.Brand;
import com.portal.model.Lead;
import com.portal.model.Model;
import com.portal.model.Person;
import com.portal.model.Seller;
import com.portal.model.Source;
import com.portal.utils.PortalNumberUtils;

public class LeadMapper implements RowMapper<Lead> {

	@Override
	public Lead mapRow(ResultSet rs, int rowNum) throws SQLException {

		Person client = Person.builder().id(rs.getInt("client_per_id")).build();
		Seller seller = Seller.builder().id(rs.getInt("seller_id"))
				.person(Person.builder().id(rs.getInt("seller_per_id")).name(rs.getString("seller_name")).build()).build();
		Source source = Source.builder().id(rs.getInt("src_id")).name(rs.getString("source_name"))
				.active(PortalNumberUtils.intToBoolean(rs.getInt("source_active"))).build();

		Brand brand = Brand.builder().id(rs.getInt("brand_id")).name(rs.getString("brand_name")).build();

		Model model = Model.builder().id(rs.getInt("mdl_id")).name(rs.getString("model_name")).brand(brand).build();

		return Lead.builder().id(rs.getInt("led_id")).name(rs.getString("name"))
				.createDate(rs.getTimestamp("create_date").toLocalDateTime()).email(rs.getString("email"))
				.phone(rs.getString("phone")).client(client).seller(seller).source(source).model(model)
				.status(LeadState.getById(rs.getInt("status_cla_id")).getType())
				.saleProbabilty(SaleProbabilty.getById(rs.getInt("sale_probability_cla_id")).getType())
				.subject(rs.getString("subject")).description(rs.getString("description")).build();
	}
}
