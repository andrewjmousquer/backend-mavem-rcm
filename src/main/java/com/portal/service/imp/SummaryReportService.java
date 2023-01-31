package com.portal.service.imp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.ISummaryReportDAO;
import com.portal.dto.SummaryReportMealDTO;
import com.portal.dto.SummaryReportRequestDTO;
import com.portal.dto.SummaryReportResponseDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.service.ISummaryReportService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SummaryReportService implements ISummaryReportService {

	@Autowired
	private ISummaryReportDAO dao;
	
	@Override
	public List<SummaryReportResponseDTO> search(SummaryReportRequestDTO request) throws AppException, BusException {

		List<SummaryReportResponseDTO> list = new ArrayList<>();
		request.getStartDate().add(Calendar.HOUR, 3);

		if (request.getEndDate() == null) {
			request.setEndDate( Calendar.getInstance() );
			request.getEndDate().setTime( request.getStartDate().getTime() );
		} else {
			request.getEndDate().add(Calendar.HOUR, 3);
		}

		while (request.getStartDate().compareTo( request.getEndDate() ) <= 0) {

			try {
				SummaryReportResponseDTO responseItem = new SummaryReportResponseDTO();
				responseItem.setDate(Calendar.getInstance());
				responseItem.getDate().setTime(request.getStartDate().getTime());

				List<SummaryReportMealDTO> pacientList = this.dao.getPacientList(request.getStartDate());
				responseItem.setPacientList(pacientList);

				List<SummaryReportMealDTO> employeeList = this.dao.getEmployeeList(request.getStartDate());
				responseItem.setEmployeeList(employeeList);

				list.add(responseItem);

			} catch (Exception e) {
				e.printStackTrace();
				throw new AppException(e);
			}

			request.getStartDate().add(Calendar.DAY_OF_MONTH, 1);
		}

		return list;
	}
	
}
