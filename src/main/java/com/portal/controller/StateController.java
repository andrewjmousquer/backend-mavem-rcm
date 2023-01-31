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
import com.portal.model.StateModel;
import com.portal.service.IStateService;

@RestController
@RequestMapping("/protected/state")
@CrossOrigin(origins = "*")
public class StateController extends BaseController {

	@Autowired
	private IStateService service;
	
	/**
	 * Retorna lista com todos estados
	 * 
	 * @return List<StateModel>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<StateModel>> listAll() throws AppException, BusException {
		List<StateModel> list =  this.service.list();
		return ResponseEntity.ok(list);
	}
	
	/**
	 * Retorna lista de estados de um pais
	 * 
	 * @return List<StateModel>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/getByCountry/{id}")
	public ResponseEntity<List< StateModel>> getByCountry(@PathVariable("id") Integer id) throws AppException, BusException {
		List<StateModel> list =  this.service.getByCountryId(id);
		return ResponseEntity.ok(list);
	}

}
