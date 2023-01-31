package com.portal.dto;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.model.Classifier;
import com.portal.model.ProposalCommission;
import com.portal.model.ProposalDetail;
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
public class ProposalCommissionDTO {

	  @EqualsAndHashCode.Include
	    @Null(groups = {OnSave.class})
	    @NotNullNotZero(groups = {OnUpdate.class})
	    private Integer id;
	    
	    @NotNull(groups = {OnUpdate.class, OnSave.class})
		private PartnerPersonDTO partnerPerson;
	    
		private Date dueDate;

	    @NotNull(groups = {OnUpdate.class, OnSave.class})
		private Double value;
		
	    private String notes;
		
	    @NotNull(groups = {OnUpdate.class, OnSave.class})
	    private Classifier commissionType;

	    @NotNull(groups = {OnUpdate.class, OnSave.class})

	    private BankAccountDTO bankAccount;

	    @NotNull(groups = {OnUpdate.class, OnSave.class})
	    private ProposalDetail proposalDetail;
	    
	    public static ProposalCommissionDTO toDTO(ProposalCommission entity) {
	        if (entity == null) {
	            return null;
	        }
	        return ProposalCommissionDTO.builder()
	                .id(entity.getId())
	                .partnerPerson(PartnerPersonDTO.builder().person(PersonDTO.toDTO(entity.getPerson())).build())
	                .dueDate(entity.getDueDate())
	                .value(entity.getValue())
	                .notes(entity.getNotes())
	                .commissionType(entity.getCommissionType())
	                .bankAccount(BankAccountDTO.toDTO(entity.getBankAccount()))
	                .proposalDetail(entity.getProposalDetail())
	                .build();
	    }

	    public static List<ProposalCommissionDTO> toDTO(List<ProposalCommission> proposalApprovalRules) {
	        if (proposalApprovalRules == null) {
	            return null;
	        }
	        return proposalApprovalRules.stream()
	                .map(ProposalCommissionDTO::toDTO)
	                .collect(Collectors.toList());
	    }
}
