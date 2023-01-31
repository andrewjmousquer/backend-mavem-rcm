package com.portal.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.ProductDTO;
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
public class Product {

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
	private Integer proposalExpirationDays;

	@Size(max = 255, groups = {OnUpdate.class, OnSave.class})
	private String productDescription;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private List<ProductModel> models;
	

	public Product(Integer id, String name, Boolean active, Integer proposalExpirationDays, String productDescription) {
		this.id = id;
		this.name = name;
		this.active = active;
		this.proposalExpirationDays = proposalExpirationDays;
		this.models  =  new ArrayList<>();
	}

	public Product(Product product) {
		this.id = product.getId();
		this.name = product.getName();
		this.active = product.getActive();
		this.proposalExpirationDays = product.getProposalExpirationDays();
		this.productDescription = product.getProductDescription();
		
	}


	public static Product toEntity(ProductDTO dto) {

		if (dto == null) {
			return null;
		}

		return Product.builder()
				.id(dto.getId())
				.name(dto.getName())
				.active(dto.getActive())
				.proposalExpirationDays(dto.getProposalExpirationDays())
				.productDescription(dto.getProductDescription())
				.models(ProductModel.toEntity(dto.getModels()))
						.build();
	}

	public void setProductModelList(ProductModel pm) {
		this.models.add(pm);
	}

	public void initializerProductModelList() {
		List<ProductModel> productModelList =  new ArrayList<>();
		this.models = productModelList;
	}
}
