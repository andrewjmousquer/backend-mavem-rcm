package com.portal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StateModel {

	private Integer id;
	private String name;
	private String abbreviation;
	private CountryModel country;

}
