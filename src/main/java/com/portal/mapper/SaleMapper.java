package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.Person;
import com.portal.model.SaleModel;
import com.portal.model.UserModel;

public class SaleMapper implements ResultSetExtractor<List<SaleModel>> {
	
	public List<SaleModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<SaleModel> sales = new LinkedList<SaleModel>();
		SaleModel model = null;
		UserModel userModel = null;
		Person personModel = null;
		
		while(rs != null && rs.next()) {
			model = new SaleModel();
			model.setId( rs.getLong("sal_id"));
			model.setComments(rs.getString("comments"));
			model.setContact(rs.getString("contact"));
			model.setCustomer(rs.getString("customer"));
			model.setDate(rs.getTimestamp("date"));;
			model.setValue(rs.getBigDecimal("value"));
			model.setFirstPayment(rs.getBigDecimal("first_payment"));
			model.setTax(rs.getBigDecimal("tax"));
			model.setPortion(rs.getInt("portion"));
			model.setPaymentType(rs.getString("payment_type"));
			
			personModel = new Person();
			personModel.setName(rs.getString("usr_person"));
			
			userModel = new UserModel();
			userModel.setId(rs.getInt("usr_id"));
			userModel.setUsername(rs.getString("usr_name"));
			userModel.setPerson(personModel);
			model.setUser(userModel);
			
			sales.add(model);
		}
		return sales;
	}
}
