package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ItemType;

public interface IItemTypeDAO extends IBaseDAO<ItemType> {
	
	public List<ItemType> listAll( Pageable pageable ) throws AppException;
	
	public List<ItemType> find( ItemType model, Pageable pageable ) throws AppException;
	
	public List<ItemType> search( ItemType model, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<ItemType> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(ItemType, Pageable)}
	 */
	@Deprecated
	public Optional<ItemType> find( ItemType model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(ItemType, Pageable)}
	 */
	@Deprecated
	public List<ItemType> search( ItemType model ) throws AppException;

	public boolean hasItemRelationship(Integer itemTypeId) throws AppException;
	
}
