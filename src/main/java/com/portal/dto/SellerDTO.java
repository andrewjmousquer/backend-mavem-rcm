package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Seller;

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
public class SellerDTO {

    @EqualsAndHashCode.Include
    private Integer id;
    private PersonDTO person;
    private UserDTO user;
    private JobDTO job;
    private List<SalesTeamDTO> salesTeamList;
    private List<PartnerDTO> partnerList;
    private List<SellerDTO> agentList;

    public static SellerDTO toDTO(Seller seller) {
        if (seller == null) {
            return null;
        }

        return SellerDTO.builder()
                .id(seller.getId())
                .person(PersonDTO.toDTO(seller.getPerson()))
                .user(UserDTO.toDTO(seller.getUser()))
                .job(JobDTO.toDTO(seller.getJob()))
                .salesTeamList(SalesTeamDTO.toDTO(seller.getSalesTeamList()))
                .partnerList(PartnerDTO.toDTO(seller.getPartnerList()))
                .agentList(SellerDTO.toDTO(seller.getAgentList()))
                .build();
    }

    public static List<SellerDTO> toDTO(List<Seller> sellers) {
        if (sellers == null) {
            return null;
        }
        return sellers.stream().map(SellerDTO::toDTO).collect(Collectors.toList());
    }
}
