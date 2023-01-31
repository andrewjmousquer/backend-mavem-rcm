package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PriceProduct;

public interface IPriceProductService extends IBaseService<PriceProduct> {
	
	public List<PriceProduct> find( PriceProduct model, Pageable pageable ) throws AppException, BusException;
}
