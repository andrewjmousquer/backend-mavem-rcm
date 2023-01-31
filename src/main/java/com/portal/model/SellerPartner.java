package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.dto.SellerPartnerDTO;

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
public class SellerPartner {

    private Seller seller;
    private Partner partner;

    public static SellerPartner toEntity(SellerPartnerDTO sellerPartnerDTO) {
        if (sellerPartnerDTO == null) {
            return null;
        }

        return SellerPartner.builder()
                .seller(Seller.toEntity(sellerPartnerDTO.getSeller()))
                .partner(Partner.toEntity(sellerPartnerDTO.getPartner()))
                .build();
    }

    public static List<SellerPartner> toEntity(List<SellerPartnerDTO> list) {
        if (list == null) {
            return null;
        }

        return list.stream().map(SellerPartner::toEntity).collect(Collectors.toList());
    }
}
