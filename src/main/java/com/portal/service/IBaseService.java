package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;

public interface IBaseService<T> {
	
	public Optional<T> find( T model ) throws AppException, BusException; 
	
	public Optional<T> getById(Integer id) throws AppException, BusException;
	
	public List<T> list() throws AppException, BusException;

	public List<T> search( T model ) throws AppException, BusException;
	
	public Optional<T> saveOrUpdate( T model, UserProfileDTO userProfile ) throws AppException, BusException;

	public Optional<T> save( T model, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public Optional<T> update( T model, UserProfileDTO userProfile  ) throws AppException, BusException;
	
	public void delete( Integer id, UserProfileDTO userProfile  ) throws AppException, BusException;
	
	public void audit( T model, AuditOperationType operationType, UserProfileDTO userProfile ) throws AppException, BusException;
	
}	