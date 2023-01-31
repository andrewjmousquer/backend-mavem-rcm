package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.ParameterModel;

public class ParameterMapper implements ResultSetExtractor<List<ParameterModel>> {
	
	public List<ParameterModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<ParameterModel> parameters = new LinkedList<ParameterModel>();
		
		if (rs != null ) {
			while( rs.next() ) {
				ParameterModel parameterMode = new ParameterModel();
				
				parameterMode.setId( rs.getInt( "prm_id" ) );
				parameterMode.setName( rs.getString( "name" ) );
				parameterMode.setValue( rs.getString( "value" ) );
				parameterMode.setDescription( rs.getString( "description" ) );
				
				parameters.add( parameterMode );
			}
		}
		
		return parameters;
	}
}
