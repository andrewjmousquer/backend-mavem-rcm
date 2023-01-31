package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ProposalCommission;
import com.portal.model.ProposalDetail;

public interface IProposalCommissionDAO extends IBaseDAO<ProposalCommission> {
	
	public List<ProposalCommission> listAll( Pageable pageable ) throws AppException;
	
	public List<ProposalCommission> find( ProposalCommission model, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<ProposalCommission> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(ProposalDetail, Pageable)}
	 */
	@Deprecated
	public Optional<ProposalCommission> find( ProposalCommission model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(ProposalDetail, Pageable)}
	 */
	@Deprecated
	public List<ProposalCommission> search( ProposalCommission model ) throws AppException;
	
}
