package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.SalesOrder;

public interface ISalesOrderDAO extends IBaseDAO<SalesOrder> {
	
	public List<SalesOrder> listAll( Pageable pageable ) throws AppException;
	
	public List<SalesOrder> find( SalesOrder model, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<SalesOrder> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(SalesOrder, Pageable)}
	 */
	@Deprecated
	public Optional<SalesOrder> find( SalesOrder model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(SalesOrder, Pageable)}
	 */
	@Deprecated
	public List<SalesOrder> search( SalesOrder model ) throws AppException;

	public  Optional<SalesOrder> findByProposal(Integer id) throws AppException;
}
