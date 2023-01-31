package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.SellerPartner;

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
public class SellerPartnerDTO {

    private SellerDTO seller;

    private PartnerDTO partner;

    public static SellerPartnerDTO toDTO(SellerPartner sellerPartner) {
        if (sellerPartner == null) {
            return null;
        }

        return SellerPartnerDTO.builder()
                .seller(SellerDTO.toDTO(sellerPartner.getSeller()))
                .partner(PartnerDTO.toDTO(sellerPartner.getPartner()))
                .build();
    }

    public static List<SellerPartnerDTO> toDTO(List<SellerPartner> sellerPartners) {
        if (sellerPartners == null) {
            return null;
        }

        return sellerPartners.stream().map(SellerPartnerDTO::toDTO).collect(Collectors.toList());
    }
}
