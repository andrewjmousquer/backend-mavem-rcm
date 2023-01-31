package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;

public interface IBaseDAO<T> {
	
	public Optional<T> find( T model ) throws AppException; 

	public Optional<T> getById(Integer id) throws AppException;
	
	public List<T> list() throws AppException;
	
	public List<T> search( T model ) throws AppException;

	public Optional<T> save( T model ) throws AppException;
	
	public Optional<T> update( T model ) throws AppException;

	public void delete(Integer id) throws AppException;
	
}
