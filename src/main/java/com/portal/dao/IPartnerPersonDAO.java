package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.PartnerPerson;

public interface IPartnerPersonDAO {
	
	public Optional<PartnerPerson> getPartnerPerson( PartnerPerson model) throws AppException;
	
	public List<PartnerPerson> findPartnerPerson( PartnerPerson model) throws AppException;
	
	public void save( PartnerPerson model ) throws AppException;
	
	public void update( PartnerPerson model ) throws AppException;
	
	public void delete( Integer ptnId, Integer perId  ) throws AppException;
}
