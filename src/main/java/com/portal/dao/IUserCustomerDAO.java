package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.CustomerModel;

public interface IUserCustomerDAO {

	public List<CustomerModel> listUserCustomer(Integer usrId) throws AppException;
	
	public void saveUserCustomer(Integer usrId, Integer cusId) throws AppException;

	public void saveUserCustomer(final Integer usrId, final List<CustomerModel> list) throws AppException;
	
	public void deleteUserCustomer(Integer usrId, Integer cusId) throws AppException;

	public void deleteUserCustomer(final Integer usrId, final List<CustomerModel> list) throws AppException;
	
}
