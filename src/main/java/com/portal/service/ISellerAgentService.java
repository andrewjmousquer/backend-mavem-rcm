package com.portal.service;

import java.util.List;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Seller;

public interface ISellerAgentService {

	public List<Seller> findBySeller( Integer selId ) throws AppException, BusException;
	
	public void save( Integer selId, Integer selAgentId, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public void delete( Integer selId, Integer selAgentId, UserProfileDTO userProfile ) throws AppException, BusException;

}
