package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ProposalPayment;

public interface IProposalPaymentDAO extends IBaseDAO<ProposalPayment> {
	
	public List<ProposalPayment> listAll( Pageable pageable ) throws AppException;
	
	public List<ProposalPayment> find( ProposalPayment model, Pageable pageable ) throws AppException;
	
	public List<ProposalPayment> search( ProposalPayment model, Pageable pageable ) throws AppException;

	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<ProposalPayment> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(ProposalPayment, Pageable)}
	 */
	@Deprecated
	public Optional<ProposalPayment> find( ProposalPayment model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(ProposalPayment, Pageable)}
	 */
	@Deprecated
	public List<ProposalPayment> search( ProposalPayment model ) throws AppException;


}
