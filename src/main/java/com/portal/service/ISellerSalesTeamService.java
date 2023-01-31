package com.portal.service;

import java.util.List;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SalesTeam;
import com.portal.model.Seller;

public interface ISellerSalesTeamService {

	public List<SalesTeam> findBySeller( Integer selId ) throws AppException, BusException;
	
	public List<Seller> findBySalesTeam( Integer sltdId ) throws AppException, BusException;
	
	public void save( Integer selId, Integer sltId, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public void delete( Integer selId, Integer sltId, UserProfileDTO userProfile ) throws AppException, BusException;
	
}
