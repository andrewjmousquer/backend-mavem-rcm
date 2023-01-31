package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.PersonClassification;
import com.portal.model.AddressModel;
import com.portal.model.Channel;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.PartnerGroup;
import com.portal.model.PartnerPerson;
import com.portal.model.Person;

public class PartnerPersonMapper implements RowMapper<PartnerPerson> {

	@Override
	public PartnerPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
	
		Person partnerPersonPerson = Person.builder()
								.id( rs.getInt( "pptp_per_id" ) )
								.name( rs.getString( "pptp_name" ) )
								.jobTitle( rs.getString( "pptp_job_title" ) )
								.cpf( rs.getString( "pptp_cpf" ) )
								.cnpj( rs.getString( "pptp_cnpj" ) )
								.rg( rs.getString( "pptp_rg" ) )
								.rne( rs.getString( "pptp_rne" ) )
								.address( new AddressModel( rs.getInt( "pptp_add_id" ) ) )
								.classification( PersonClassification.getById( rs.getInt( "pptpt_cla_id" ) ).getType() )
								.build();
		
		PartnerGroup group = null;
		int groupId = rs.getInt( "ptg_id" );
		if( groupId > 0 ) {
			group = PartnerGroup.builder()
					.id( rs.getInt( "ptg_id" ))
					.name(rs.getString("ptg_name"))
					.build();
		}
		
		Person partnerPerson = Person.builder()
				.id( rs.getInt( "pptn_per_id" ) )
				.name( rs.getString( "pptn_name" ) )
				.jobTitle( rs.getString( "pptn_job_title" ) )
				.cpf( rs.getString( "pptn_cpf" ) )
				.cnpj( rs.getString( "pptn_cnpj" ) )
				.rg( rs.getString( "pptn_rg" ) )
				.rne( rs.getString( "pptn_rne" ) )
				.address( new AddressModel( rs.getInt( "pptn_add_id" ) ) )
				.classification( PersonClassification.getById( rs.getInt( "ptnt_cla_id" ) ).getType() )
				.build();
		
		Channel channel = Channel.builder()
				.id( rs.getInt( "chn_id" ) )
				.name(rs.getString("chn_name"))
				.build();
		
		Classifier situationType = Classifier.builder()
				.id(rs.getInt("sit_cla_id"))
				.value(rs.getString("sit_cla_value"))
				.type(rs.getString("sit_cla_type"))
				.label(rs.getString("sit_cla_label"))
				.build();
		
		Partner partner = Partner.builder()
								.id( rs.getInt( "ptn_id" ) )
								.partnerGroup( group )
								.person(partnerPerson)
								.channel(channel)
								.situation( situationType ) 
								.build();
		
		Classifier partnerPersonType = Classifier.builder()
				.id(rs.getInt("pper_cla_id"))
				.value(rs.getString("pper_cla_value"))
				.type(rs.getString("pper_cla_type"))
				.label(rs.getString("pper_cla_label"))
				.build();
		
		
		return PartnerPerson.builder()
								.person(partnerPersonPerson)
								.partner(partner)
								.personType( partnerPersonType )
								.build();
	}
}