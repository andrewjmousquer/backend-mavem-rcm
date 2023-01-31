package com.portal.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.portal.dto.proposal.ProposalDTO;
import com.portal.enums.ProposalRisk;
import com.portal.enums.ProposalState;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proposal {

	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;
 	
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Long num;
	
	@Null(groups = {OnSave.class })
	private String proposalNumber;
	
	@NotBlank(groups = {OnUpdate.class})
	@Size(max = 1, groups = {OnUpdate.class})
	private String cod;
	
	private SalesOrder salesOrder;
	
	private LocalDateTime createDate;

	private LocalDateTime validityDate;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ProposalState status;
	private Classifier statusClassification;
	
	private Lead lead;

	@JsonManagedReference
	private ProposalDetail proposalDetail;
	private ProposalDetailVehicle proposalDetailVehicle;
	private List<ProposalDetailVehicleItem> proposalDetailVehicleItem;
	private List<ProposalPayment> proposalPayment;
	
	private List<ProposalPerson> personList; 
	
	private List<ProposalCommission> proposalCommission;
	
	private List<ProposalFollowUp> proposalFollowUp;
	
	private Boolean finantialContact;
	private String finantialContactName;
	private String finantialContactEmail;
	private String finantialContactPhone;
	
	private Boolean documentContact;
	private String documentContactName;
	private String documentContactEmail;
	private String documentContactPhone;
	
	private String commercialContactName;
	private String commercialContactEmail;
	private String commercialContactPhone;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ProposalRisk risk;
	private Classifier riskClassification;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean immediateDelivery;
	
	private String contract;

	private List<Document> documents;

	public String getVerion() {
		return ( this.num + "-" + this.cod );
	}
	
	
	public static Proposal toEntity(ProposalDTO dto) {
        if (dto == null) {
            return null;
        }
        return Proposal.builder()
                .build();
    }

    public static List<Proposal> toEntity(List<ProposalDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(Proposal::toEntity)
                .collect(Collectors.toList());

    }
	
} 