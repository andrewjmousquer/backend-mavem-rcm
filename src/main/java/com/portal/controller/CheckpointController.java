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
import com.portal.model.CheckpointModel;
import com.portal.service.ICheckpointService;

@RestController
@RequestMapping("/protected/checkpoint")
@CrossOrigin(origins = "*")
public class CheckpointController extends BaseController {

	@Autowired
	private ICheckpointService service;

	/**
	 * Retorna lista com todas listas de acesso.
	 * 
	 * @return ResponseEntity<Response<CheckpointModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<CheckpointModel>> listAll() throws AppException, BusException {
		List<CheckpointModel> menuList =  this.service.list();
		return ResponseEntity.ok(menuList);
	}

	/**
	 * Retorna lista de usuários filtrados
	 * 
	 * @param id
	 * @return ResponseEntity<Response<CheckpointModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@RequestMapping(value = "/search", method=RequestMethod.POST)
	public ResponseEntity<List<CheckpointModel>> search(@Valid @RequestBody CheckpointModel model) throws AppException, BusException {
		return ResponseEntity.ok(this.service.search(model)); 
	}
	
	/**
	 * Retorna menu por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<CheckpointModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<CheckpointModel> getById(@PathVariable("id") Integer id) throws AppException, BusException {
		return ResponseEntity.ok(this.service.getById(id).get());
	}

	/**
	 * Adiciona um novo menu.
	 * 
	 * @param lancamento
	 * @param result
	 * @return ResponseEntity<Response<CheckpointModel>>
	 * @throws ParseException 
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PostMapping
	@RequestMapping(value = "/save", method=RequestMethod.POST)
	public ResponseEntity<CheckpointModel> save(@Valid @RequestBody CheckpointModel model, BindingResult result) throws AppException, BusException {
		return ResponseEntity.ok(this.service.saveOrUpdate(model, this.getUserProfile()).get());
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

	@GetMapping(value = "/list/currentUser")
	public ResponseEntity<List<CheckpointModel>> getByCurrentUser() throws AppException, BusException {
		return ResponseEntity.ok(this.service.getByCurrentUser(getUserProfile().getUser().getAccessList().getId()));
	}
}
