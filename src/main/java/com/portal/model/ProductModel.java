package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.dto.ProductModelDTO;
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
public class ProductModel {
	
	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Boolean hasProject;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer modelYearStart;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer modelYearEnd;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer manufactureDays;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Product product;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Model model;

    public static ProductModel toEntity(ProductModelDTO dto) {
		if(dto == null){
			return null;
		}

		return ProductModel.builder()
				.id(dto.getId())
				.hasProject(dto.getHasProject())
				.modelYearStart(dto.getModelYearStart())
				.modelYearEnd(dto.getModelYearEnd())
				.manufactureDays(dto.getManufactureDays())
				.product(Product.toEntity(dto.getProduct()))
				.model(Model.toEntity(dto.getModel()))
				.build();
    }

	public static List<ProductModel> toEntity(List<ProductModelDTO> productModelDTOS) {

		if( productModelDTOS == null ) {
			return null;
		}

		return productModelDTOS.stream()
				.map( ProductModel::toEntity )
				.collect( Collectors.toList() );
	}


}
