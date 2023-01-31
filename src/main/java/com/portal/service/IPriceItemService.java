package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PriceItem;

public interface IPriceItemService extends IBaseService<PriceItem> {
	
	public List<PriceItem> find( PriceItem model, Pageable pageable ) throws AppException, BusException;
}
