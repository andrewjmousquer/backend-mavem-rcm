package com.portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CityModel;
import com.portal.service.ICityService;

@RestController
@RequestMapping("/protected/city")
@CrossOrigin(origins = "*")
public class CityController extends BaseController {


	@Autowired
	private ICityService service;
	
	/**
	 * Retorna lista de estados de um pais
	 * 
	 * @return List<StateModel>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/getByState/{id}")
	public ResponseEntity<List<CityModel>> getByState(@PathVariable("id") Integer id) throws AppException, BusException {
		List<CityModel> list =  this.service.getByState(id);
		return ResponseEntity.ok(list);
	}

}
