package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.dto.UserProfileDTO;
import com.portal.model.ProposalApproval;
import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalApprovalRule;

public interface IProposalApprovalRuleService extends IBaseService<ProposalApprovalRule> {

    public List<ProposalApprovalRule> searchForm(String text, Pageable pageable) throws AppException, BusException;

    public List<ProposalApprovalRule> listAll(Pageable pageable) throws AppException, BusException;

    public List<ProposalApprovalRule> find(ProposalApprovalRule model, Pageable pageable) throws AppException, BusException ;

    public List<ProposalApprovalRule> search(ProposalApprovalRule model, Pageable pageable) throws AppException, BusException ;

    public void validateRuleApproval(ProposalApproval model, UserProfileDTO userProfile) throws AppException, BusException;

    public Optional<ProposalApprovalRule> getByJob(Integer id) throws AppException;

}
