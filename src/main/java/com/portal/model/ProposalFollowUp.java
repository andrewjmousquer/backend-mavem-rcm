package com.portal.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.ProposalFollowUpDTO;
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
public class ProposalFollowUp {

    @EqualsAndHashCode.Include
    @Null(groups = {ValidationHelper.OnSave.class})
    @NotNullNotZero(groups = {ValidationHelper.OnUpdate.class})
    private Integer id;

    private LocalDateTime date;

    @NotNull(groups = {ValidationHelper.OnUpdate.class, ValidationHelper.OnSave.class})
    private Integer proposal;

    @NotNull(groups = {ValidationHelper.OnUpdate.class, ValidationHelper.OnSave.class})
    private Classifier media;

    @Size(max = 150, groups = {ValidationHelper.OnUpdate.class, ValidationHelper.OnSave.class})
    private String person;

    @Size(max = 245, groups = {ValidationHelper.OnUpdate.class, ValidationHelper.OnSave.class})
    private String comment;

    public static ProposalFollowUp toEntity( ProposalFollowUpDTO dto ) {

        if( dto == null ) {
            return null;
        }

        return ProposalFollowUp.builder()
                .id( dto.getId() )
                .comment( dto.getComment() )
                .proposal(dto.getProposal())
                .date( dto.getDate() )
                .media( dto.getMedia() )
                .person( dto.getPerson() )
                .build();
    }

    public static List<ProposalFollowUp> toEntity(List<ProposalFollowUpDTO> dtos ) {
        if( dtos == null ) {
            return null;
        }

        return dtos.stream()
                .map( ProposalFollowUp::toEntity )
                .collect( Collectors.toList() );
    }
    
    public ProposalFollowUp(Integer proposal) {
    	this.proposal = proposal;
    }
}
