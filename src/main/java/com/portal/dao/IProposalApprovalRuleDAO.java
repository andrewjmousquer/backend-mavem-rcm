package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ProposalApprovalRule;

public interface IProposalApprovalRuleDAO extends IBaseDAO<ProposalApprovalRule> {

    public List<ProposalApprovalRule> find(ProposalApprovalRule model, Pageable pageable) throws AppException;

    public List<ProposalApprovalRule> listAll(Pageable pageable) throws AppException;

    public List<ProposalApprovalRule> search(ProposalApprovalRule model, Pageable pageable) throws AppException;

    public List<ProposalApprovalRule> searchForm(String text, Pageable pageable) throws AppException;

    public Optional<ProposalApprovalRule> getByJob(Integer id) throws AppException;
}
