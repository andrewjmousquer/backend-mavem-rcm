package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Bank;

public interface IBankService extends IBaseService<Bank> {
	
	public List<Bank> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Bank> find( Bank model, Pageable pageable ) throws AppException, BusException;
	
	public List<Bank> search( Bank model, Pageable pageable ) throws AppException, BusException;
	
}
