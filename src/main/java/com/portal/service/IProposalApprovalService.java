package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.dto.*;
import com.portal.model.Proposal;
import com.portal.validators.ValidationHelper;
import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalApproval;

public interface IProposalApprovalService extends IBaseService<ProposalApproval> {

    public List<ProposalApprovalListDTO> listAll(Pageable pageable) throws AppException;

    public List<ProposalApproval> find(ProposalApproval model, Pageable pageable) throws AppException;

    public List<ProposalApprovalListDTO> search(ProposalApprovalFilterDTO filterDTO, UserProfileDTO userProfileDTO, Pageable pageable) throws AppException;

    public Optional<ProposalApprovalDetailDTO> getByIdProposalAppoval(Integer id) throws AppException, BusException;

    public ProposalApprovalCheckpointRules applyRulesCheckpoin(UserProfileDTO userProfile);

    public List<ProposalApprovalListDTO> fillDiscount(List<ProposalApprovalListDTO> list);
    public ProposalApproval fillDiscount(Proposal proposal);
}
