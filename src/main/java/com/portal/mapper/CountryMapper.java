package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.CountryModel;

public class CountryMapper implements ResultSetExtractor<List<CountryModel>> {
	
	public List<CountryModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<CountryModel> returnList = new LinkedList<CountryModel>();
		
		if (rs != null ) {
			while( rs.next() ) {
			
				CountryModel model = new CountryModel();
				model.setId(rs.getInt("cou_id"));
				model.setName(rs.getString("name"));
				model.setAbbreviation(rs.getString("abbreviation"));
				
				returnList.add( model );
			}
		}
		
		return returnList;
	}
}
