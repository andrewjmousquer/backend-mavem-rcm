package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.model.ProposalStateHistory;

public interface IProposalStateHistoryDAO {
	
	public Optional<ProposalStateHistory> getLastProposalHistory(Integer ppsId) throws AppException;
	
	public List<ProposalStateHistory> find(ProposalStateHistory model, Pageable pageable) throws AppException;
	
    public Optional<ProposalStateHistory> save(ProposalStateHistory proposalStateHistory, UserProfileDTO userProfile) throws AppException;
}
