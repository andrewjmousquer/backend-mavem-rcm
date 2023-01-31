package com.portal.dto.proposal;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Proposal;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class ProposalDTO {

	private Proposal proposal;

	public static ProposalDTO toDTO(Proposal entity) {
        if (entity == null) {
            return null;
        }
        return ProposalDTO.builder()
                .proposal(entity)
                .build();
    }

	public static List<ProposalDTO> toDTO(List<Proposal> proposalApprovalRules) {
	    if (proposalApprovalRules == null) {
	        return null;
	    }
	    return proposalApprovalRules.stream()
	            .map(ProposalDTO::toDTO)
	            .collect(Collectors.toList());
	}
	
}
