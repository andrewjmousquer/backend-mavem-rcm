package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CustomerModel;

public interface ICustomerService extends IBaseService<CustomerModel> {

	public List<CustomerModel> listByUserId( Integer userId ) throws AppException, BusException;
	
	public List<CustomerModel> listByHoldingId(Integer holId) throws AppException, BusException;
	
	public List<CustomerModel> listByUserHolding( Integer usrId, Integer holId ) throws AppException, BusException;
	
	public Optional<CustomerModel> findByCNPJ(String cnpj) throws AppException, BusException;
	
	public void verifyCustomerConstraint(CustomerModel customer) throws AppException, BusException;
	
}
