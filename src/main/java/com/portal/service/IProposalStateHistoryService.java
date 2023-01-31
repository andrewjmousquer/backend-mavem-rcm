package com.portal.service;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.model.Proposal;
import com.portal.model.ProposalStateHistory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface IProposalStateHistoryService  {
	
	public Optional<ProposalStateHistory> getLastProposalHistory(Integer ppsId) throws AppException;
	
	public List<ProposalStateHistory> find(ProposalStateHistory model, Pageable pageable) throws AppException;
	
    public Optional<ProposalStateHistory> save(ProposalStateHistory proposalStateHistory, UserProfileDTO userProfile) throws AppException;

    public Optional<ProposalStateHistory> saveLog(Proposal proposal, UserProfileDTO userProfile) throws AppException;
}
