package com.portal.service.imp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.portal.dao.IProposalStateHistoryDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.ProposalState;
import com.portal.exceptions.AppException;
import com.portal.model.Proposal;
import com.portal.model.ProposalStateHistory;
import com.portal.service.IProposalStateHistoryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProposalStateHistoryService implements IProposalStateHistoryService {

    @Autowired
    private IProposalStateHistoryDAO proposalStateHistoryDAO;

    @Autowired
    public MessageSource messageSource;

    @Override
    public Optional<ProposalStateHistory> getLastProposalHistory(Integer ppsId) throws AppException {
    	return proposalStateHistoryDAO.getLastProposalHistory(ppsId);
    }
	
    @Override
	public List<ProposalStateHistory> find(ProposalStateHistory model, Pageable pageable) throws AppException {
		return proposalStateHistoryDAO.find(model, pageable);
	}
	
    @Override
    public Optional<ProposalStateHistory> save(ProposalStateHistory proposalStateHistory, UserProfileDTO userProfile) throws AppException{
    	return proposalStateHistoryDAO.save(proposalStateHistory, userProfile);
    }

    @Override
    public Optional<ProposalStateHistory> saveLog(Proposal proposal, UserProfileDTO userProfile) throws AppException {
    	Optional<ProposalStateHistory> returnStatus = null;
    	
    	ProposalStateHistory proposalStateHistory = new ProposalStateHistory();
        proposalStateHistory.setProposal(proposal);
        proposalStateHistory.setUser(userProfile.getUser());
        proposalStateHistory.setStatusDate(LocalDateTime.now());
        proposalStateHistory.setStatusNew(proposal.getStatus().getType());
    	
    	Optional<ProposalStateHistory> lastHistory = this.getLastProposalHistory(proposal.getId());
    	if(lastHistory.isPresent()) {
    		if(!lastHistory.get().getStatusNew().equals(proposal.getStatus().getType())) {
    	        proposalStateHistory.setStatusOld(lastHistory.get().getStatusNew());
    	        returnStatus = this.proposalStateHistoryDAO.save(proposalStateHistory, userProfile);
    		}
    	} else {
    		returnStatus = this.proposalStateHistoryDAO.save(proposalStateHistory, userProfile);
    	}
    	
    	return returnStatus;
    }
}
