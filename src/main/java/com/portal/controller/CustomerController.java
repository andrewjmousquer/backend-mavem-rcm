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
import com.portal.model.CustomerModel;
import com.portal.service.ICustomerService;

@RestController
@RequestMapping("/protected/customer")
@CrossOrigin(origins = "*")
public class CustomerController extends BaseController {

	@Autowired
	private ICustomerService service;
	
	/**
	 * Retorna lista com todos usuários.
	 * 
	 * @return ResponseEntity<Response<CustomerModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<CustomerModel>> listAll() throws AppException, BusException {
		return ResponseEntity.ok(this.service.list());
	}
	
	/**
	 * Retorna lista de usuários filtrados
	 * 
	 * @param id
	 * @return ResponseEntity<Response<CustomerModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/search")
	public ResponseEntity<List<CustomerModel>> search(@Valid @RequestBody CustomerModel model) throws AppException, BusException {
		return ResponseEntity.ok(this.service.search(model));
	}
	
	/**
	 * Retorna user por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<CustomerModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<CustomerModel> getById(@PathVariable("id") Integer id) throws AppException, BusException {
		return ResponseEntity.ok(this.service.getById(id).get());
	}

	/**
	 * Adiciona um novo user.
	 * 
	 * @param lancamento
	 * @param result
	 * @return ResponseEntity<Response<CustomerModel>>
	 * @throws ParseException 
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PostMapping
	@RequestMapping(value = "/save", method=RequestMethod.POST)
	public ResponseEntity<CustomerModel> save(@Valid @RequestBody CustomerModel model, BindingResult result) throws AppException, BusException {
		return ResponseEntity.ok(this.service.saveOrUpdate(model, this.getUserProfile()).get());
	}
	
	/**
	 * Atualiza os dados de um usuario
	 * 
	 * @param id
	 * @param CustomerModel
	 * @param result
	 * @return ResponseEntity<Response<CustomerModel>>
	 * @throws NoSuchAlgorithmException
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PutMapping(value = "/{id}")
	public ResponseEntity<CustomerModel> update(@PathVariable("id") Integer id, @Valid @RequestBody CustomerModel customerModel, BindingResult result) throws NoSuchAlgorithmException, AppException, BusException {
		return ResponseEntity.ok(this.service.saveOrUpdate(customerModel, this.getUserProfile()).get());
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
