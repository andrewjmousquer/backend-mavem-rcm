package com.portal.service;

import java.util.List;

import com.portal.dto.SummaryReportRequestDTO;
import com.portal.dto.SummaryReportResponseDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;

public interface ISummaryReportService {

	List<SummaryReportResponseDTO> search(SummaryReportRequestDTO request) throws AppException, BusException;

}
