package com.portal.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.ItemTypeDTO;
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
public class ItemType {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String name;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean mandatory;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean multi;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer seq;

	public static ItemType toEntity( ItemTypeDTO dto ) {
		
		if( dto == null ) {
			return null;
		}
		
		return ItemType.builder()
							.id( dto.getId() )
							.name( dto.getName() )
							.mandatory( dto.getMandatory() )
							.multi( dto.getMulti() )
							.seq( dto.getSeq() )
							.build();
	}
}
