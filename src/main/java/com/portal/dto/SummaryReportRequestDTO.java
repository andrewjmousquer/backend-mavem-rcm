package com.portal.dto;

import java.util.Calendar;

public class SummaryReportRequestDTO {

	private Calendar startDate;
	private Calendar endDate;
	
	public Calendar getStartDate() {
		return startDate;
	}
	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}
	public Calendar getEndDate() {
		return endDate;
	}
	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}
}
