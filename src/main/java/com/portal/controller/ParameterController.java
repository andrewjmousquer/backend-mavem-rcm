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
import com.portal.model.ParameterModel;
import com.portal.service.IParameterService;

@RestController
@RequestMapping("/protected/parameter")
@CrossOrigin(origins = "*")
public class ParameterController extends BaseController {

	@Autowired
	private IParameterService service;
	
	/**
	 * Retorna lista com todos usuários.
	 * 
	 * @return ResponseEntity<Response<ParameterModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<ParameterModel>> listAll() throws AppException, BusException {
		List<ParameterModel> parameterList =  this.service.list();
		return ResponseEntity.ok(parameterList);
	}
	
	/**
	 * Retorna lista de usuários filtrados
	 * 
	 * @param id
	 * @return ResponseEntity<Response<ParameterModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@RequestMapping(value = "/search", method=RequestMethod.POST)
	public ResponseEntity<List<ParameterModel>> search(@Valid @RequestBody ParameterModel model) throws AppException, BusException {
		return ResponseEntity.ok(this.service.search(model));
	}
	
	/**
	 * Retorna parameter por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<ParameterModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<ParameterModel> getById(@PathVariable("id") Integer id) throws AppException, BusException {
		return ResponseEntity.ok(this.service.getById(id).get());
	}

	/**
	 * Adiciona um novo parameter.
	 * 
	 * @param lancamento
	 * @param result
	 * @return ResponseEntity<Response<ParameterModel>>
	 * @throws ParseException 
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PostMapping
	@RequestMapping(value = "/save", method=RequestMethod.POST)
	public ResponseEntity<ParameterModel> save(@Valid @RequestBody ParameterModel model, BindingResult result) throws AppException, BusException {
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

	/**
	 * Retorna valor do parameter por NOME.
	 *
	 * @param name
	 * @return ResponseEntity<?>
	 */
	@GetMapping(value = "/name/{name}")
	public ResponseEntity<?> getValueByName(@PathVariable String name) {
		return ResponseEntity.ok(this.service.getValueOf(name));
	}
}
