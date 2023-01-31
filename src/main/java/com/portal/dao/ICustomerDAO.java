package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.CustomerModel;

public interface ICustomerDAO extends IBaseDAO<CustomerModel>{

	public List<CustomerModel> listByUserId( Integer userId ) throws AppException;
	
	public List<CustomerModel> listByHoldingId(Integer holId) throws AppException;
	
	public List<CustomerModel> listByUserHolding( Integer usrId, Integer holId ) throws AppException;
	
	public Optional<CustomerModel> findByCNPJ(String cnpj) throws AppException;
	
	public boolean verifyCustomerConstraint(Integer cusId) throws AppException;

}