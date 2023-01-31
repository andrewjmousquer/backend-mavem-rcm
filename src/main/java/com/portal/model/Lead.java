package com.portal.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.LeadDTO;
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
public class Lead {

	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private String name;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private LocalDateTime createDate;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private String email;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private String phone;
	
	private Person client;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Seller seller;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Source source;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier status;

	private Model model;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier saleProbabilty;

	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String subject;
	
	private String description;

	public static Lead toEntity(LeadDTO leadDTO) {
		if (leadDTO == null) {
			return null;
		}
		return Lead.builder()
				.id(leadDTO.getId())
				.createDate(leadDTO.getCreateDate())
				.name(leadDTO.getName())
				.email(leadDTO.getEmail())
				.phone(leadDTO.getPhone())
				.description(leadDTO.getDescription())
				.seller(Seller.toEntity(leadDTO.getSeller()))
				.source(Source.toEntity(leadDTO.getSource()))
				.status(leadDTO.getStatus())
				.model(Model.toEntity(leadDTO.getModel()))
				.saleProbabilty(leadDTO.getSaleProbability())
				.subject(leadDTO.getSubject())
				.build();
	}

	public static List<Lead> toEntity(List<LeadDTO> list) {
		if (list == null) {
			return null;
		}

		return list.stream().map(Lead::toEntity).collect(Collectors.toList());
	}
}