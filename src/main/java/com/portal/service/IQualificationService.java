package com.portal.service;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.portal.dto.QualificationTreePathDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Qualification;

public interface IQualificationService extends IBaseService<Qualification> {

	public List<Qualification> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Qualification> find( Qualification model, Pageable pageable ) throws AppException, BusException;
	
	public List<Qualification> search( Qualification model, Pageable pageable ) throws AppException, BusException;
	
	public void move( int nodeId, int parentId, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public Set<QualificationTreePathDTO> loadTree() throws AppException, BusException;
}
