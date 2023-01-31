package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.dto.proposal.ProposalDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Proposal;
import com.portal.model.ProposalFrontForm;
import com.portal.model.VehicleModel;

public interface IProposalService extends IBaseService<Proposal> {
	
	public List<Proposal> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Proposal> find( Proposal model, Pageable pageable ) throws AppException, BusException;
	
	public List<Proposal> search( Proposal model, Pageable pageable ) throws AppException, BusException;
	
	public void validateInProgressState( Proposal entity ) throws BusException, AppException;
	
	public void validateInCommercialApprovalState( Proposal entity ) throws BusException, AppException;
	
	public void validateCommercialDisapprovedState( Proposal entity ) throws BusException, AppException;
	
	public void validateCommercialApprovedState( Proposal entity ) throws BusException, AppException;
	
	public void validateOnCustomerApprovalState(Proposal entity) throws BusException, AppException;

	public void validateFinishedWithoutSaleState( Proposal entity ) throws BusException, AppException;

	public void validateFinishedWithSaleState( Proposal entity ) throws BusException, AppException;

	public void validateCanceledState( Proposal entity ) throws BusException, AppException;
	
	public ProposalDTO getProposal(Integer id) throws AppException, BusException;
	
	public Optional<Proposal> getAllProposalId(Integer id) throws AppException, BusException;

	public List<ProposalFrontForm> getByVehicle(VehicleModel vehicleModel) throws AppException, BusException;
	
	public Long getLastProposalNumber() throws AppException;

	public ProposalDTO getProposalForFillFollowUp(Integer id) throws AppException, BusException;

	public Optional<Proposal> getAllProposalIdForFillFollowUp(Integer id) throws BusException, AppException;
}
