package com.portal.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.dto.LeadFollowUpDTO;
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
public class LeadFollowUp {
	
	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private LocalDateTime date;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private String person;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier media;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Lead lead;
	
	private String comment;
	
	public static LeadFollowUp toEntity(LeadFollowUpDTO leadFollowUpDTO) {
		if (leadFollowUpDTO == null) {
			return null;
		}
		return LeadFollowUp.builder()
				.id( leadFollowUpDTO.getId() )
				.date( leadFollowUpDTO.getDate() )
				.lead( Lead.builder().id(leadFollowUpDTO.getLeadId()).build() )
				.media( leadFollowUpDTO.getMedia() )
				.person( leadFollowUpDTO.getPerson() )
				.comment( leadFollowUpDTO.getComment() )
				.build();
	}
	
	public static List<LeadFollowUp> toEntity(List<LeadFollowUpDTO> list) {
		if (list == null) {
			return null;
		}

		return list.stream().map(LeadFollowUp::toEntity).collect(Collectors.toList());
	}
}
