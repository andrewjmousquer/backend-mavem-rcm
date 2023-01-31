package com.portal.service;

import java.util.List;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Partner;

public interface IPartnerBrandService  {
	
	public List<Brand> findByPartner( Integer ptnId ) throws AppException, BusException;
	
	public List<Partner> findByBrand( Integer brdId ) throws AppException, BusException;
	
	public void save( Integer ptnId, Integer brdId, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public void delete( Integer ptnId, Integer brdId, UserProfileDTO userProfile ) throws AppException, BusException;
}
