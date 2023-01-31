package com.portal.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.portal.dto.QualificationDTO;
import com.portal.dto.QualificationMoveDTO;
import com.portal.dto.QualificationTreePathDTO;
import com.portal.model.Qualification;
import com.portal.service.imp.QualificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Qualification Controller", description = "CRUD for Qualification Entity, represent the veihcle qualification")
public class QualificationController extends BaseController {

	@Autowired
	private QualificationService service;
	
	@Operation( summary = "Adds a new qualification in the tree" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the qualification", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema( implementation = QualificationDTO.class ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/qualification", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<QualificationDTO> save( @RequestBody(required = true) QualificationDTO qualificationDTO ) throws Exception {
		
		Optional<Qualification> qualification = this.service.save( Qualification.toEntity( qualificationDTO ) , this.getUserProfile());
		
		if( qualification != null && qualification.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body( QualificationDTO.toDTO( qualification.get() ) );
		} else {
			return ResponseEntity.badRequest().body( new QualificationDTO() );
		}
	}
	
	@Operation( summary = "Updates the qualification record. This operation does not affect the structure, just update NAME and SEQUENCE." )
	@ApiResponse( responseCode = "200", description = "Successfully updated the Qualification" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/qualification" )
	public ResponseEntity<Void> update( @RequestBody(required = true) QualificationDTO qualificationDTO) throws Exception {
		Optional<Qualification> qualification = this.service.update( Qualification.toEntity( qualificationDTO ) , this.getUserProfile());
		
		if( qualification != null && qualification.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Delete a qualification by ID. All child structure will be deleted." )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the Qualification" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/qualification/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "Qualification ID to be deleted" ) int id ) throws Exception {
		this.service.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}

	@Operation( summary = "Moves qualification between tree structure." )
	@ApiResponse( responseCode = "201", description = "Successfully add qualification to tree", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/qualification/move", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> move( @RequestBody(required = true) QualificationMoveDTO dto ) throws Exception {
		this.service.move( dto.getNodeId(), dto.getNewParent(), this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}
	
	@Operation( summary = "Lists all qualifications in tree structure." )
	@ApiResponse( responseCode = "200", description = "Successfully return qualification tree", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema( implementation = QualificationTreePathDTO.class ) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/qualification/tree")
	public @ResponseBody ResponseEntity<Set<QualificationTreePathDTO>> tree() throws Exception {
		Set<QualificationTreePathDTO> qualifications = this.service.loadTree();
		return ResponseEntity.ok( qualifications );
	}

	@Operation( summary = "Get a list of qualifications" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of qualifications", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = QualificationDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/qualification")
	public @ResponseBody ResponseEntity<List<QualificationDTO>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "qlf_id";
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
		
		List<Qualification> models = this.service.listAll( pageReq );
		return ResponseEntity.ok( QualificationDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Find a qualification" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of qualifications", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = QualificationDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/qualification/find")
	public @ResponseBody ResponseEntity<List<QualificationDTO>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) QualificationDTO dto ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "qlf_id";
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
		
		Qualification searchModel = Qualification.toEntity( dto );
		List<Qualification> models = null;
		
		if(searchModel.getName() == "" || searchModel.getName() == null)
			return this.listAll(page, size, sortDir, sortBy);
		
		if( like ) {
			models = this.service.search(searchModel, pageReq);
		} else {
			models = this.service.find(searchModel, pageReq);
		}
		
		return ResponseEntity.ok( QualificationDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Get qualification by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the qualification by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = QualificationDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/qualification/{id}")
	public @ResponseBody ResponseEntity<QualificationDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Qualification ID to be searched" ) int id  ) throws Exception {
		
		Optional<Qualification> model = this.service.getById( id );
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( QualificationDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}

}
