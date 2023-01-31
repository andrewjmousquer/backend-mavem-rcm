package com.portal.service;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalFollowUp;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IProposalFollowUp extends IBaseService<ProposalFollowUp>{

        Optional<ProposalFollowUp> save(ProposalFollowUp model) throws AppException;

        Optional<ProposalFollowUp> update(ProposalFollowUp model) throws AppException;

        Optional<ProposalFollowUp> getById(Integer id) throws AppException, BusException;

        List<ProposalFollowUp> listAll(Pageable pageable) throws AppException, BusException;


    void delete(Integer id) throws AppException;
}
