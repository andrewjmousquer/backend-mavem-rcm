package com.portal.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ProductModelCost;

public interface IProductModelCostDAO extends IBaseDAO<ProductModelCost> {

	public List<ProductModelCost> listAll( Pageable pageable ) throws AppException;

	public List<ProductModelCost> find( ProductModelCost model, Pageable pageable ) throws AppException;

	public List<ProductModelCost> findDuplicate(ProductModelCost model) throws AppException;
	
	public List<ProductModelCost> search( ProductModelCost model, Pageable pageable ) throws AppException;
}