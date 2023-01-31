package com.portal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.portal.enums.ProposalState;
import com.portal.model.Proposal;
import com.portal.model.ProposalApproval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProposalApprovalDTO {

    private Proposal proposal;

    private LocalDateTime date;

    private ProposalState status;

    private String comment;

    private BigDecimal discount;


    public static ProposalApprovalDTO toDTO(ProposalApproval proposalApproval) {
        if (proposalApproval == null) {
            return null;
        }

        return ProposalApprovalDTO.builder()
                .proposal(proposalApproval.getProposal())
                .date(proposalApproval.getDate())
                .status(proposalApproval.getStatus())
                .comment(proposalApproval.getComment())
                .discount(proposalApproval.getDiscount())
                .build();
    }

    public static List<ProposalApprovalDTO> toDTO(List<ProposalApproval> list) {
        if (list == null) {
            return null;
        }

        return list.stream().map(ProposalApprovalDTO::toDTO).collect(Collectors.toList());
    }

}
