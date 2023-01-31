package com.portal.dto;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.model.Classifier;
import com.portal.model.ProposalFollowUp;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper;

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
public class ProposalFollowUpDTO {

    @EqualsAndHashCode.Include
    @NotNullNotZero(groups = {ValidationHelper.OnUpdate.class})
    @Null(groups = {ValidationHelper.OnSave.class})
    private Integer id;

    @NotNull(groups = {ValidationHelper.OnUpdate.class, ValidationHelper.OnSave.class})
    private LocalDateTime date;

    private Integer proposal;

    private Classifier media;

    @Size(max = 150, groups = {ValidationHelper.OnUpdate.class, ValidationHelper.OnSave.class})
    private String person;

    @Size(max = 245, groups = {ValidationHelper.OnUpdate.class, ValidationHelper.OnSave.class})
    private String comment;

    public static ProposalFollowUpDTO toDTO(ProposalFollowUp proposalFollowUp) {
        if (proposalFollowUp == null) {
            return null;
        }

        return ProposalFollowUpDTO.builder()
                .id(proposalFollowUp.getId())
                .proposal(proposalFollowUp.getProposal())
                .date(proposalFollowUp.getDate())
                .comment(proposalFollowUp.getComment())
                .media(proposalFollowUp.getMedia())
                .person(proposalFollowUp.getPerson())
                .build();
    }

    public static List<ProposalFollowUpDTO> toDTO(List<ProposalFollowUp> proposalFollowUp) {
        if (proposalFollowUp == null) {
            return null;
        }
        return proposalFollowUp.stream()
                .map(ProposalFollowUpDTO::toDTO)
                .collect(Collectors.toList());
    }
}
