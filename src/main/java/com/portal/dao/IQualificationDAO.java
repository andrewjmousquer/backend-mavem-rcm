package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Qualification;

public interface IQualificationDAO extends IBaseDAO<Qualification> {
	
	public List<Qualification> listAll( Pageable pageable ) throws AppException;
	
	public List<Qualification> find( Qualification model, Pageable pageable ) throws AppException;
	
	public List<Qualification> search( Qualification model, Pageable pageable ) throws AppException;
	
	public boolean hasPersonRelationship(Integer qlfId) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Qualification> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Qualification, Pageable)}
	 */
	@Deprecated
	public Optional<Qualification> find( Qualification model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Qualification, Pageable)}
	 */
	@Deprecated
	public List<Qualification> search( Qualification model ) throws AppException;
	
}
