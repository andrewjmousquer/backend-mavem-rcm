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

import com.portal.dto.ModelDTO;
import com.portal.model.Model;
import com.portal.service.imp.ModelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Model Controller", description = "CRUD for Model Entity, represent the vehicle model")
public class ModelController extends BaseController {

	@Autowired
	private ModelService service;
	
	@Operation( summary = "Get a list of model" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Models", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ModelDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/model")
	public @ResponseBody ResponseEntity<List<ModelDTO>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "mdl_id";
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
		
		List<Model> models = this.service.listAll( pageReq );
		return ResponseEntity.ok( ModelDTO.toDTO( models ) ) ;
	}


	@Operation( summary = "Get a list of model" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Models", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ModelDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/model/brand/{id}")
	public @ResponseBody ResponseEntity<List<ModelDTO>> getAllByBrand(
			@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
			@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
			@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
			@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
			@PathVariable( name = "id", required = true ) @Parameter( description = "Brand ID to be searched" ) int id ) throws Exception {

		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "mdl_id";
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

		List<Model> models = this.service.listAllByBrand( id, pageReq );
		return ResponseEntity.ok( ModelDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Find a model" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Models", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ModelDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/model/find")
	public @ResponseBody ResponseEntity<List<ModelDTO>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) ModelDTO modelDTO ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "mdl_id";
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
		
//		if(modelDTO.getName() == "" || modelDTO.getName() == null)
//			return this.listAll(page, size, sortDir, sortBy);
		
//		Brand brand = new Brand();
//		brand.setName(modelDTO.getName());
		
		Model searchModel = Model.toEntity( modelDTO );
//		searchModel.setBrand(brand);
		
		List<Model> models = null;
		
		if( like ) {
			models = this.service.search(searchModel, pageReq);
		} else {
			models = this.service.find(searchModel, pageReq);
		}
		
		return ResponseEntity.ok( ModelDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Get model by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Model by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ModelDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/model/{id}")
	public @ResponseBody ResponseEntity<ModelDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Model ID to be searched" ) int id  ) throws Exception {
		
		Optional<Model> model = this.service.getById( id );
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( ModelDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Save new model record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the Model", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE , schema = @Schema(implementation = ModelDTO.class)) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/model", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<ModelDTO> save( @RequestBody(required = true) ModelDTO modelDTO ) throws Exception {
		Optional<Model> model = this.service.save( Model.toEntity( modelDTO ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body( ModelDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.badRequest().body(new ModelDTO());
		}
	}
	
	@Operation( summary = "Update model record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the Model" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/model" )
	public ResponseEntity<Void> update( @RequestBody(required = true) ModelDTO modelDTO) throws Exception {
		Optional<Model> model = this.service.update( Model.toEntity( modelDTO ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Delete model by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the Model" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/model/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "Model ID to be deleted" ) int id ) throws Exception {
		this.service.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}
}
