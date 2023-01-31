package com.portal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckpointModel {

	private Integer id;
	private String name;
	private String description;

	public CheckpointModel(String name) {
		this.name = name;
	}
	
}
