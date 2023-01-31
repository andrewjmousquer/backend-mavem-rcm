package com.portal.service;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.ParameterModel;

public interface IParameterService extends IBaseService<ParameterModel> {

	public String getValueOf(String name);

	public List<String> getListFromConcatenatedParameter(String parameter) throws AppException;

}