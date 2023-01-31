package com.portal.dto;

import java.util.List;

public class SummaryReportMealDTO {

	private String name;
	private List<SummaryReportTypeDTO> list;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<SummaryReportTypeDTO> getList() {
		return list;
	}
	public void setList(List<SummaryReportTypeDTO> list) {
		this.list = list;
	}
}
