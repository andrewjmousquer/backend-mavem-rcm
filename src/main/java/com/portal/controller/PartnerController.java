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

import com.portal.dto.PartnerDTO;
import com.portal.model.Partner;
import com.portal.service.IPartnerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected/partner")
@Tag(name = "Partner Controller", description = "CRUD for partner entity")
public class PartnerController extends BaseController {

	@Autowired
	private IPartnerService service;
	
	@Operation( summary = "Get a list of partners" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Partners", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = PartnerDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listAll")
	public @ResponseBody ResponseEntity<List<PartnerDTO>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {
		if (sortBy == null || sortBy.equals("id")) {
			sortBy = "ptn_id";
		}

		if (sortDir == null) {
			sortDir = "DESC";
		}

		if (size <= 0) {
			size = 1;
		}

		if (page < 0) {
			page = 0;
		}

		PageRequest pageReq = PageRequest.of(page, size, Sort.by( Direction.fromString( sortDir ), sortBy ) );
		List<Partner> models = this.service.listAll( pageReq );
		return ResponseEntity.ok( PartnerDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Find a partner" )
	@ApiResponse(responseCode = "200", description = "Successfully return list of Partners", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PartnerDTO.class))))
	@ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@PostMapping(path = "/find")
	public @ResponseBody
	ResponseEntity<List<PartnerDTO>> find(
			@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
			@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
			@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
			@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
			@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
			@RequestBody(required = true) String searchText) throws Exception {

		if (sortBy == null || sortBy.equals("id")) {
			sortBy = "ptn_id";
		}

		if (sortDir == null) {
			sortDir = "DESC";
		}

		if (size <= 0) {
			size = 1;
		}

		if (page < 0) {
			page = 0;
		}

		if (searchText.equals("%")) {
			searchText = "";
		}

		PageRequest pageReq = PageRequest.of(page, size, Sort.by(Direction.fromString(sortDir), sortBy));

		List<Partner> models =  this.service.searchForm(searchText, pageReq);
	
		return ResponseEntity.ok(PartnerDTO.toDTO(models));
	}
	
	@Operation( summary = "Get partner by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Partner by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PartnerDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/{id}")
	public @ResponseBody ResponseEntity<PartnerDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Partner ID to be searched" ) int id  ) throws Exception {
		
		Optional<Partner> model = this.service.getById( id );
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( PartnerDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Save new partner record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the Partner", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PartnerDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<PartnerDTO> save( @RequestBody(required = true) PartnerDTO dto ) throws Exception {
		Optional<Partner> model = this.service.save( Partner.toEntity( dto ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body(  PartnerDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.badRequest().body(new PartnerDTO());
		}
	}
	
	@Operation( summary = "Update partner record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the Partner" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/" )
	public ResponseEntity<Void> update( @RequestBody(required = true) PartnerDTO dto) throws Exception {
		Optional<Partner> model = this.service.update( Partner.toEntity( dto ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Delete partner by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the Partner" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "Partner ID to be deleted" ) int id ) throws Exception {
		this.service.delete(id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}
}
