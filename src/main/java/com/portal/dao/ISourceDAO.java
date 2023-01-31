package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Source;

public interface ISourceDAO extends IBaseDAO<Source> {
	
	public List<Source> listAll( Pageable pageable ) throws AppException;
	
	public List<Source> find( Source model, Pageable pageable ) throws AppException;
	
	public List<Source> search( Source model, Pageable pageable ) throws AppException;
	
	public boolean hasLeadRelationship(Integer srcId) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Source> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Source, Pageable)}
	 */
	@Deprecated
	public Optional<Source> find( Source model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Source, Pageable)}
	 */
	@Deprecated
	public List<Source> search( Source model ) throws AppException;

	
}
