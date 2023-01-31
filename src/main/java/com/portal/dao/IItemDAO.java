package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Item;

public interface IItemDAO extends IBaseDAO<Item> {

	public void updateFile(Integer id, String column, String value) throws AppException;
	
	public List<Item> listAll( Pageable pageble ) throws AppException;
	
	public List<Item> find( Item Item, Pageable pageable ) throws AppException;
	
	public List<Item> search( Item Item, Pageable pageable ) throws AppException;
	
	public boolean hasModelRelationship(Integer itmId)  throws AppException;
	
	public boolean hasPriceItemRelationship(Integer itmId)  throws AppException;
	
	public boolean hasPriceItemModelRelationship(Integer itmId)  throws AppException;
	
	public void saveItemImage(Integer itmId, String image)  throws AppException;
	
	public String getItemImage(Integer itmId) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Item> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Item, Pageable)}
	 */
	@Deprecated
	public Optional<Item> find( Item Item ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Item, Pageable)}
	 */
	@Deprecated
	public List<Item> search( Item Item ) throws AppException;

	

}
