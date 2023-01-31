package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Product;

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
public class ProductDTO {

	@EqualsAndHashCode.Include
	private Integer id;

	private String name;

	private Boolean active;

	private Integer proposalExpirationDays;

	private String productDescription;
	
	private List<ProductModelDTO> models;
	

	public ProductDTO(Integer id, String name, Boolean active, Integer proposalExpirationDays, String productDescription) {
		this.id = id;
		this.name = name;
		this.active = active;
		this.proposalExpirationDays = proposalExpirationDays;
		this.productDescription = productDescription;
	}
    public static ProductDTO toDTO(Product model) {

		if (model == null) {
			return null;
		}

		return ProductDTO.builder()
				.id(model.getId())
				.name(model.getName())
				.active(model.getActive())
				.proposalExpirationDays(model.getProposalExpirationDays())
				.productDescription(model.getProductDescription())
				.models(ProductModelDTO.toDTO(model.getModels()))
							.build();
	}

	public static List<ProductDTO> toDTO( List<Product> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( ProductDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
