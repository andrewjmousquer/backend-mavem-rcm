package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ModelItemCost;

public interface IModelItemCostService extends IBaseService<ModelItemCost> {

	public List<ModelItemCost> listAll(Pageable pageable) throws AppException, BusException;

	public List<ModelItemCost> find(ModelItemCost model, Pageable pageable) throws AppException, BusException;
	
	public List<ModelItemCost> search(ModelItemCost model, Pageable pageable) throws AppException, BusException;
	
	public void validateHasDuplicate(ModelItemCost model) throws AppException, BusException;
	
}
