package com.portal.controller;

import com.portal.dto.BrandDTO;
import com.portal.model.Brand;
import com.portal.service.imp.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/protected")
@Tag(name = "Brand Controller", description = "CRUD for Brand Entity, represent the veihcle brand")
public class BrandController extends BaseController {

	@Autowired
	private BrandService service;
	
	@Operation( summary = "Get a list of brand" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Brands", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/brand")
	public @ResponseBody ResponseEntity<List<BrandDTO>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {  
			sortBy = "brd_id";
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
		
		List<Brand> brands = this.service.listAll( pageReq );
		return ResponseEntity.ok( BrandDTO.toDTO( brands ) ) ;
	}
	
	@Operation( summary = "Find a brand" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Brands", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/brand/find")
	public @ResponseBody ResponseEntity<List<BrandDTO>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) BrandDTO dto ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "brd_id";
		}
		
		if( sortDir == null ) {
			sortDir = "DESC";
		}
		
		if( size <= 0 ) {
			size = 1;
		}

		if (page < 0) {
			page = 0;
		}

		PageRequest pageReq = PageRequest.of(page, size, Sort.by(Direction.fromString(sortDir), sortBy));

		Brand searchModel = Brand.toEntity(dto);
		List<Brand> brands = null;

		brands = this.service.find(searchModel, pageReq);

		return ResponseEntity.ok(BrandDTO.toDTO(brands));
	}
	
	@Operation( summary = "Get brand by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Brand by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BrandDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/brand/{id}")
	public @ResponseBody ResponseEntity<BrandDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Brand ID to be searched" ) int id  ) throws Exception {
		
		Optional<Brand> brand = this.service.getById( id );
		
		if( brand != null && brand.isPresent() ) {
			return ResponseEntity.ok( BrandDTO.toDTO( brand.get() ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Save new brand record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the Brand", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BrandDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/brand", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Brand> save( @RequestBody(required = true) BrandDTO brandDTO ) throws Exception {
		Optional<Brand> brand = this.service.save( Brand.toEntity( brandDTO ) , this.getUserProfile());
		
		if( brand != null && brand.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body( brand.get() );
		} else {
			return ResponseEntity.badRequest().body(new Brand());
		}
	}
	
	@Operation( summary = "Update brand record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the Brand" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/brand" )
	public ResponseEntity<Void> update( @RequestBody(required = true) BrandDTO brandDTO) throws Exception {
		Optional<Brand> brand = this.service.update( Brand.toEntity( brandDTO ) , this.getUserProfile());
		
		if( brand != null && brand.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Delete brand by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the Brand" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/brand/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "Brand ID to be deleted" ) int id ) throws Exception {
		this.service.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}
}
