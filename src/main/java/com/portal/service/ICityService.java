package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CityModel;

public interface ICityService extends IBaseService<CityModel> {
		
	public List<CityModel> listAllFillState() throws AppException, BusException;
	
	public List<CityModel> findFillState(CityModel model) throws AppException, BusException;

	public List<CityModel> fillState( List<CityModel> cities )  throws AppException, BusException;
	
	public Optional<CityModel> fillState( CityModel city )  throws AppException, BusException;
	
	public List<CityModel> getByState(Integer steId) throws AppException, BusException;
}
