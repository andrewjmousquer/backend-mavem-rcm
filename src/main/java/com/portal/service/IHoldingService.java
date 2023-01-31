package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.HoldingModel;

public interface IHoldingService extends IBaseService<HoldingModel> {

	public Optional<HoldingModel> getDefaultHolding( Integer usrId ) throws AppException, BusException;
	
	public List<HoldingModel> listByUserId(Integer usrId) throws AppException, BusException;
	
	public List<HoldingModel> fillCustomer( List<HoldingModel> holdings ) throws BusException, AppException;
	
	public Optional<HoldingModel> fillCustomer( HoldingModel holding ) throws BusException, AppException;
	
	public Optional<HoldingModel> getLogo( HoldingModel holding ) throws BusException, AppException;
	
	public Optional<HoldingModel> getHoldingByCustomer(Integer cusId) throws BusException, AppException;
	
}
