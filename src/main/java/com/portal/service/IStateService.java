package com.portal.service;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.StateModel;

public interface IStateService extends IBaseService<StateModel> {
	
	public List<StateModel> getByCountryId(Integer couId) throws AppException;
	
}
