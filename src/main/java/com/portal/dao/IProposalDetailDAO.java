package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ProposalDetail;

public interface IProposalDetailDAO extends IBaseDAO<ProposalDetail> {
	
public List<ProposalDetail> listAll( Pageable pageable ) throws AppException;
	
	public List<ProposalDetail> find( ProposalDetail model, Pageable pageable ) throws AppException;
	
	public Optional<ProposalDetail> getDetailByProposal( ProposalDetail model ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<ProposalDetail> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(ProposalDetail, Pageable)}
	 */
	@Deprecated
	public Optional<ProposalDetail> find( ProposalDetail model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(ProposalDetail, Pageable)}
	 */
	@Deprecated
	public List<ProposalDetail> search( ProposalDetail model ) throws AppException;
	
}
