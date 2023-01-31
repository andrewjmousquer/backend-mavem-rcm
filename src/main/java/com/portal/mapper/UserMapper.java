package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.AccessListModel;
import com.portal.model.Classifier;
import com.portal.model.CustomerModel;
import com.portal.model.MenuModel;
import com.portal.model.Person;
import com.portal.model.UserModel;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalStringUtils;

public class UserMapper implements ResultSetExtractor<List<UserModel>> {
	
	public List<UserModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		
		List<UserModel> users = new LinkedList<UserModel>();
		
		if (rs != null ) {
			while( rs.next() ) {
				UserModel userModel = new UserModel();
				userModel.setId( rs.getInt( "usr_id" ) );
				userModel.setUsername( rs.getString( "username" ) );
				userModel.setPassword( rs.getString( "password" ) );
				userModel.setEnabled( PortalNumberUtils.intToBoolean( rs.getInt( "enabled" ) ) );
				userModel.setChangePass( PortalNumberUtils.intToBoolean( rs.getInt( "change_pass" ) ) );
				userModel.setExpirePass( PortalNumberUtils.intToBoolean( rs.getInt( "expire_pass" ) ) );
				userModel.setPassErrorCount( rs.getInt( "pass_error_count" ) );

				userModel.setForgotKey( rs.getString( "forgot_key" ) );
				Date forgotKeyCreated = rs.getDate( "forgot_key_created" );
				if( forgotKeyCreated != null ) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis( forgotKeyCreated.getTime() );
					userModel.setForgotKeyCreated( calendar.getTime() );	
				}
			
				Date lastPassChange = rs.getDate( "last_pass_change" );
				if( lastPassChange != null ) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis( lastPassChange.getTime() );
					userModel.setLastPassChange( calendar.getTime() );	
				}
				
				userModel.setBlocked( PortalNumberUtils.intToBoolean( rs.getInt( "blocked" ) ) );
				
				Classifier personType = Classifier.builder()
						.id(rs.getInt("per_cla_id"))
						.value(rs.getString("per_cla_value"))
						.type(rs.getString("per_cla_type"))
						.label(rs.getString("per_cla_label"))
						.build();
				
				Person person = new Person();
				person.setId(rs.getInt("per_id"));
				person.setName(rs.getString("person"));
				person.setJobTitle(rs.getString("job_title"));
				person.setCpf(rs.getString("cpf"));
				person.setClassification(personType);
				userModel.setPerson(person);
				
				AccessListModel accesslist = new AccessListModel();
				accesslist.setId(rs.getInt("acl_id"));
				accesslist.setName(rs.getString("accesslits"));
				userModel.setAccessList(accesslist);
				
				if(rs.getInt("mnu_id") > 0 ) {
					MenuModel menu = new MenuModel();
					menu.setId(rs.getInt("mnu_id"));
					menu.setName(rs.getString("menu_name"));
					menu.setRoute(rs.getString("url"));
					accesslist.setDefaultMenu(menu);
				}
				
				if(rs.getInt("cus_id") > 0 ) {
					CustomerModel customer = new CustomerModel();
					customer.setId(rs.getInt("cus_id"));
					customer.setName(rs.getString("cus_name"));
					customer.setCnpj(rs.getString("cus_cnpj"));
					customer.setLabel(customer.getName() + " - " + PortalStringUtils.formatCnpj(customer.getCnpj()));
					userModel.setCustomer(customer);
				}
				
				Classifier type = new Classifier();
				type.setId( rs.getInt("cla_id"));
				type.setValue(rs.getString("cla_value"));
				type.setType(rs.getString("cla_type"));
				userModel.setUserType(type);
								
				Date lastLogin = rs.getDate( "last_login" );
				if( lastLogin != null ) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis( lastLogin.getTime() );
					userModel.setLastLogin( calendar.getTime() );	
				}
				
				userModel.setLastErrorCount( rs.getInt( "last_error_count" ) );
				userModel.setConfig( rs.getString( "config" ) );
				
				users.add( userModel );
			}
		}
		
		return users;
	}
}
