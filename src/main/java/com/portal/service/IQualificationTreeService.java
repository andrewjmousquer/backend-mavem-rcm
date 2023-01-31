package com.portal.service;

import java.util.List;

import com.portal.dto.QualificationTreePathDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;

public interface IQualificationTreeService {
	
	public void addNode( int nodeId, int parentId, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public void deleteNode( int nodeId, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public void moveNode( int nodeId, int parentId ) throws AppException, BusException;
	
	public List<QualificationTreePathDTO> tree() throws AppException, BusException;

	public List<QualificationTreePathDTO> treeByName( String name ) throws AppException, BusException;

	public List<QualificationTreePathDTO> treeByParent(int parentId) throws AppException, BusException;
}
