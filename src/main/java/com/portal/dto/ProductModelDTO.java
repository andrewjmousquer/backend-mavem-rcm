package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.ProductModel;

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
public class ProductModelDTO {

    @EqualsAndHashCode.Include
    private Integer id;
    private Boolean hasProject;
    private Integer modelYearStart;
    private Integer modelYearEnd;
    private Integer manufactureDays;
    private ProductDTO product;
    private ModelDTO model;

    public static ProductModelDTO toDTO(ProductModel productModel) {
        
    	if (productModel == null) {
            return null;
        }
        
        return ProductModelDTO.builder()
				                .id(productModel.getId())
				                .hasProject(productModel.getHasProject())
				                .modelYearStart(productModel.getModelYearStart())
				                .modelYearEnd(productModel.getModelYearEnd())
				                .manufactureDays(productModel.getManufactureDays())
				                .product(ProductDTO.toDTO(productModel.getProduct()))
				                .model(ModelDTO.toDTO(productModel.getModel()))
				                .build();
    }

    public static List<ProductModelDTO> toDTO(List<ProductModel> productModelList) {

    	if (productModelList == null) {
            return null;
        }
        
        return productModelList.stream()
                .map(ProductModelDTO::toDTO)
                .collect(Collectors.toList());
    }
}
