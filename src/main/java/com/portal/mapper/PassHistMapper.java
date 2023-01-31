package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.PassHistModel;
import com.portal.model.UserModel;

public class PassHistMapper implements ResultSetExtractor<List<PassHistModel>> {
	
	public List<PassHistModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<PassHistModel> passHists = new LinkedList<PassHistModel>();
		
		if (rs != null ) {
			while( rs.next() ) {
				UserModel user = new UserModel();
				user.setId( rs.getInt( "usr_id" ) );
				
				PassHistModel passHistModel = new PassHistModel();
				passHistModel.setId( rs.getInt( "pas_id" ) );
				passHistModel.setPassword( rs.getString( "password" ) );
				passHistModel.setUser( user );
				
				Date changeDate = rs.getDate( "change_date" );
				if( changeDate != null ) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis( changeDate.getTime() );
					passHistModel.setChangeDate( calendar.getTime() );	
				}
				
				passHists.add( passHistModel );
			}
		}
		
		return passHists;
	}
}
