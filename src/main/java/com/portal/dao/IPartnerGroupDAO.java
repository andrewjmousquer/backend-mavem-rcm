package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.PartnerGroup;

public interface IPartnerGroupDAO extends IBaseDAO<PartnerGroup> {
	
	public List<PartnerGroup> listAll( Pageable pageable ) throws AppException;
	
	public List<PartnerGroup> find( PartnerGroup model, Pageable pageable ) throws AppException;
	
	public List<PartnerGroup> search( PartnerGroup model, Pageable pageable ) throws AppException;
	
	public boolean hasPartnerRelationship(Integer ptgId) throws AppException;

	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<PartnerGroup> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(PartnerGroup, Pageable)}
	 */
	@Deprecated
	public Optional<PartnerGroup> find( PartnerGroup model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(PartnerGroup, Pageable)}
	 */
	@Deprecated
	public List<PartnerGroup> search( PartnerGroup model ) throws AppException;
}
