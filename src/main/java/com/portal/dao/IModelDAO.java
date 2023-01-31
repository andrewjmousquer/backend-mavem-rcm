package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Model;

public interface IModelDAO extends IBaseDAO<Model> {
	
	public List<Model> listAll( Pageable pageable ) throws AppException;
	
	public List<Model> find( Model model, Pageable pageable ) throws AppException;
	
	public List<Model> search( Model model, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Model> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Model, Pageable)}
	 */
	@Deprecated
	public Optional<Model> find( Model model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Model, Pageable)}
	 */
	@Deprecated
	public List<Model> search( Model model ) throws AppException;

	public boolean hasLeadRelationship(Integer modelId) throws AppException;

	public boolean hasItemRelationship(Integer modelId) throws AppException;

	public boolean hasProductRelationship(Integer modelId) throws AppException;

	public boolean hasVehicleRelationship(Integer modelId) throws AppException;

    List<Model> listAllByBrand(int id, Pageable pageable) throws AppException;
}
