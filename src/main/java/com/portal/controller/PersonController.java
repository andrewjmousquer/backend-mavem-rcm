package com.portal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.portal.dto.PersonDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Person;
import com.portal.service.IPersonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/protected")
@CrossOrigin(origins = "*")
public class PersonController extends BaseController {

	@Autowired
	private IPersonService service;
	
	/**
	 * Retorna lista com todos usuários.
	 * 
	 * @return ResponseEntity<Response<MenuModel>>
	 * @throws BusException 
	 * @throws AppException 
	 */
	@GetMapping(value = "/person/listAll")
	public ResponseEntity<List<Person>> listAll() throws AppException, BusException {
		List<Person> personList =  this.service.list();
		return ResponseEntity.ok(personList);
	}

	@Operation( summary = "Get a list of brand" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Brands", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/person")
	public @ResponseBody ResponseEntity<List<PersonDTO>> listAll(
			@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
			@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
			@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
			@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {

		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "name";
		}

		if( sortDir == null ) {
			sortDir = "DESC";
		}

		if( size <= 0 ) {
			size = 1;
		}

		if( page < 0 ) {
			page = 0;
		}

		PageRequest pageReq = PageRequest.of(page, size, Sort.by( Sort.Direction.fromString( sortDir ), sortBy ) );

		List<Person> persons = this.service.listAll( pageReq );
		return ResponseEntity.ok( PersonDTO.toDTO( persons ) ) ;
	}

	@Operation( summary = "Get person by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the person by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Person.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/person/{id}")
	public @ResponseBody ResponseEntity<Optional<Person>> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Person ID to be searched" ) int id  ) throws Exception {
		
		Optional<Person> model = this.service.getById( id );
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( model );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}

	@Operation( summary = "Find a person" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of persons", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = PersonDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/person/find")
	public @ResponseBody ResponseEntity<List<PersonDTO>> find(
			@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
			@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
			@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
			@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
			@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
			@RequestBody(required = true) PersonDTO dto ) throws Exception {


		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "per_id";
		}

		if( sortDir == null ) {
			sortDir = "DESC";
		}

		if( size <= 0 ) {
			size = 1;
		}

		if( page < 0 ) {
			page = 0;
		}

		PageRequest pageReq = PageRequest.of(page, size, Sort.by( Sort.Direction.fromString( sortDir ), sortBy ) );

		Person searchModel = Person.toEntity( dto );
		List<Person> person = this.service.searchForm(searchModel.getName(), pageReq);
		
		return ResponseEntity.ok( PersonDTO.toDTO( person ) ) ;
	}


	@Operation( summary = "Save new person record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the person", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Person.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/person/", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Person> save( @RequestBody(required = true) Person person ) throws Exception {
		Optional<Person> model = this.service.saveOrUpdate( person , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body( model.get() );
		} else {
			return ResponseEntity.badRequest().body(new Person());
		}
	}
	
	@Operation( summary = "Update person record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the person" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/person" )
	public @ResponseBody ResponseEntity<Person> update( @RequestBody(required = true) Person person) throws Exception {
		Optional<Person> model = this.service.saveOrUpdate( person , this.getUserProfile());
		
		if ( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).body( model.get() );
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Delete person by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the person" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/person/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "person ID to be deleted" ) int id ) throws Exception {
		this.service.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}
	
	@Operation( summary = "Find list of person by document" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of persons", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = PersonDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/person/findByDocument")
	public @ResponseBody ResponseEntity<Optional<Person>> findByDocument(@RequestBody(required = true) String searchText) throws Exception {
		Optional<Person> model = this.service.searchByDocument(searchText);
		return ResponseEntity.ok(model);
	}
	
	@Operation( summary = "Find list of person by contact" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of persons", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = PersonDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/person/findByContact")
	public @ResponseBody ResponseEntity<List<PersonDTO>>  findByContact(@RequestBody(required = true) String searchText) throws Exception {
		List<Person> list = this.service.searchByContact(searchText);
		return ResponseEntity.ok( PersonDTO.toDTO( list ));
	}
	
}
