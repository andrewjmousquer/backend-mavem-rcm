package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalPayment;

public interface IProposalPaymentService extends IBaseService<ProposalPayment> {
	
	public List<ProposalPayment> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<ProposalPayment> find( ProposalPayment model, Pageable pageable ) throws AppException, BusException;
	
	public List<ProposalPayment> search( ProposalPayment model, Pageable pageable ) throws AppException, BusException;
	
}
