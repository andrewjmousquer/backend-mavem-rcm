package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.BankAccount;

public interface IBankAccountService extends IBaseService<BankAccount> {
	
	public List<BankAccount> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<BankAccount> find( BankAccount model, Pageable pageable ) throws AppException, BusException;
	
	public List<BankAccount> search( BankAccount model, Pageable pageable ) throws AppException, BusException;
	
}
