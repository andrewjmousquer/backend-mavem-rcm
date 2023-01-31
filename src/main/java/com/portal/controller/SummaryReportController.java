package com.portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.dto.SummaryReportRequestDTO;
import com.portal.dto.SummaryReportResponseDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.service.ISummaryReportService;

@RestController
@RequestMapping("/protected/summary/report")
@CrossOrigin(origins = "*")
public class SummaryReportController extends BaseController {

	@Autowired
	private ISummaryReportService service;

	@PostMapping(value = "/search")
	public ResponseEntity<List<SummaryReportResponseDTO>> search(@RequestBody SummaryReportRequestDTO request) throws AppException, BusException {
		return ResponseEntity.ok(this.service.search(request));
	}

}
