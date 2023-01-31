package com.portal.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductWithPriceListIdDTO {

    private Integer id;
    private String name;
    private Boolean active;
    private Integer proposalExpirationDays;
    private Integer prlId;
    private BigDecimal over_parceiro;
}
