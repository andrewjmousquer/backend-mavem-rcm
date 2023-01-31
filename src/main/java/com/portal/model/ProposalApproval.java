package com.portal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.portal.dto.ProposalApprovalDTO;
import com.portal.enums.ProposalState;

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
public class ProposalApproval {

    private Proposal proposal;

    private LocalDateTime date;

    private Person person;

    private ProposalState status;

    private String comment;

    private BigDecimal discount;


    public static ProposalApproval toEntity(ProposalApprovalDTO dto) {
        if (dto == null) {
            return null;
        }

        return ProposalApproval.builder()
                .proposal(dto.getProposal())
                .date(dto.getDate())
                .status(dto.getStatus())
                .comment(dto.getComment())
                .discount(dto.getDiscount())
                .build();
    }

    public static List<ProposalApproval> toEntity(List<ProposalApprovalDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream().map(ProposalApproval::toEntity).collect(Collectors.toList());
    }


}
