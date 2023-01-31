package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.SalesTeam;

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
public class SalesTeamDTO {


    @EqualsAndHashCode.Include
    private Integer id;

    private String name;
    
    private List<SellerDTO> sellerList;
    
    public static SalesTeamDTO toDTO(SalesTeam salesTeam) {
        if (salesTeam == null) {
            return null;
        }

        return SalesTeamDTO.builder()
                .id(salesTeam.getId())
                .name(salesTeam.getName())
                .sellerList(SellerDTO.toDTO(salesTeam.getSellerList()))
                .build();
    }

    public static List<SalesTeamDTO> toDTO(List<SalesTeam> salesTeamList) {
        if (salesTeamList == null) {
            return null;
        }

        return salesTeamList.stream().map(SalesTeamDTO::toDTO).collect(Collectors.toList());
    }
}
