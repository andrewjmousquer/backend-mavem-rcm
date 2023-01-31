package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.Seller;

public interface ISellerAgentDAO  {

	public List<Seller> findBySeller( Integer selId ) throws AppException;
	
	public void save( Integer selId, Integer selAgentId ) throws AppException;
	
	public void delete( Integer selId, Integer selAgentId ) throws AppException;
}
