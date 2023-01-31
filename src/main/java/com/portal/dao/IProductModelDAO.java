package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PriceItem;
import com.portal.model.ProductModel;

public interface IProductModelDAO extends IBaseDAO<ProductModel> {
	
	public List<ProductModel> find( ProductModel model, Pageable pageable ) throws AppException;
	
	public boolean hasPriceListRelationship(Integer prmId) throws AppException;
	
	public List<ProductModel> findDuplicated(ProductModel model) throws AppException;

	/**
	 * Usar a função {@link #find(PriceItem, Pageable)}
	 */
	@Deprecated
	public Optional<ProductModel> find( ProductModel model ) throws AppException;

	List<ProductModel> listAll(Pageable pageable) throws AppException;

    List<ProductModel> getByProduct(Integer id) throws AppException;
}
