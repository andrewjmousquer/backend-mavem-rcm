package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.CountryModel;
import com.portal.model.StateModel;

public class StateMapper implements ResultSetExtractor<List<StateModel>> {

	@Override
	public List<StateModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<StateModel> list = new LinkedList<StateModel>();
		
		if (rs != null ) {
			while( rs.next() ) {
				CountryModel country = new CountryModel();
				country.setId( rs.getInt("cou_id") );
				country.setName( rs.getString("nameCountry") );
				country.setAbbreviation( rs.getString("abbrevCountry") );
				
				StateModel model = new StateModel();
				model.setId( rs.getInt( "ste_id" ) );
				model.setName( rs.getString( "nameState" ) );
				model.setAbbreviation( rs.getString( "abbrevState" ) );
				model.setCountry(country);
				
				list.add(model);
			}
		}
		
		return list;
	}

}
