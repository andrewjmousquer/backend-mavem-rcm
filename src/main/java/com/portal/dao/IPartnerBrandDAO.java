package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.Brand;
import com.portal.model.Partner;

public interface IPartnerBrandDAO {

	public List<Brand> findByPartner( Integer ptnId ) throws AppException;
	
	public List<Partner> findByBrand( Integer brdId ) throws AppException;
	
	public void save( Integer ptnId, Integer brdId ) throws AppException;
	
	public void delete( Integer ptnId, Integer brdId  ) throws AppException;
}
