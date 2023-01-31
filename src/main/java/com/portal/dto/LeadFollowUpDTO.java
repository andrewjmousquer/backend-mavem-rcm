package com.portal.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Classifier;
import com.portal.model.LeadFollowUp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LeadFollowUpDTO {

	@EqualsAndHashCode.Include
    private Integer id;
	
	private Integer leadId;
	
	private LocalDateTime date;
	
	private String person;
	
	private Classifier media;
	
	private String comment;
	
	public static LeadFollowUpDTO toDTO(LeadFollowUp leadFollowUp) {
        if (leadFollowUp == null) {
            return null;
        }
        return LeadFollowUpDTO.builder()
                .id(leadFollowUp.getId())
                .date(leadFollowUp.getDate())
                .leadId(leadFollowUp.getLead() != null ? leadFollowUp.getLead().getId() : null)
                .media(leadFollowUp.getMedia())
                .person( leadFollowUp.getPerson() )
                .comment(leadFollowUp.getComment())
                .build();
    }

    public static List<LeadFollowUpDTO> toDTO(List<LeadFollowUp> leads) {
        if (leads == null) {
            return null;
        }

        return leads.stream()
                .map(LeadFollowUpDTO::toDTO)
                .collect(Collectors.toList());
    }
}
