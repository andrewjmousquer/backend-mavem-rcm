package com.portal.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.dto.BankDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.UserModel;
import com.portal.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "User Controller", description = "CRUD for bank User")
public class UserController extends BaseController {

	@Autowired
	private IUserService service;
	
	/**
	 * Retorna lista com todos usuários.
	 * 
	 * @return ResponseEntity<Response<UserModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/user/listAll")
	public ResponseEntity<List<UserModel>> listAll() throws AppException, BusException {
		List<UserModel> userList =  this.service.list();
		return ResponseEntity.ok(userList);
	}
	
	/**
	 * Retorna lista de usuários filtrados
	 * 
	 * @param id
	 * @return ResponseEntity<Response<UserModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PostMapping(value = "/user/search")
	public ResponseEntity<List<UserModel>> search(@Valid @RequestBody UserModel userModel) throws AppException, BusException {
		return ResponseEntity.ok(this.service.search(userModel));
	}
	
	/**
	 * Retorna user por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<UserModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/user/{id}")
	public ResponseEntity<UserModel> getById(@PathVariable("id") Integer id) throws AppException, BusException {
		return ResponseEntity.ok(this.service.getById(id).get());
	}

	/**
	 * Retorna user por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<UserModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PostMapping(value = "/user/getByUsername")
	public ResponseEntity<UserModel> getByUsername(@Valid @RequestBody UserModel model, BindingResult result) throws AppException, BusException {
		return ResponseEntity.ok(this.service.findByUsername(model).get());
	}
	
	/**
	 * Adiciona um novo user.
	 * 
	 * @param lancamento
	 * @param result
	 * @return ResponseEntity<Response<UserModel>>
	 * @throws ParseException 
	 * @throws BusException 
	 * @throws AppException 
	 */
	@Operation( summary = "Save new user record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the User", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BankDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserModel> save(@Valid @RequestBody UserModel model, BindingResult result) throws AppException, BusException {
		return ResponseEntity.ok(this.service.save(model, this.getUserProfile()).get());
	}
	
	@Operation( summary = "Update user record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the User" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/user" )
	public ResponseEntity<Void> update( @RequestBody(required = true) UserModel entity) throws Exception {
		Optional<UserModel> model = this.service.update( entity , this.getUserProfile());
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
		
	/**
	 * Adiciona um novo user.
	 * 
	 * @param lancamento
	 * @param result
	 * @return ResponseEntity<Response<UserModel>>
	 * @throws ParseException 
	 * @throws BusException 
	 * @throws AppException 
	 */
	@PostMapping(value = "/user/saveUserConfig")
	public ResponseEntity<UserModel> saveUserConfig(@Valid @RequestBody UserModel model, BindingResult result) throws AppException, BusException {
		if (!StringUtils.isEmpty(model.getPassword())) this.service.changePassword(model, this.getUserProfile()).get();
		return ResponseEntity.ok(this.service.saveUserConfig(model).get());
	}	
	/**
	 * Remove um usuario por ID.
	 * 
	 * @param id
	 * @return ResponseEntity<Response<Lancamento>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@DeleteMapping(value = "/user/{id}")
	public ResponseEntity<Boolean> remove(@PathVariable("id") Integer id) throws AppException, BusException {
		this.service.delete(id, this.getUserProfile());
		return new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.OK);
	}

	@PostMapping(value = "/user/changePassword")
	public ResponseEntity<UserModel> changePassword(@Valid @RequestBody UserModel model) throws AppException, BusException {
		return ResponseEntity.ok(this.service.changePassword(model, this.getUserProfile()).get());
	}

	/**
	 * Retorna usuário atual.
	 *
	 * @return ResponseEntity<Response<UserModel>>
	 * @throws BusException
	 * @throws AppException
	 */
	@GetMapping(value = "/user/current")
	public ResponseEntity<UserModel> getCurrent() throws AppException, BusException {
		return ResponseEntity.ok(this.getUserProfile().getUser());
	}

}
