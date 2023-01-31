package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.CityModel;
import com.portal.model.CountryModel;
import com.portal.model.StateModel;

public class CityMapper implements ResultSetExtractor<List<CityModel>> {

	@Override
	public List<CityModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<CityModel> list = new LinkedList<CityModel>();
		if (rs != null ) {
			while( rs.next() ) {
		
				CountryModel country = new CountryModel();
				country.setId( rs.getInt("cou_id") );
				country.setName( rs.getString("nameCountry") );
				country.setAbbreviation( rs.getString("abbrevCountry") );
				
				StateModel state = new StateModel();
				state.setId( rs.getInt("ste_id") );
				state.setName( rs.getString("nameState") );
				state.setAbbreviation( rs.getString("abbrevState") );
				state.setCountry( country );
				
				CityModel city = new CityModel();
				city.setId( rs.getInt( "cit_id" ) );
				city.setName( rs.getString( "nameCity" ) );
				city.setState( state );
				
				list.add(city);
			}
		}
		
		return list;
	}

}
