package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.dto.LeadDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.LeadEvents;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.exceptions.StateWorkflowException;
import com.portal.model.Lead;

public interface ILeadService extends IBaseService<Lead> {
	
	public List<Lead> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Lead> find( Lead model, Pageable pageable ) throws AppException, BusException;
	
	public List<Lead> search( Lead model, Pageable pageable ) throws AppException, BusException;
	
	public Optional<Lead> changeStatus( Lead model, LeadEvents event ) throws AppException, BusException, StateWorkflowException;

	public void validateOpenedState( Lead entity ) throws BusException, AppException;
	
	public void validateCanceledState( Lead entity ) throws BusException, AppException;
	
	public void validateContactedState( Lead entity ) throws BusException, AppException;
	
	public void validateConvertedState( Lead entity ) throws BusException, AppException;
	
	public void validateUnConvertedState( Lead entity ) throws BusException, AppException;

	public Optional<Lead> save(LeadDTO dto, UserProfileDTO userProfile) throws AppException, BusException;

	public Optional<Lead> update(LeadDTO dto, UserProfileDTO userProfile) throws AppException, BusException;
}
