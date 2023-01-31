package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.AccessListModel;
import com.portal.model.MenuModel;

public class AccesslistMapper implements ResultSetExtractor<List<AccessListModel>> {

	@Override
	public List<AccessListModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<AccessListModel> list = new LinkedList<AccessListModel>();
		
		if (rs != null ) {
			while( rs.next() ) {
				
				AccessListModel model = new AccessListModel();
				model.setId( rs.getInt( "acl_id" ) );
				model.setName( rs.getString( "name" ) );
				
				if(rs.getInt("mnu_id") > 0 ) {
					MenuModel menu = new MenuModel();
					menu.setId(rs.getInt("mnu_id"));
					menu.setName(rs.getString("menu_name"));
					menu.setRoute(rs.getString("url"));
					model.setDefaultMenu(menu);;
				
					
				}
				
				list.add(model);
			}
		}
		
		return list;
}
	
}