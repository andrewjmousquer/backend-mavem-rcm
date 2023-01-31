package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.AddressModel;
import com.portal.model.CityModel;
import com.portal.model.CountryModel;
import com.portal.model.StateModel;

public class AddressMapper implements ResultSetExtractor<List<AddressModel>> {

	@Override
	public List<AddressModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<AddressModel> list = new LinkedList<AddressModel>();
		
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
				
				AddressModel model = new AddressModel();
				model.setId(rs.getInt("add_id"));
				model.setStreet(rs.getString("street"));
				model.setNumber(rs.getString("number"));
				model.setDistrict(rs.getString("district"));
				model.setComplement(rs.getString("complement"));
				model.setZipCode(rs.getString("zip_code"));
				model.setLatitude(rs.getString("latitude"));
				model.setLongitude(rs.getString("longitude"));
				model.setCity(city);
				
				list.add(model);
			}
		}
		
		return list;
	}

}
