package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Null;

import com.portal.dto.SalesTeamDTO;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SalesTeam {

    @EqualsAndHashCode.Include
    @Null(groups = {ValidationHelper.OnSave.class})
    @NotNullNotZero(groups = {ValidationHelper.OnUpdate.class})
    private Integer id;

    private String name;

    private List<Seller> sellerList;
    
    public static SalesTeam toEntity(SalesTeamDTO salesTeamDTO) {
        if (salesTeamDTO == null) {
            return null;
        }

        return SalesTeam.builder()
                .id(salesTeamDTO.getId())
                .name(salesTeamDTO.getName())
                .sellerList(Seller.toEntity(salesTeamDTO.getSellerList()))
                .build();
    }

    public static List<SalesTeam> toEntity(List<SalesTeamDTO> list) {
        if (list == null) {
            return null;
        }

        return list.stream().map(SalesTeam::toEntity).collect(Collectors.toList());
    }
}
