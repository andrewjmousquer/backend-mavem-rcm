package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Classifier;
import com.portal.model.PartnerPersonCommission;

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
public class PartnerPersonCommissionDTO {
	
	private Double defaultValue;
	
	private Classifier commissionType;
	
	private Double commissionDefaultValue;
	
	private PersonDTO person;
	
	private PartnerDTO partner;
	
	public static PartnerPersonCommissionDTO toDTO(PartnerPersonCommission entity) {
        if (entity == null) {
            return null;
        }

        return PartnerPersonCommissionDTO.builder()
                .defaultValue(entity.getDefaultValue())
                .commissionType(entity.getCommissionType())
                .person(PersonDTO.toDTO(entity.getPerson()))
                .partner(PartnerDTO.toDTO(entity.getPartner()))
                .build();
    }

    public static List<PartnerPersonCommissionDTO> toDTO(List<PartnerPersonCommission> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(PartnerPersonCommissionDTO::toDTO).collect(Collectors.toList());
    }
}
