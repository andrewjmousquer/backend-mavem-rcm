package com.portal.service;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CustomerModel;

public interface IUserCustomerService {
	
	public List<CustomerModel> listUserCustomer(Integer usrId) throws  AppException, BusException;

	public void saveUserCustomer(Integer usrId, Integer cusId) throws  AppException, BusException;

	public void saveUserCustomer(Integer usrId, List<CustomerModel> list) throws AppException, BusException;
	
	public void deleteUserCustomer(Integer usrId, Integer cusId) throws  AppException, BusException;

	public void deleteUserCustomer(Integer usrId, List<CustomerModel> list) throws  AppException, BusException;
}
