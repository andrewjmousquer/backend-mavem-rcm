package com.portal.dto;

import java.sql.Timestamp;

public class HistoryAccessVisitorDTO {
	private long id;
	private String visitorName;
	private String beneficiaryName;
	private String accessType;
	private String visitorType;
	private String bed;
	private Timestamp accessDate;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getVisitorName() {
		return visitorName;
	}
	public void setVisitorName(String visitorName) {
		this.visitorName = visitorName;
	}
	public String getBeneficiaryName() {
		return beneficiaryName;
	}
	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}
	public String getAccessType() {
		return accessType;
	}
	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}
	public String getVisitorType() {
		return visitorType;
	}
	public void setVisitorType(String visitorType) {
		this.visitorType = visitorType;
	}
	public String getBed() {
		return bed;
	}
	public void setBed(String bed) {
		this.bed = bed;
	}
	public Timestamp getAccessDate() {
		return accessDate;
	}
	public void setAccessDate(Timestamp accessDate) {
		this.accessDate = accessDate;
	}
	
}
