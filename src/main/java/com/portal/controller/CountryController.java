package com.portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.CountryModel;
import com.portal.service.ICountryService;

@RestController
@RequestMapping("/protected/country")
@CrossOrigin(origins = "*")
public class CountryController extends BaseController {

	@Autowired
	private ICountryService service;
	
	/**
	 * Retorna lista com todos tipos de usuario.
	 * 
	 * @return List<UserTypeModel>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<CountryModel>> listAll() throws AppException, BusException {
		List<CountryModel> userList =  this.service.list();
		return ResponseEntity.ok(userList);
	}

}
