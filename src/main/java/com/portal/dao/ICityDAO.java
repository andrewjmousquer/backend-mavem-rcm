package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.CityModel;

public interface ICityDAO extends IBaseDAO<CityModel> {
	
	public List<CityModel> listAllFillState() throws AppException;
	
	public List<CityModel> findFillState(CityModel cityModel) throws AppException;
	
	public List<CityModel> getByState(Integer steId) throws AppException;
	
}
