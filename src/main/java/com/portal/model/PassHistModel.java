package com.portal.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassHistModel {

	private Integer id;
	private String password;
	private Date changeDate;
	private UserModel user;

}
