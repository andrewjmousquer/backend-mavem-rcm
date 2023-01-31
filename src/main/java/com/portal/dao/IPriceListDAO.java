package com.portal.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PriceList;

public interface IPriceListDAO extends IBaseDAO<PriceList> {

	public List<PriceList> listAll( Pageable pageable ) throws AppException;
	
	public List<PriceList> find( PriceList model, Pageable pageable ) throws AppException;
	
	public List<PriceList> findByStartPeriod( PriceList model, LocalDateTime start, LocalDateTime end, Pageable pageable ) throws AppException;
	
	public List<PriceList> findByEndPeriod( PriceList model, LocalDateTime start, LocalDateTime end, Pageable pageable ) throws AppException;
	
	public List<PriceList> search( PriceList model, Pageable pageable ) throws AppException;
	
	public List<PriceList> listOverlay( PriceList model ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<PriceList> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(PriceList, Pageable)}
	 */
	@Deprecated
	public Optional<PriceList> find( PriceList model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(PriceList, Pageable)}
	 */
	@Deprecated
	public List<PriceList> search( PriceList model ) throws AppException;
}
