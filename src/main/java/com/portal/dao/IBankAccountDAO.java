package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.BankAccount;

public interface IBankAccountDAO extends IBaseDAO<BankAccount> {
	
	public List<BankAccount> listAll( Pageable pageable ) throws AppException;
	
	public List<BankAccount> find( BankAccount model, Pageable pageable ) throws AppException;
	
	public List<BankAccount> search( BankAccount model, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<BankAccount> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(BankAccount, Pageable)}
	 */
	@Deprecated
	public Optional<BankAccount> find( BankAccount model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(BankAccount, Pageable)}
	 */
	@Deprecated
	public List<BankAccount> search( BankAccount model ) throws AppException;
	
}
