package com.portal.model;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.dto.ProposalCommissionDTO;
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
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProposalCommission {

    @EqualsAndHashCode.Include
    @Null(groups = {OnSave.class})
    @NotNullNotZero(groups = {OnUpdate.class})
    private Integer id;
    
    @NotNull(groups = {OnUpdate.class, OnSave.class})
	private Person person;
    
    private PartnerPerson partnerPerson;
    
	private Date dueDate;

    @NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double value;
	
    private String notes;
	
    @NotNull(groups = {OnUpdate.class, OnSave.class})
    private Classifier commissionType;

    @NotNull(groups = {OnUpdate.class, OnSave.class})
    private BankAccount bankAccount;

    @NotNull(groups = {OnUpdate.class, OnSave.class})
    private ProposalDetail proposalDetail;
	
    public static ProposalCommission toEntity(ProposalCommissionDTO dto) {
        if (dto == null) {
            return null;
        }
        return ProposalCommission.builder()
                .id(dto.getId())
                .person(Person.toEntity(dto.getPartnerPerson().getPerson()))
                .dueDate(dto.getDueDate())
                .value(dto.getValue())
                .notes(dto.getNotes())
                .commissionType(dto.getCommissionType())
                .bankAccount(BankAccount.toEntity(dto.getBankAccount()))
                .proposalDetail(dto.getProposalDetail())
                .build();
    }

    public static List<ProposalCommission> toEntity(List<ProposalCommissionDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(ProposalCommission::toEntity)
                .collect(Collectors.toList());

    }
}
