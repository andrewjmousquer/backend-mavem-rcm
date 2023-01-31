package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.CheckpointModel;

public class CheckpointMapper implements ResultSetExtractor<List<CheckpointModel>> {
	
	public List<CheckpointModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<CheckpointModel> checks = new LinkedList<CheckpointModel>();
		
		if (rs != null ) {
			while( rs.next() ) {

				CheckpointModel checkpoint = new CheckpointModel();
				checkpoint.setId( rs.getInt( "ckp_id" ) );
				checkpoint.setName( rs.getString( "name" ) );
				checkpoint.setDescription( rs.getString( "description" ) );
				
				checks.add( checkpoint );
			}
		}
		
		return checks;
	}
}
