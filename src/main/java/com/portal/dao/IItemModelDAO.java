package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ItemModel;
import com.portal.model.PriceItem;

public interface IItemModelDAO extends IBaseDAO<ItemModel> {
	
	public List<ItemModel> find( ItemModel model, Pageable pageable ) throws AppException;
	
	public boolean hasPriceListRelationship(Integer prmId) throws AppException;
	
	public List<ItemModel> findDuplicated(ItemModel model) throws AppException;

	/**
	 * Usar a função {@link #find(PriceItem, Pageable)}
	 */
	@Deprecated
	public Optional<ItemModel> find( ItemModel model ) throws AppException;
}
