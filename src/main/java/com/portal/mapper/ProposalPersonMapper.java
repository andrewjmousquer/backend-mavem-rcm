package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Classifier;
import com.portal.model.Person;
import com.portal.model.ProposalPerson;

public class ProposalPersonMapper implements RowMapper<ProposalPerson> {

	@Override
	public ProposalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Classifier classifier = Classifier.builder()
								.id(rs.getInt("pper_cla_id"))
								.value(rs.getString("pper_cla_value"))
								.type(rs.getString("pper_cla_type"))
								.label(rs.getString("pper_cla_label"))
								.build();
		
		Person person = Person.builder()
						.id( rs.getInt( "per_id" ) )
						.build();

		return ProposalPerson.builder()
								.person(person)
								.proposalPersonClassification(classifier)
								.build();
	}
}
