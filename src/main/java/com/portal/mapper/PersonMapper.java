package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.AddressModel;
import com.portal.model.Classifier;
import com.portal.model.Person;

public class PersonMapper implements RowMapper<Person> {

	@Override
	public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		LocalDate birthdate = null; 
		Date birthdateDB = rs.getDate( "birthdate" );
		if( birthdateDB != null ) {
			birthdate = birthdateDB.toLocalDate();
		}
		
		Classifier negativeList = null;
		if(rs.getInt("neg_list_cla_id") > 0) {
			negativeList = new Classifier();
			negativeList.setId(rs.getInt("neg_list_cla_id") );
			negativeList.setValue(rs.getString("neg_list_value"));
			negativeList.setLabel(rs.getString("neg_list_label"));
			negativeList.setType(rs.getString("neg_list_type"));
		}
		
		Classifier personType = Classifier.builder()
				.id(rs.getInt("per_cla_id"))
				.value(rs.getString("per_cla_value"))
				.type(rs.getString("per_cla_type"))
				.label(rs.getString("per_cla_label"))
				.build();
		
		return Person.builder()
						.id( rs.getInt( "per_id" ) )
						.name( rs.getString( "name" ) )
						.corporateName( rs.getString( "corporate_name") )
						.jobTitle( rs.getString( "job_title" ) )
						.cpf( rs.getString( "cpf" ) )
						.cnpj( rs.getString( "cnpj" ) )
						.rg( rs.getString( "rg" ) )
						.ie( rs.getString( "ie" ) )
						.rne( rs.getString( "rne" ) )
						.classification( personType )
						.address( rs.getInt( "add_id" ) > 0 ? new AddressModel( rs.getInt( "add_id" ) ) : null )
						.birthdate( birthdate )
						.negativeList(negativeList)
						.build();
	}
}
