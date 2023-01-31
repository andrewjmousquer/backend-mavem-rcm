package com.portal.dto;

import java.util.List;

public class CoverageDTO {

	private String value;
	private List<Integer> operatorIdList;

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<Integer> getOperatorIdList() {
		return operatorIdList;
	}
	public void setOperatorIdList(List<Integer> operatorIdList) {
		this.operatorIdList = operatorIdList;
	}
}
