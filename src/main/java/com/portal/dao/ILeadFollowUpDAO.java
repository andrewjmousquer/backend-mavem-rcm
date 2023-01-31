package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.LeadFollowUp;

public interface ILeadFollowUpDAO extends IBaseDAO<LeadFollowUp> {

	public List<LeadFollowUp> find( LeadFollowUp model, Pageable pageable ) throws AppException;
	
	public List<LeadFollowUp> findByLeadId(Integer leadId) throws AppException;
	
	/**
	 * Usar a função {@link #find(LeadFollowUp, Pageable)}
	 */
	@Deprecated
	public Optional<LeadFollowUp> find( LeadFollowUp model ) throws AppException;
	
	/**
	 * Usar a função {@link #search(LeadFollowUp, Pageable)}
	 */
	@Deprecated
	public List<LeadFollowUp> search( LeadFollowUp model ) throws AppException;
}
