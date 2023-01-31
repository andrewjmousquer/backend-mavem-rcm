package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SalesOrder;

public interface ISalesOrderService extends IBaseService<SalesOrder> {
	
	public List<SalesOrder> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<SalesOrder> find( SalesOrder model, Pageable pageable ) throws AppException, BusException;
	
	public List<SalesOrder> search( SalesOrder model, Pageable pageable ) throws AppException, BusException;

	public Optional<SalesOrder> findByProposal(Integer id) throws BusException, AppException;
}
