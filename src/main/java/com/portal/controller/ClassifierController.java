package com.portal.controller;

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
import com.portal.model.Classifier;
import com.portal.service.IClassifierService;

@RestController
@RequestMapping("/protected/classifier")
@CrossOrigin(origins = "*")
public class ClassifierController extends BaseController {

	@Autowired
	private IClassifierService service;
	
	/**
	 * Retorna lista com todos tipos de classifiers.
	 * 
	 * @return List<UserTypeModel>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/listAll")
	public ResponseEntity<List<Classifier>> listAll() throws AppException, BusException {
		List<Classifier> userList =  this.service.list();
		return ResponseEntity.ok(userList);
	}
	
	/**
	 * Busca classifiers
	 * 
	 * @return List<UserTypeModel>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@RequestMapping(value = "/search", method=RequestMethod.POST)
	public ResponseEntity<List<Classifier>> search(@Valid @RequestBody Classifier classifierModel) throws AppException, BusException {
		List<Classifier> list =  this.service.search(classifierModel);
		return ResponseEntity.ok(list);
	}
	
	/**
	 * Retorna classifier por tipo.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<ClassifierModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@RequestMapping(value="/searchByType/{type}", method=RequestMethod.GET)
	public ResponseEntity<List<Classifier>> getByType(@PathVariable("type") String type) throws AppException, BusException {
		return ResponseEntity.ok(this.service.search(new Classifier(type)));
	}
	
	@RequestMapping(value="/searchByNameOrType", method=RequestMethod.POST)
	public ResponseEntity<List<Classifier>> getByNameOrType(@Valid @RequestBody Classifier classifierModel) throws AppException, BusException {
		return ResponseEntity.ok(this.service.searchByNameOrType(classifierModel));
	}
	
	/**
	 * Retorna classifier por tipo.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<ClassifierModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@RequestMapping(value="/searchByValue/{value}", method=RequestMethod.GET)
	public ResponseEntity<Classifier> getByValue(@PathVariable("value") String value) throws AppException, BusException {
		return ResponseEntity.ok(this.service.find(new Classifier(value, null)).get());
	}

	@PostMapping
	@RequestMapping(value = "/save", method=RequestMethod.POST)
	public ResponseEntity<Classifier> save(@Valid @RequestBody Classifier model, BindingResult result) throws AppException, BusException {
		return ResponseEntity.ok(this.service.saveOrUpdate(model, this.getUserProfile()).get());
	}

	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Boolean> remove(@PathVariable("id") Integer id) throws AppException, BusException {
		this.service.delete(id, this.getUserProfile());
		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
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
	public ResponseEntity<Classifier> getById(@PathVariable("id") Integer id) throws AppException, BusException {
		return ResponseEntity.ok(this.service.getById(id).get());
	}

}
