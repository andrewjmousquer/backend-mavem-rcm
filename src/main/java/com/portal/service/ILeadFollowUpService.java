package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.NoSuchMessageException;
import org.springframework.data.domain.Pageable;

import com.portal.dto.LeadFollowUpDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.LeadFollowUp;

public interface ILeadFollowUpService  extends IBaseService<LeadFollowUp> {

	public List<LeadFollowUp> find( LeadFollowUp model, Pageable pageable ) throws AppException, BusException;
	
	public List<LeadFollowUp> findByLeadId( Integer leadId ) throws NoSuchMessageException, AppException, BusException;

	Optional<LeadFollowUp> save(LeadFollowUpDTO model, UserProfileDTO userProfile) throws AppException, BusException;

	public Optional<LeadFollowUp> update(LeadFollowUpDTO dto, UserProfileDTO userProfile) throws AppException, BusException;
}
