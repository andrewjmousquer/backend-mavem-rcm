package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.PartnerPersonCommission;
import com.portal.model.Person;

public class PartnerPersonCommissionMapper implements RowMapper<PartnerPersonCommission> {

	@Override
	public PartnerPersonCommission mapRow(ResultSet rs, int rowNum) throws SQLException {
		Person person = Person.builder()
								.id( rs.getInt( "per_id" ) )
								.build();
		
		Partner partner = Partner.builder()
									.id( rs.getInt( "ptn_id" ) )
									.build();
		

		Classifier comissitonType = Classifier.builder()
				.id(rs.getInt("com_cla_id"))
				.value(rs.getString("com_cla_value"))
				.type(rs.getString("com_cla_type"))
				.label(rs.getString("com_cla_label"))
				.build();
		
		return PartnerPersonCommission.builder()
											.partner( partner )
											.person( person )
											.commissionType( comissitonType )
											.defaultValue( rs.getDouble( "commission_default_value" ) )
											.build();
	}
}
