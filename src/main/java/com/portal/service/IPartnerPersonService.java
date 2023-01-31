package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PartnerPerson;

public interface IPartnerPersonService  {
		
	public Optional<PartnerPerson> getPartnerPerson( PartnerPerson model) throws AppException, BusException;
	
	public List<PartnerPerson> findPartnerPerson( PartnerPerson model) throws AppException, BusException;
	
	public void save( PartnerPerson model, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public void update( PartnerPerson model, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public void delete( Integer ptnId, Integer perId, UserProfileDTO userProfile ) throws AppException, BusException;
	
}
