package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProductModelCost;

/**
 * @author Ederson Sergio Monteiro Coelho
 *
 */

public interface IProductModelCostService extends IBaseService<ProductModelCost> {

	public List<ProductModelCost> listAll( Pageable pageable ) throws AppException, BusException;

	public List<ProductModelCost> find( ProductModelCost model, Pageable pageable ) throws AppException, BusException;

	public List<ProductModelCost> findDuplicateMultipleValidate( List<ProductModelCost> models ) throws AppException, BusException;
	
	public List<ProductModelCost> search( ProductModelCost model, Pageable pageable ) throws AppException, BusException;
	
	public List<ProductModelCost> saveBulk( List<ProductModelCost> list, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public List<ProductModelCost> updateBulk( List<ProductModelCost> list, UserProfileDTO userProfile  ) throws AppException, BusException;
}