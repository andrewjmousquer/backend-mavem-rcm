package com.portal.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.Classifier;
import com.portal.model.ProposalFrontForm;
import com.portal.model.VehicleModel;

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
public class VehicleDTO {

    @EqualsAndHashCode.Include
    private Integer id;
    private String chassi;
    private String plate;
    private ModelDTO model;
    private String version;
    private Integer modelYear;
    private LocalDate purchaseDate;
    private Double purchaseValue;
    private List<ProposalFrontForm> proposals;
    private Classifier color;

    public static VehicleDTO toDTO(VehicleModel vehicleModel) {
        if (vehicleModel == null) {
            return null;
        }
        return VehicleDTO.builder()
                .id(vehicleModel.getId())
                .chassi(vehicleModel.getChassi())
                .plate(vehicleModel.getPlate())
                .model(ModelDTO.toDTO(vehicleModel.getModel()))
                .version(vehicleModel.getVersion())
                .modelYear(vehicleModel.getModelYear())
                .purchaseDate(vehicleModel.getPurchaseDate())
                .purchaseValue(vehicleModel.getPurchaseValue())
                .proposals(vehicleModel.getProposals())
                .color(vehicleModel.getColor())
                .build();
    }

    public static List<VehicleDTO> toDTO(List<VehicleModel> vehicleModelList) {
        if (vehicleModelList == null) {
            return null;
        }
        return vehicleModelList.stream()
                                    .map(VehicleDTO::toDTO)
                                    .collect(Collectors.toList());
    }
}
