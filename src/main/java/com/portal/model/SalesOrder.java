package com.portal.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.portal.enums.SalesOrderState;
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
public class SalesOrder {

	@EqualsAndHashCode.Include
	@NotNullNotZero(groups = {OnUpdate.class})
	@Null(groups = {OnSave.class})
	private Integer id;
	
	private Proposal proposal;
	
	private Integer orderNumber;
	
	private String jiraKey;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private SalesOrderState status;
	private Classifier statusClassification;
	
	private UserModel user;
	
} 