package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.model.VehicleModel;
import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Proposal;
import com.portal.model.ProposalFrontForm;

public interface IProposalDAO extends IBaseDAO<Proposal> {
	
	public List<Proposal> listAll( Pageable pageable ) throws AppException;
	
	public List<Proposal> find( Proposal model, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Proposal> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Proposal, Pageable)}
	 */
	@Deprecated
	public Optional<Proposal> find( Proposal model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Proposal, Pageable)}
	 */
	@Deprecated
	public List<Proposal> search( Proposal model ) throws AppException;

    List<ProposalFrontForm> getByVehicle(VehicleModel vehicleModel, Pageable defaultPagination) throws AppException;
    
	public Long getLastProposalNumber() throws AppException;

}
