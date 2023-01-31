package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ItemModel;

public interface IItemModelService extends IBaseService<ItemModel> {
	
	public List<ItemModel> find( ItemModel model, Pageable pageable ) throws AppException, BusException;
}
