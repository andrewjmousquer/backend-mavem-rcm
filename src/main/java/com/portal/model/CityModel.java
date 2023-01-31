package com.portal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityModel {

	private Integer id;
	private String name;
	private Integer codIbge;
	private StateModel state;


}
