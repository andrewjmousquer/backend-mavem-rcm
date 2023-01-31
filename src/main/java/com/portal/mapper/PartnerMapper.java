package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.AddressModel;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.PartnerGroup;
import com.portal.model.Person;
import com.portal.utils.PortalNumberUtils;

public class PartnerMapper implements RowMapper<Partner> {

	@Override
	public Partner mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Channel channel = Channel.builder()
							.id( rs.getInt( "chn_id" ) )
							.name( rs.getString( "chn_name" ) )
							.active( PortalNumberUtils.intToBoolean( rs.getInt( "chn_active" ) ) )
							.build();
		
		Classifier personType = Classifier.builder()
				.id(rs.getInt("per_cla_id"))
				.value(rs.getString("per_cla_value"))
				.type(rs.getString("per_cla_type"))
				.label(rs.getString("per_cla_label"))
				.build();
				
		Person person = Person.builder()
								.id( rs.getInt( "entity_per_id" ) )
								.name( rs.getString( "per_name" ) )
								.jobTitle( rs.getString( "per_job_title" ) )
								.cpf( rs.getString( "per_cpf" ) )
								.cnpj( rs.getString( "per_cnpj" ) )
								.rg( rs.getString( "per_rg" ) )
								.rne( rs.getString( "per_rne" ) )
								.classification( personType )
								.address( new AddressModel( rs.getInt( "per_add_id" ) ) )
								.build();
		
		
		PartnerGroup group = null;
		int groupId = rs.getInt( "ptg_id" );
		if( groupId > 0 ) {
			group = PartnerGroup.builder()
					.id( rs.getInt( "ptg_id" ) )
					.name( rs.getString( "ptg_name") )
					.build();
		}
		
		Classifier situationType = Classifier.builder()
				.id(rs.getInt("sit_cla_id"))
				.value(rs.getString("sit_cla_value"))
				.type(rs.getString("sit_cla_type"))
				.label(rs.getString("sit_cla_label"))
				.build();
		
		return Partner.builder()
						.id( rs.getInt( "ptn_id" ) )
						.partnerGroup( group )
						.channel( channel )
						.person( person )
						.situation( situationType )
						.additionalTerm( rs.getInt("additional_term") )
						.isAssistance(rs.getBoolean("is_assistance"))
						.build();
	}
}
