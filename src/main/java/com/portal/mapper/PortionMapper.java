package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.Classifier;
import com.portal.model.PortionModel;

public class PortionMapper implements ResultSetExtractor<List<PortionModel>> {
	
	public List<PortionModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<PortionModel> portions = new LinkedList<PortionModel>();
		
		if (rs != null ) {
			while( rs.next() ) {
				Classifier type = new Classifier();
				type.setId( rs.getInt("cla_id"));
				type.setValue(rs.getString("cla_value"));
				type.setType(rs.getString("cla_type"));
				
				PortionModel model = new PortionModel();
				model.setId( rs.getInt( "por_id" ) );
				model.setName( rs.getInt( "name" ) );
				model.setTax( rs.getBigDecimal("tax") );
				model.setPaymentType(type);
				
				portions.add(model);
			}
		}
		
		return portions;
	}
}
