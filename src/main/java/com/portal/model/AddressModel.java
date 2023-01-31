package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import com.portal.dto.AddressDTO;

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
public class AddressModel {

	private Integer id;
	private String street;
	private String number;
	private String district;
	private String complement;
	private String zipCode;
	private String latitude;
	private String longitude;
	private CityModel city;

	public AddressModel(Integer id) {
		this.id = id;
	}

	public static AddressModel toEntity(AddressDTO addressDto) {
		if (addressDto == null) {
			return null;
		}

		return AddressModel.builder()
				.id(addressDto.getId())
				.street(addressDto.getStreet())
				.number(addressDto.getNumber())
				.district(addressDto.getDistrict())
				.complement(addressDto.getComplement())
				.zipCode(addressDto.getZipCode())
				.latitude(addressDto.getLatitude())
				.longitude(addressDto.getLongitude())
				.city(addressDto.getCity())
				.build();
	}

	public static List<AddressModel> toEntity(List<AddressDTO> list) {
		if (list == null) {
			return null;
		}

		return list.stream().map(AddressModel::toEntity).collect(Collectors.toList());
	}
}
