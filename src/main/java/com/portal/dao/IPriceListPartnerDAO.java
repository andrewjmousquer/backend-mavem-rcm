package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.Partner;
import com.portal.model.PriceList;

public interface IPriceListPartnerDAO {

	public Optional<PriceList> getPriceList( Integer prlId, Integer ptnId ) throws AppException;
	
	public Optional<Partner> getPartner( Integer prlId, Integer ptnId ) throws AppException;
	
	public List<PriceList> findByPartner( Integer ptnId ) throws AppException;
	
	public List<Partner> findByPriceList( Integer prlId ) throws AppException;
	
	public void save( Integer prlId, Integer ptnId ) throws AppException;
	
	public void delete( Integer prlId, Integer ptnId  ) throws AppException;
	
	public void deleteByPriceList( Integer prlId  ) throws AppException;
}
