package com.portal.dao;

import java.util.List;

import com.portal.dto.QualificationTreePathDTO;
import com.portal.exceptions.AppException;

public interface IQualificationTreeDAO {
	
	public void addNode( int parentId, int childId ) throws AppException;
	
	public void deleteNode( Integer id ) throws AppException;
	
	public void disconnectNode( int nodeId ) throws AppException;
	
	public void connectNode(int nodeId, int newParentId) throws AppException;

	public boolean isChildOf( int parentId, int childId ) throws AppException;
	
	public boolean hasDuplicate( int nodeA, int nodeB ) throws AppException;
	
	public List<QualificationTreePathDTO> tree() throws AppException;
	
	public List<QualificationTreePathDTO> treeByName( String name ) throws AppException;

	public List<QualificationTreePathDTO> treeByParent(int parentId) throws AppException;
}
