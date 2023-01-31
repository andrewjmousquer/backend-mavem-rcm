package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Product;

public interface IProductDAO extends IBaseDAO<Product> {
	
	public List<Product> listAll( Pageable pageable ) throws AppException;
	
	public List<Product> find( Product model, Pageable pageable ) throws AppException;
	
	public List<Product> search( Product model, Pageable pageable ) throws AppException;
	
	public Optional<Product> getProductByProductModel(Integer prmId) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Product> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Product, Pageable)}
	 */
	@Deprecated
	public Optional<Product> find( Product model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Product, Pageable)}
	 */
	@Deprecated
	public List<Product> search( Product model ) throws AppException;
	
}
