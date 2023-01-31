package com.portal.model;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.VehicleDTO;
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
public class VehicleModel {

	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;

	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String chassi;

	private String plate;

	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Model model;
	
	@Size(max = 100, groups = {OnUpdate.class, OnSave.class})
	private String version;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Integer modelYear;

	private LocalDate purchaseDate;

	private Double purchaseValue;

	private List<ProposalFrontForm> proposals;

    private Classifier color;
	
	public void setBrand(Brand brand) {
		this.model.setBrand(brand);
	}

	public static VehicleModel toEntity(VehicleDTO vehicleDTO) {
		if (vehicleDTO == null) {
			return null;
		}
		return VehicleModel.builder()
				.id(vehicleDTO.getId())
				.chassi(vehicleDTO.getChassi())
				.plate(vehicleDTO.getPlate())
				.model(Model.toEntity(vehicleDTO.getModel()))
				.version(vehicleDTO.getVersion())
				.modelYear(vehicleDTO.getModelYear())
				.purchaseDate(vehicleDTO.getPurchaseDate())
				.purchaseValue(vehicleDTO.getPurchaseValue())
				.proposals(vehicleDTO.getProposals())
				.color(vehicleDTO.getColor())
				.build();
	}
} 