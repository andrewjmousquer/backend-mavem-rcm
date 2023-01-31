package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Partner;

public interface IPartnerDAO extends IBaseDAO<Partner> {

	public List<Partner> listAll( Pageable pageble ) throws AppException;
	
	public List<Partner> find( Partner partner, Pageable pageable ) throws AppException;
	
	public List<Partner> search( Partner partner, Pageable pageable ) throws AppException;
	
	public boolean hasPriceListRelationship( Integer partnerId ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Partner> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Partner, Pageable)}
	 */
	@Deprecated
	public Optional<Partner> find(Partner Partner) throws AppException;

	/**
	 * Usar a função {@link #search(Partner, Pageable)}
	 */
	@Deprecated
	public List<Partner> search(Partner Partner) throws AppException;


	public List<Partner> searchForm(String searchText, Pageable pageable) throws AppException;
}
