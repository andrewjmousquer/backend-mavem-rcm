package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.portal.model.Classifier;
import com.portal.model.CustomerModel;
import com.portal.model.HoldingModel;
import com.portal.utils.PortalStringUtils;

public class CustomerMapper implements ResultSetExtractor<List<CustomerModel>> {

	@Override
	public List<CustomerModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<CustomerModel> customers = new LinkedList<CustomerModel>();
		if(rs != null) {
			while(rs.next()) {
				CustomerModel customer = new CustomerModel();
				customer.setId(rs.getInt("cus_id"));
				customer.setName(rs.getString("name"));
				customer.setCnpj(rs.getString("cnpj"));
				if(customer.getCnpj() != null) {
					if(customer.getCnpj().length() > 11)
						customer.setLabel(customer.getName() + " - " + PortalStringUtils.formatCnpj(customer.getCnpj()));
					else
						customer.setLabel(customer.getName() + " - " + PortalStringUtils.formatCpf(customer.getCnpj()));
				}
				
				customer.setType(new Classifier(rs.getInt("cla_id")));
				customer.getType().setType(rs.getString("cla_type"));
				customer.getType().setValue(rs.getString("cla_value"));

				HoldingModel holding = new HoldingModel();
				holding.setId(rs.getInt("hol_id"));
				customer.setHolding(holding);
				
				customers.add(customer);
			}
		}
		
		return customers;
	}
	
}
