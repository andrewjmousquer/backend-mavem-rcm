package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Brand;

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
public class BrandDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	
	private String name;
	
	private Boolean active;
	

	public static BrandDTO toDTO( Brand brand ) {
		
		if( brand == null ) {
			return null;
		}
		
		return BrandDTO.builder()
				.id( brand.getId() )
				.name( brand.getName() )
				.active( brand.getActive() )
				.build();
	}

	public static List<BrandDTO> toDTO( List<Brand> brands ) {
		
		if( brands == null ) {
			return null;
		}
		
		return brands.stream()
						.map( BrandDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
