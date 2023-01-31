package com.portal.model;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.portal.dto.ItemDTO;
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
public class Item {
	
	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	@Size(max = 255, groups = {OnUpdate.class, OnSave.class})
	private String name;
	
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String cod;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer seq;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean forFree;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean generic;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier mandatory;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private ItemType itemType;
	
	@Size(max = 255, groups = {OnUpdate.class, OnSave.class})
	private String file;
	
	@Size(max = 255, groups = {OnUpdate.class, OnSave.class})
	private String icon;
	
	private String description;

	private String hyperlink;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Classifier responsability;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer term;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean termWorkDay;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean highlight;
	
	@JsonManagedReference
	private List<ItemModel> itemModels;
	
	public static Item toEntity( ItemDTO dto ) {
		
		if( dto == null ) {
			return null;
		}
		
		return Item.builder()
						.id( dto.getId() )
						.name( dto.getName() )
						.cod( dto.getCod() )
						.seq( dto.getSeq() )
						.forFree( dto.getForFree() )
						.generic( dto.getGeneric() )
						.itemType( ItemType.toEntity( dto.getItemType() ) )
						.mandatory( dto.getMandatory() )
						.description( dto.getDescription() )
						.hyperlink( dto.getHyperlink() )
						.file( dto.getFile() )
						.icon( dto.getIcon() )
						.itemModels( dto.getItemModels() )
						.responsability( dto.getResponsability() )
						.term( dto.getTerm() )
						.termWorkDay( dto.getTermWorkDay() )
						.highlight( dto.getHighlight() )
						.build();
	}
}