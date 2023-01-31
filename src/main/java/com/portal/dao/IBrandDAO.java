package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Brand;

public interface IBrandDAO extends IBaseDAO<Brand> {
	
	public List<Brand> listAll( Pageable pageble ) throws AppException;
	
	public List<Brand> find( Brand brand, Pageable pageable ) throws AppException;
	
	public List<Brand> search( Brand brand, Pageable pageable ) throws AppException;
	
	public boolean hasModelRelationship( Integer brandId ) throws AppException;
	
	public boolean hasLeadRelationship( Integer brandId ) throws AppException;
	
	public boolean hasPartnerRelationship( Integer brandId ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Brand> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Brand, Pageable)}
	 */
	@Deprecated
	public Optional<Brand> find( Brand brand ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Brand, Pageable)}
	 */
	@Deprecated
	public List<Brand> search( Brand brand ) throws AppException;
}
