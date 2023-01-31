package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ProposalDetailVehicle;

public interface IProposalDetailVehicleDAO extends IBaseDAO<ProposalDetailVehicle> {
	
	public List<ProposalDetailVehicle> listAll( Pageable pageable ) throws AppException;
	
	public List<ProposalDetailVehicle> find( ProposalDetailVehicle model, Pageable pageable ) throws AppException;
	
	public boolean hasVehicleItemRelationship(Integer pdvId) throws AppException;
	
	public Optional<ProposalDetailVehicle> getDetailVehicleByDetail(ProposalDetailVehicle model) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<ProposalDetailVehicle> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(ProposalDetailVehicle, Pageable)}
	 */
	@Deprecated
	public Optional<ProposalDetailVehicle> find( ProposalDetailVehicle model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(ProposalDetailVehicle, Pageable)}
	 */
	@Deprecated
	public List<ProposalDetailVehicle> search( ProposalDetailVehicle model ) throws AppException;
}
