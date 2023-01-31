package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.AddressModel;
import com.portal.model.Classifier;
import com.portal.model.HoldingModel;
import com.portal.model.Person;

public class HoldingMapper implements ResultSetExtractor<List<HoldingModel>> {

	@Override
	public List<HoldingModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<HoldingModel> holders = new LinkedList<HoldingModel>();
		
		if(rs != null) {
			while(rs.next()) {
				
				HoldingModel holding = new HoldingModel();
				holding.setId( rs.getInt( "hol_id" ) );
				holding.setName( rs.getString( "name" ) );
				holding.setCnpj( rs.getString( "cnpj" ) );
				holding.setSocialName( rs.getString( "social_name" ) );
				holding.setStateRegistration( rs.getString( "state_registration" ) );
				holding.setMunicipalRegistration( rs.getString( "municipal_registration" ) );

				AddressModel address = new AddressModel();
				address.setId( rs.getInt( "add_id" ) );
				holding.setAddress( address );
				
				Person person = new Person();
				person.setId( rs.getInt("per_id") );
				holding.setPerson(person);
				
				Classifier type = new Classifier();
				type.setId( rs.getInt("type_cla") );
				holding.setType(type);
				
				holders.add(holding);
			}
		}
		
		return holders;
	}

}
