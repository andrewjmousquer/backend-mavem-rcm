package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Bank;

public interface IBankDAO extends IBaseDAO<Bank> {
	
	public List<Bank> listAll( Pageable pageable ) throws AppException;
	
	public List<Bank> find( Bank model, Pageable pageable ) throws AppException;
	
	public List<Bank> search( Bank model, Pageable pageable ) throws AppException;
	
	public boolean hasBankAccountRelationship( Integer bankId ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Bank> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Bank, Pageable)}
	 */
	@Deprecated
	public Optional<Bank> find( Bank model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Bank, Pageable)}
	 */
	@Deprecated
	public List<Bank> search( Bank model ) throws AppException;
	
}
