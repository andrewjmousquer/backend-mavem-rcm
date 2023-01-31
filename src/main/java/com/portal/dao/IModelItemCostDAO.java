package com.portal.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ModelItemCost;

public interface IModelItemCostDAO extends IBaseDAO<ModelItemCost>  {

	public List<ModelItemCost> listAll( Pageable pageble ) throws AppException;
	
	public List<ModelItemCost> find( ModelItemCost model, Pageable pageable ) throws AppException;
	
	public boolean hasDuplicate(ModelItemCost model) throws AppException;
	
}
