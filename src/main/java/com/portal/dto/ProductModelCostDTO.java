package com.portal.dto;

/**
 * @author Ederson Sergio Monteiro Coelho
 *
 */

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Ederson Sergio Monteiro Coelho
 *
 */

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.ProductModel;
import com.portal.model.ProductModelCost;

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
public class ProductModelCostDTO {

    @EqualsAndHashCode.Include
	private Integer id;
	private ProductModel productModel;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal totalValue;

    public static ProductModelCostDTO toDTO(ProductModelCost productModelCost) {

    	if (productModelCost == null) {
            return null;
        }

        return ProductModelCostDTO.builder()
				                .id(productModelCost.getId())
				                .productModel(productModelCost.getProductModel())
				                .startDate(productModelCost.getStartDate())
				                .endDate(productModelCost.getEndDate())
				                .totalValue(productModelCost.getTotalValue())
				                .build();
    }

    public static List<ProductModelCostDTO> toDTO(List<ProductModelCost> productModelCostList) {

    	if (productModelCostList == null) {
            return null;
        }

        return productModelCostList.stream()
        						   .map(ProductModelCostDTO::toDTO)
        						   .collect(Collectors.toList());
    }
}