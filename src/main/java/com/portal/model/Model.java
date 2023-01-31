package com.portal.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.ModelDTO;
import com.portal.enums.ModelBodyType;
import com.portal.enums.ModelCategory;
import com.portal.enums.ModelSize;
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
public class Model {
	
	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String name;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean active;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Brand brand;
	
	@Size(max = 15, groups = {OnUpdate.class, OnSave.class})
	private String codFipe;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ModelBodyType bodyType;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ModelSize size;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ModelCategory category;
	
	public static Model toEntity( ModelDTO dto ) {
		
		if( dto == null ) {
			return null;
		}
		
		return Model.builder()
						.id( dto.getId() )
						.name( dto.getName() )
						.active( dto.getActive() )
						.brand( Brand.toEntity( dto.getBrand() ) )
						.bodyType( dto.getBodyType() )
						.size( dto.getSize() )
						.category( dto.getCategory() )
						.codFipe( dto.getCodFipe() )
						.build();
	}
}
