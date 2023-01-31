package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.portal.dto.PartnerPersonCommissionDTO;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PartnerPersonCommission {
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier commissionType;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Double defaultValue;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Person person;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Partner partner;
	
	public PartnerPersonCommission(Person person, Partner partner, Classifier type) {
		this.person = person;
		this.partner = partner;
		this.commissionType = type;
	}
	
	public static PartnerPersonCommission toEntity(PartnerPersonCommissionDTO entity) {
        if (entity == null) {
            return null;
        }

        return PartnerPersonCommission.builder()
                .defaultValue(entity.getDefaultValue())
                .commissionType(entity.getCommissionType())
                .person(Person.toEntity(entity.getPerson()))
                .partner(Partner.toEntity(entity.getPartner()))
                .build();
    }

    public static List<PartnerPersonCommission> toEntity(List<PartnerPersonCommissionDTO> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(PartnerPersonCommission::toEntity).collect(Collectors.toList());
    }
}