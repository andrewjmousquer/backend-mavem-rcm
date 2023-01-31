package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalCommission;

public interface IProposalCommissionService extends IBaseService<ProposalCommission> {
	
	public List<ProposalCommission> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<ProposalCommission> find( ProposalCommission model, Pageable pageable ) throws AppException, BusException;
	
	public List<ProposalCommission> search( ProposalCommission model, Pageable pageable ) throws AppException, BusException;
	
}
