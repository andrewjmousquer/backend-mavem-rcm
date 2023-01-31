package com.portal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.portal.dto.BankDTO;
import com.portal.model.Bank;
import com.portal.service.imp.BankService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Bank Controller", description = "CRUD for bank Entity")
public class BankController extends BaseController {

	@Autowired
	private BankService service;
	
	@Operation( summary = "Get a list of banks" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Bank", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = BankDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/bank")
	public @ResponseBody ResponseEntity<List<BankDTO>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "bnk_id";
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
		
		PageRequest pageReq = PageRequest.of(page, size, Sort.by( Direction.fromString( sortDir ), sortBy ) );
		
		List<Bank> models = this.service.listAll( pageReq );
		return ResponseEntity.ok( BankDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Find a bank" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Bank", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = BankDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/bank/find")
	public @ResponseBody ResponseEntity<List<BankDTO>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) BankDTO dto ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "bnk_id";
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
		
		PageRequest pageReq = PageRequest.of(page, size, Sort.by( Direction.fromString( sortDir ), sortBy ) );
		
		Bank searchModel = Bank.toEntity( dto );
		List<Bank> models = null;
		
		if(searchModel.getName() == "" || searchModel.getName() == null)
			return this.listAll(page, size, sortDir, sortBy);
		
		if( like ) {
			models = this.service.search(searchModel, pageReq);
		} else {
			models = this.service.find(searchModel, pageReq);
		}
		
		return ResponseEntity.ok( BankDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Get bank by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Bank by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BankDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/bank/{id}")
	public @ResponseBody ResponseEntity<BankDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Bank ID to be searched" ) int id  ) throws Exception {
		
		Optional<Bank> model = this.service.getById( id );
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( BankDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Save new bank record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the Bank", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BankDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/bank", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Bank> save( @RequestBody(required = true) BankDTO dto ) throws Exception {
		Optional<Bank> model = this.service.save( Bank.toEntity( dto ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body( model.get() );
		} else {
			return ResponseEntity.badRequest().body(new Bank());
		}
	}
	
	@Operation( summary = "Update bank record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the Bank" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/bank" )
	public ResponseEntity<Void> update( @RequestBody(required = true) BankDTO dto) throws Exception {
		Optional<Bank> model = this.service.update( Bank.toEntity( dto ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Delete bank by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the Bank" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/bank/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "Bank ID to be deleted" ) int id ) throws Exception {
		this.service.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}
}
