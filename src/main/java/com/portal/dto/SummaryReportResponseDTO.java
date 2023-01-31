package com.portal.dto;

import java.util.Calendar;
import java.util.List;

public class SummaryReportResponseDTO {

	private Calendar date;
	private List<SummaryReportMealDTO> pacientList;
	private List<SummaryReportMealDTO> employeeList;

	public Calendar getDate() {
		return date;
	}
	public void setDate(Calendar date) {
		this.date = date;
	}
	public List<SummaryReportMealDTO> getPacientList() {
		return pacientList;
	}
	public void setPacientList(List<SummaryReportMealDTO> pacientList) {
		this.pacientList = pacientList;
	}
	public List<SummaryReportMealDTO> getEmployeeList() {
		return employeeList;
	}
	public void setEmployeeList(List<SummaryReportMealDTO> employeeList) {
		this.employeeList = employeeList;
	}
}
