package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.BrandDTO;
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
public class Brand {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String name;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean active;
	

	public static Brand toEntity( BrandDTO dto ) {
		
		if( dto == null ) {
			return null;
		}
		
		return Brand.builder()
				.id( dto.getId() )
				.name( dto.getName() )
				.active( dto.getActive() )
				.build();
	}
	
	public static List<Brand> toEntity( List<BrandDTO> dtos ) {
		
		if( dtos == null ) {
			return null;
		}
		
		return dtos.stream()
						.map( Brand::toEntity )
						.collect( Collectors.toList() );	
		
	}
}
