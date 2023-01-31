package com.portal.model;

/**
 * @author Ederson Sergio Monteiro Coelho
 *
 */

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.dto.ProductModelCostDTO;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductModelCost {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ProductModel productModel;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private LocalDate startDate;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private LocalDate endDate;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private BigDecimal totalValue;

    public static ProductModelCost toEntity(ProductModelCostDTO dto) {

    	if(dto == null){
			return null;
		}

		return ProductModelCost.builder()
							   .id(dto.getId())
							   .productModel(dto.getProductModel())
							   .startDate(dto.getStartDate())
							   .endDate(dto.getEndDate())
							   .totalValue(dto.getTotalValue())
							   .build();
    }

	public static List<ProductModelCost> toEntity(List<ProductModelCostDTO> productModelCostDTOS) {

		if( productModelCostDTOS == null ) {
			return null;
		}

		return productModelCostDTOS.stream()
								   .map( ProductModelCost::toEntity )
								   .collect( Collectors.toList() );
	}
}