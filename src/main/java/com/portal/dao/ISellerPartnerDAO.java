package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.Partner;
import com.portal.model.Seller;

public interface ISellerPartnerDAO {

	public List<Partner> findBySeller( Integer selId ) throws AppException;
	
	public List<Seller> findByPartner( Integer ptnId ) throws AppException;
	
	public void save( Integer selId, Integer ptnId ) throws AppException;
	
	public void delete( Integer selId, Integer ptnId  ) throws AppException;

}
