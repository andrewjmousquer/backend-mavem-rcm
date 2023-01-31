package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.dto.ProposalApprovalRuleDTO;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

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
public class ProposalApprovalRule {

    @EqualsAndHashCode.Include
    @Null(groups = {OnSave.class})
    @NotNullNotZero(groups = {OnUpdate.class})
    private Integer id;

    @NotNull(groups = {OnUpdate.class, OnSave.class})
    private Double value;

    @NotNull(groups = {OnUpdate.class, OnSave.class})
    private Job job;

    public static ProposalApprovalRule toEntity(ProposalApprovalRuleDTO dto) {
        if (dto == null) {
            return null;
        }
        return ProposalApprovalRule.builder()
                .id(dto.getId())
                .value(dto.getValue())
                .job(Job.toEntity(dto.getJob()))
                .build();
    }

    public static List<ProposalApprovalRule> toEntity(List<ProposalApprovalRuleDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(ProposalApprovalRule::toEntity)
                .collect(Collectors.toList());

    }

}
