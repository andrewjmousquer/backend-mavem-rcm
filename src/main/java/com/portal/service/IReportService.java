package com.portal.service;

import java.util.List;

import com.portal.model.ReportModel;

public interface IReportService {
	
	public Boolean check();
	
	public List<ReportModel> listAll(String token);
	
	public List<ReportModel> listAllFolders(String token);

	public byte[] getReport(String jasperUrl, String reportPath, String jasperUser, String jasperPassword, String params);

}
