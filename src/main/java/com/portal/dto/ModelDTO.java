package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
import com.portal.model.Model;

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
public class ModelDTO {

	@EqualsAndHashCode.Include
	private Integer id;
	private String name;
	private Boolean active;
	private BrandDTO brand;
	private String codFipe;
	private ModelBodyType bodyType;
	private ModelSize size;
	private ModelCategory category;

	public static ModelDTO toDTO( Model model ) {
		
		if( model == null ) {
			return null;
		}
		
		return ModelDTO.builder()
							.id( model.getId() )
							.name( model.getName() )
							.active( model.getActive() )
							.brand( BrandDTO.toDTO( model.getBrand() ) )
							.bodyType( model.getBodyType() )
							.size( model.getSize() )
							.category( model.getCategory() )
							.codFipe( model.getCodFipe() )
							.build();
	}

	public static List<ModelDTO> toDTO( List<Model> models ) {
		
		if( models == null ) {
			return null;
		}
		
		return models.stream()
						.map( ModelDTO::toDTO )
						.collect( Collectors.toList() );	
		
	}
}
