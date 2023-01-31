package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ItemType;

public interface IItemTypeService extends IBaseService<ItemType> {
	
	public List<ItemType> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<ItemType> find( ItemType model, Pageable pageable ) throws AppException, BusException;
	
	public List<ItemType> search( ItemType model, Pageable pageable ) throws AppException, BusException;
	
}
