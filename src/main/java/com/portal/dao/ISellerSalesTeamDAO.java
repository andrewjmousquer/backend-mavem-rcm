package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.SalesTeam;
import com.portal.model.Seller;

public interface ISellerSalesTeamDAO {

	public List<SalesTeam> findBySeller( Integer selId ) throws AppException;
	
	public List<Seller> findBySalesTeam( Integer sltdId ) throws AppException;
	
	public void save( Integer selId, Integer sltId ) throws AppException;
	
	public void delete( Integer selId, Integer sltId  ) throws AppException;
	
}
