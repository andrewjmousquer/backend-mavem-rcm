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

import com.portal.dto.ItemTypeDTO;
import com.portal.model.ItemType;
import com.portal.service.imp.ItemTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Item Type Controller", description = "CRUD for item type Entity")
public class ItemTypeController extends BaseController {

	@Autowired
	private ItemTypeService service;
	
	@Operation( summary = "Get a list of itens type" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of item type", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ItemTypeDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/itemtype")
	public @ResponseBody ResponseEntity<List<ItemTypeDTO>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "itt_id";
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
		
		List<ItemType> models = this.service.listAll( pageReq );
		return ResponseEntity.ok( ItemTypeDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Find a item type" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of itens type", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ItemTypeDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/itemtype/find")
	public @ResponseBody ResponseEntity<List<ItemTypeDTO>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) ItemTypeDTO dto ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "itt_id";
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
		
		ItemType searchModel = ItemType.toEntity( dto );
		List<ItemType> models = null;
		
		if(searchModel.getName() == "" || searchModel.getName() == null)
			return this.listAll(page, size, sortDir, sortBy);
		
		if( like ) {
			models = this.service.search(searchModel, pageReq);
		} else {
			models = this.service.find(searchModel, pageReq);
		}
		
		return ResponseEntity.ok( ItemTypeDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Get item type by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the item type by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ItemTypeDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/itemtype/{id}")
	public @ResponseBody ResponseEntity<ItemTypeDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "ItemType ID to be searched" ) int id  ) throws Exception {
		
		Optional<ItemType> model = this.service.getById( id );
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( ItemTypeDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Save new item type record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the item", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ItemTypeDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/itemtype", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<ItemTypeDTO> save( @RequestBody(required = true) ItemTypeDTO dto ) throws Exception {
		Optional<ItemType> model = this.service.save( ItemType.toEntity( dto ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body( ItemTypeDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.badRequest().body(new ItemTypeDTO());
		}
	}
	
	@Operation( summary = "Update item type record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the item type" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/itemtype" )
	public ResponseEntity<Void> update( @RequestBody(required = true) ItemTypeDTO dto) throws Exception {
		Optional<ItemType> model = this.service.update( ItemType.toEntity( dto ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Delete item type by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the item type" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/itemtype/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "ItemType ID to be deleted" ) int id ) throws Exception {
		this.service.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}
}
