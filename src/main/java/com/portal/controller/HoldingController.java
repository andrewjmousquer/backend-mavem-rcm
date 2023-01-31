package com.portal.controller;

import java.security.NoSuchAlgorithmException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.HoldingModel;
import com.portal.service.IHoldingService;

@RestController
@RequestMapping("/protected/holding")
@CrossOrigin(origins = "*")
public class HoldingController extends BaseController {

	@Autowired
	private IHoldingService service;
	
	/**
	 * Retorna lista com todos usuários.
	 * 
	 * @return ResponseEntity<Response<HoldingModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<HoldingModel>> listAll() throws AppException, BusException {
		return ResponseEntity.ok(this.service.list());
	}
	
	/**
	 * Retorna lista de usuários filtrados
	 * 
	 * @param id
	 * @return ResponseEntity<Response<HoldingModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@RequestMapping(value = "/search", method=RequestMethod.POST)
	public ResponseEntity<List<HoldingModel>> search(@Valid @RequestBody HoldingModel model) throws AppException, BusException {
		return ResponseEntity.ok(this.service.search(model));
	}
	
	/**
	 * Retorna user por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<HoldingModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<HoldingModel> getById(@PathVariable("id") Integer id) throws AppException, BusException {
		return ResponseEntity.ok(this.service.getById(id).get());
	}

	/**
	 * Adiciona um novo user.
	 * 
	 * @param lancamento
	 * @param result
	 * @return ResponseEntity<Response<HoldingModel>>
	 * @throws ParseException 
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PostMapping
	@RequestMapping(value = "/save", method=RequestMethod.POST)
	public ResponseEntity<HoldingModel> save(@Valid @RequestBody HoldingModel model, BindingResult result) throws AppException, BusException {
		return ResponseEntity.ok(this.service.saveOrUpdate(model, this.getUserProfile()).get());
	}
	
	/**
	 * Atualiza os dados de um usuario
	 * 
	 * @param id
	 * @param HoldingModel
	 * @param result
	 * @return ResponseEntity<Response<HoldingModel>>
	 * @throws NoSuchAlgorithmException
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PutMapping(value = "/{id}")
	public ResponseEntity<HoldingModel> update(@PathVariable("id") Integer id, @Valid @RequestBody HoldingModel HoldingModel, BindingResult result) throws NoSuchAlgorithmException, AppException, BusException {
		return ResponseEntity.ok(this.service.saveOrUpdate(HoldingModel, this.getUserProfile()).get());
	}
	
	/**
	 * Remove um usuario por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<Lancamento>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Boolean> remove(@PathVariable("id") Integer id) throws AppException, BusException {
		this.service.delete(id, this.getUserProfile());
		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}

}
