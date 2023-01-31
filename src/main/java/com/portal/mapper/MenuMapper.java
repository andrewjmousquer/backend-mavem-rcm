package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.Classifier;
import com.portal.model.MenuModel;

public class MenuMapper implements ResultSetExtractor<List<MenuModel>> {
	
	public List<MenuModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<MenuModel> menus = new LinkedList<MenuModel>();
		
		if (rs != null ) {
			while( rs.next() ) {
				Classifier type = new Classifier();
				type.setId( rs.getInt("cla_id"));
				type.setValue(rs.getString("cla_value"));
				type.setType(rs.getString("cla_type"));
				
				MenuModel model = new MenuModel();
				model.setId( rs.getInt( "mnu_id" ) );
				model.setName( rs.getString( "name" ) );
				model.setMenuPath( rs.getString("path") );
				model.setDescription( rs.getString( "description" ) );
				model.setIcon( rs.getString( "icon" ) );
				model.setRoute( rs.getString( "url" ) );
				model.setShow( rs.getBoolean("show"));
				
				if(rs.getInt( "root_id" ) != 0) {
					MenuModel root = new MenuModel();
					root.setId(rs.getInt( "root_id" ));
					model.setRoot(root);
				}
				
				model.setType(type);
				
				menus.add(model);
			}
		}
		
		return menus;
	}
}
