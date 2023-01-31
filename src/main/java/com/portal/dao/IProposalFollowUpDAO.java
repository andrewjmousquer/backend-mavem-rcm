package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.ProposalFollowUp;

public interface IProposalFollowUpDAO extends IBaseDAO<ProposalFollowUp> {
    Optional<ProposalFollowUp> save(ProposalFollowUp model) throws AppException;

    Optional<ProposalFollowUp> update(ProposalFollowUp model) throws AppException;

    Optional<ProposalFollowUp> getById(Integer id) throws AppException;

    void delete(Integer id) throws AppException;

    List<ProposalFollowUp> listAll(Pageable pageable) throws AppException;
}
