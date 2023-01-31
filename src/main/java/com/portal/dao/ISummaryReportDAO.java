package com.portal.dao;

import java.util.Calendar;
import java.util.List;

import com.portal.dto.SummaryReportMealDTO;
import com.portal.exceptions.AppException;

public interface ISummaryReportDAO {

	List<SummaryReportMealDTO> getPacientList(Calendar date) throws AppException;

	List<SummaryReportMealDTO> getEmployeeList(Calendar date) throws AppException;


}
