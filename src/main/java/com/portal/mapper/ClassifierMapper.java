package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.Classifier;

public class ClassifierMapper implements ResultSetExtractor<List<Classifier>> {
	
	public List<Classifier> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<Classifier> returnList = new LinkedList<Classifier>();
		if (rs != null ) {
			while( rs.next() ) {
				Classifier model = new Classifier();
				model.setId( rs.getInt( "cla_id" ) );
				model.setValue( rs.getString( "value" ) );
				model.setType( rs.getString( "type" ) );
				model.setLabel( rs.getString( "label" ) );
				model.setDescription( rs.getString( "description" ) );

				returnList.add( model );
			}
		}
		
		return returnList;
	}
}
