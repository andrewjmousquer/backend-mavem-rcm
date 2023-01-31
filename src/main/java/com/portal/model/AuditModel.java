package com.portal.model;

import java.util.Date;

import com.portal.enums.AuditOperationType;

public class AuditModel {

	private Integer id;
	private Date date;
	private String ip;
	private String hostname;
	private String username;
	private String details;
	private AuditOperationType operation;

	public AuditModel() {
	}
	
	public AuditModel( Date date, String ip, String hostname, String username, String details, AuditOperationType operationType ) {
		this.date = date;
		this.ip = ip;
		this.hostname = hostname;
		this.username = username;
		this.details = details;
		this.operation = operationType;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public AuditOperationType getOperation() {
		return operation;
	}
	public void setOperation(AuditOperationType operation) {
		this.operation = operation;
	}
}
