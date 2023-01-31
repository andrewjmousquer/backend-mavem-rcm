package com.portal.service;

import java.util.List;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SaleModel;

public interface IHistoryService {
	
	public List<SaleModel> searchExport(SaleModel dto, UserProfileDTO userProfile) throws AppException, BusException;
	
	Long getTotalRecords(SaleModel dto) throws AppException, BusException;
	
	public byte[] generateExportExcel(SaleModel dto) throws AppException, BusException;
	
	public byte[] generateExportPdf(SaleModel dto) throws AppException, BusException;

}
