package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PersonRelated;

public interface IPersonRelatedDAO extends IBaseDAO<PersonRelated> {
	
	public List<PersonRelated> listAll( Pageable pageble ) throws AppException;
	
	public List<PersonRelated> find( PersonRelated brand, Pageable pageable ) throws AppException;
	
	public List<PersonRelated> search( PersonRelated brand, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<PersonRelated> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(PersonRelated, Pageable)}
	 */
	@Deprecated
	public Optional<PersonRelated> find( PersonRelated brand ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(PersonRelated, Pageable)}
	 */
	@Deprecated
	public List<PersonRelated> search( PersonRelated brand ) throws AppException;

    List<PersonRelated> findByPerson(Integer id) throws AppException;
}
