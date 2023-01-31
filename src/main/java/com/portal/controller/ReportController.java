package com.portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ReportModel;
import com.portal.service.IReportService;

@RestController
@RequestMapping("/protected/report")
@CrossOrigin(origins = "*")
public class ReportController extends BaseController {

	@Autowired
	private IReportService service;
	
	@GetMapping(value = "/check")
	public ResponseEntity<Boolean> get() throws AppException, BusException {
		return new ResponseEntity<Boolean>(this.service.check(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<ReportModel>> listAll(@RequestHeader("Authorization") String token) throws AppException, BusException {
		return new ResponseEntity<List<ReportModel>>(this.service.listAll(token), HttpStatus.OK);
	}
	
	@GetMapping(value = "/listAllFolders")
	public ResponseEntity<List<ReportModel>> listAllFolders(@RequestHeader("Authorization") String cookie) throws AppException, BusException {
		return new ResponseEntity<List<ReportModel>>(this.service.listAllFolders(cookie), HttpStatus.OK);
	}
	
}
