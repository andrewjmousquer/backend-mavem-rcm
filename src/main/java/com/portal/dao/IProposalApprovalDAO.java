package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.dto.ProposalApprovalDetailDTO;
import com.portal.dto.ProposalApprovalFilterDTO;
import com.portal.dto.ProposalApprovalListDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalApproval;

public interface IProposalApprovalDAO extends IBaseDAO<ProposalApproval> {

    public List<ProposalApproval> find(ProposalApproval model, Pageable pageable) throws AppException;

    public List<ProposalApprovalListDTO> listAll(Pageable pageable) throws AppException;

    public List<ProposalApprovalListDTO> search(ProposalApprovalFilterDTO filterDTO, Integer proposalDaysLimit,  Pageable pageable) throws AppException;

    public Optional<ProposalApprovalDetailDTO> getByIdProposalAppoval(Integer id) throws AppException, BusException;

}
