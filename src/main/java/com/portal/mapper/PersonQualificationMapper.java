package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.AddressModel;
import com.portal.model.Classifier;
import com.portal.model.Person;
import com.portal.model.PersonQualification;
import com.portal.model.Qualification;

public class PersonQualificationMapper implements RowMapper<PersonQualification> {

	@Override
	public PersonQualification mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		//TODO
		Classifier personType = Classifier.builder()
				.id(rs.getInt("per_cla_id"))
				.build();
		
		Person person = Person.builder()
								.id( rs.getInt( "per_id" ) )
								.name( rs.getString( "per_name" ) )
								.jobTitle( rs.getString( "per_job_title" ) )
								.cpf( rs.getString( "per_cpf" ) )
								.cnpj( rs.getString( "per_cnpj" ) )
								.rg( rs.getString( "per_rg" ) )
								.rne( rs.getString( "per_rne" ) )
								.classification( personType )
								.address( new AddressModel( rs.getInt( "per_add_id" ) ) )
								.build();
		
		Qualification qualification = Qualification.builder()
													.id( rs.getInt("qlf_id" ) )
													.name( rs.getString("qlf_name" ) )
													.seq( rs.getInt("qlf_seq" ) )
													.build();
		
		return PersonQualification.builder()
										.person(person)
										.qualification(qualification)
										.comments( rs.getString( "comments" ) )
										.build();
	}
}
