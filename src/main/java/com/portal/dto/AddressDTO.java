package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.model.AddressModel;
import com.portal.model.CityModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    @EqualsAndHashCode.Include
    private Integer id;
    private String street;
    private String number;
    private String district;
    private String complement;
    private String zipCode;
    private String latitude;
    private String longitude;
    private CityModel city;

    public static AddressDTO toDTO(AddressModel addressModel) {
        if (addressModel == null) {
            return null;
        }
        return AddressDTO.builder()
                .id(addressModel.getId())
                .street(addressModel.getStreet())
                .number(addressModel.getNumber())
                .district(addressModel.getDistrict())
                .complement(addressModel.getComplement())
                .zipCode(addressModel.getZipCode())
                .latitude(addressModel.getLatitude())
                .longitude(addressModel.getLongitude())
                .city(addressModel.getCity())
                .build();
    }

    public static List<AddressDTO> toDTO(List<AddressModel> addressModelList) {
        if (addressModelList == null) {
            return null;
        }
        return addressModelList.stream()
                .map(AddressDTO::toDTO)
                .collect(Collectors.toList());
    }

}
