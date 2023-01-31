package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.HoldingModel;

public interface IHoldingDAO extends IBaseDAO<HoldingModel> {

	public boolean hasUser(Integer holId) throws AppException;

	public List<HoldingModel> listByUserId(Integer usrId) throws AppException;
	
	public  Optional<HoldingModel> getLogo( HoldingModel holding ) throws AppException;
	
	public  Optional<HoldingModel> getDefaultHolding(Integer usrId) throws AppException;
	
	public Optional<HoldingModel> getHoldingByCustomer(Integer cusId) throws AppException;
	
}