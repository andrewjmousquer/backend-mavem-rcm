package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Product;

public interface IProductService extends IBaseService<Product> {
	
	public List<Product> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Product> find( Product model, Pageable pageable ) throws AppException, BusException;
	
	public List<Product> search( Product model, Pageable pageable ) throws AppException, BusException;
	
	public Optional<Product> getProductByProductModel(Integer prmId) throws AppException, BusException;
}
