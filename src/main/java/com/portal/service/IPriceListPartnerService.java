package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Partner;
import com.portal.model.PriceList;

public interface IPriceListPartnerService  {
	
	public Optional<PriceList> getPriceList( Integer prlId, Integer ptnId ) throws AppException, BusException;
	
	public Optional<Partner> getPartner( Integer prlId, Integer ptnId ) throws AppException, BusException;
	
	public List<PriceList> findByPartner( Integer ptnId ) throws AppException, BusException;
	
	public List<Partner> findByPriceList( Integer prlId ) throws AppException, BusException;
	
	public void save( Integer prlId, Integer ptnId ) throws AppException, BusException;
	
	public void delete( Integer prlId, Integer ptnId  ) throws AppException, BusException;
	
	public void deleteByPriceList( Integer prlId  ) throws AppException, BusException;
}
