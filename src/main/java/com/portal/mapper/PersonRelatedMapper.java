package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Classifier;
import com.portal.model.Person;
import com.portal.model.PersonRelated;

public class PersonRelatedMapper implements RowMapper<PersonRelated> {

	@Override
	public PersonRelated mapRow(ResultSet rs, int rowNum) throws SQLException {

		LocalDate birthdate = null; 
		Date birthdateDB = rs.getDate( "birthdate" );
		if( birthdateDB != null ) {
			birthdate = birthdateDB.toLocalDate();
		}
		
		Classifier relatedType = Classifier.builder()
				.id(rs.getInt("per_cla_id"))
				.value(rs.getString("per_cla_value"))
				.type(rs.getString("per_cla_type"))
				.label(rs.getString("per_cla_label"))
				.build();
		
		return PersonRelated.builder()
						.id( rs.getInt( "psr_id" ) )
						.name( rs.getString( "name" ) )
						.birthdate(birthdate)
						.relatedType( relatedType )
						.person( Person.builder().id( rs.getInt( "per_id" ) ).build() )
						.build();
	}
}
