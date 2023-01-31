package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.ProposalApprovalRule;

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
public class ProposalApprovalRuleDTO {

    @EqualsAndHashCode.Include
    private Integer id;

    private Double value;

    private JobDTO job;

    public static ProposalApprovalRuleDTO toDTO(ProposalApprovalRule proposalApprovalRule) {
        if (proposalApprovalRule == null) {
            return null;
        }
        return ProposalApprovalRuleDTO.builder()
                .id(proposalApprovalRule.getId())
                .value(proposalApprovalRule.getValue())
                .job(JobDTO.toDTO(proposalApprovalRule.getJob()))
                .build();
    }

    public static List<ProposalApprovalRuleDTO> toDTO(List<ProposalApprovalRule> proposalApprovalRules) {
        if (proposalApprovalRules == null) {
            return null;
        }
        return proposalApprovalRules.stream()
                .map(ProposalApprovalRuleDTO::toDTO)
                .collect(Collectors.toList());
    }
}
