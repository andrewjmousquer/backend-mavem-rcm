package com.portal.controller;

import java.text.ParseException;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SaleModel;
import com.portal.service.ISaleService;

@RestController
@RequestMapping("/protected/sale")
@CrossOrigin(origins = "*")
public class SaleController extends BaseController {

	@Autowired
	private ISaleService service;

	/**
	 * Retorna lista com todas as sales
	 * 
	 * @return ResponseEntity<Response<SaleModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<SaleModel>> listAll() throws AppException, BusException {
		return ResponseEntity.ok(this.service.list());
	}
	
	/**
	 * Retorna lista de sales filtrados
	 * 
	 * @param id
	 * @return ResponseEntity<Response<SaleModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@RequestMapping(value = "/search", method=RequestMethod.POST)
	public ResponseEntity<List<SaleModel>> search(@Valid @RequestBody SaleModel model) throws AppException, BusException {
		return ResponseEntity.ok(this.service.search(model));
	}
	
	/**
	 * Retorna sale por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<SaleModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<SaleModel> getById(@PathVariable("id") Integer id) throws AppException, BusException {
		return ResponseEntity.ok(this.service.getById(id).get());
	}

	/**
	 * Adiciona uma sale
	 * 
	 * @param lancamento
	 * @param result
	 * 
	 * @return ResponseEntity<Response<SaleModel>>
	 * 
	 * @throws ParseException 
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PostMapping
	@RequestMapping(value = "/save", method=RequestMethod.POST)
	public ResponseEntity<SaleModel> save(@Valid @RequestBody SaleModel model, BindingResult result) throws AppException, BusException {
		return ResponseEntity.ok(this.service.saveOrUpdate(model, this.getUserProfile()).get());
	}
		
	/**
	 * Remove um sale por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<Boolean>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Boolean> remove(@PathVariable("id") Integer id) throws AppException, BusException {
		this.service.delete(id, this.getUserProfile());
		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}

}