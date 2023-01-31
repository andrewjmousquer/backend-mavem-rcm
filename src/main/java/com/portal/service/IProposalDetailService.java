package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalDetail;

public interface IProposalDetailService extends IBaseService<ProposalDetail> {
	
	public List<ProposalDetail> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<ProposalDetail> find( ProposalDetail model, Pageable pageable ) throws AppException, BusException;
	
	public List<ProposalDetail> search( ProposalDetail model, Pageable pageable ) throws AppException, BusException;
	
	public ProposalDetail getDetailByProposal( ProposalDetail model ) throws AppException, BusException;
}
