package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.StateModel;

public interface IStateDAO extends IBaseDAO<StateModel> {
	
	public List<StateModel> getByCountryId(Integer couId) throws AppException;
}
