package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PriceList;

public interface IPriceListService extends IBaseService<PriceList> {
	
	public List<PriceList> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<PriceList> find( PriceList model, Pageable pageable ) throws AppException, BusException;
	
	public List<PriceList> search( PriceList model, Pageable pageable ) throws AppException, BusException;

	public List<PriceList> listOverlay(PriceList model)  throws AppException, BusException;
}
