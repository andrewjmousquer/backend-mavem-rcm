package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PriceItemModel;

public interface IPriceItemModelService extends IBaseService<PriceItemModel> {
	
	public List<PriceItemModel> find( PriceItemModel model, Pageable pageable ) throws AppException, BusException;
}
