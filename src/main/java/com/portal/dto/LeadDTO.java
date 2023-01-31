package com.portal.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Classifier;
import com.portal.model.Lead;

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
public class LeadDTO {

    @EqualsAndHashCode.Include
    private Integer id;
    
    private String name;
    
    private LocalDateTime createDate;
    
    private String email;
    
    private String phone;

    private PersonDTO client;

    private SellerDTO seller;

    private SourceDTO source;
    
    private Classifier status;
    
    private ModelDTO model;

    private Classifier saleProbability;
    
    private String subject;
    
    private String description;
    
    private String searchText;

    public static LeadDTO toDTO(Lead lead) {
        if (lead == null) {
            return null;
        }
        return LeadDTO.builder()
                .id(lead.getId())
                .createDate(lead.getCreateDate())
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .description(lead.getDescription())
                .seller(SellerDTO.toDTO((lead.getSeller())))
                .source(SourceDTO.toDTO(lead.getSource()))
                .status(lead.getStatus())
                .model(ModelDTO.toDTO(lead.getModel()))
                .saleProbability(lead.getSaleProbabilty())
                .subject(lead.getSubject())
                .build();
    }

    public static List<LeadDTO> toDTO(List<Lead> leads) {
        if (leads == null) {
            return null;
        }

        return leads.stream()
                .map(LeadDTO::toDTO)
                .collect(Collectors.toList());
    }
}


