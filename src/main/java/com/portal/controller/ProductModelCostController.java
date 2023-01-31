package com.portal.controller;

import java.util.ArrayList;

/**
 * @author Ederson Sergio Monteiro Coelho
 *
 */

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

import com.portal.dto.ProductDTO;
import com.portal.dto.ProductModelCostDTO;
import com.portal.model.ProductModelCost;
import com.portal.service.IProductModelCostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Product Model Cost Controller", description = "CRUD for product model cost Entity")
public class ProductModelCostController extends BaseController {

	@Autowired
	private IProductModelCostService service;

	@Operation( summary = "Get product model cost by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Product Model Cost by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductModelCostDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/productmodelcost/{id}")
	public @ResponseBody ResponseEntity<ProductModelCostDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Product ID to be searched" ) int id  ) throws Exception {

		Optional<ProductModelCost> model = this.service.getById( id );

		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( ProductModelCostDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}

	@Operation( summary = "Get a list of products models costs" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Products Models Costs", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ProductDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/productmodelcost")
	public @ResponseBody ResponseEntity<List<ProductModelCostDTO>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {

		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "pmc_id";
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

		List<ProductModelCost> models = this.service.listAll( pageReq );
		return ResponseEntity.ok( ProductModelCostDTO.toDTO( models ) ) ;
	}

	@Operation( summary = "Find a product model cost" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Products Models Costs", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ProductModelCostDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/productmodelcost/find")
	public @ResponseBody ResponseEntity<List<ProductModelCostDTO>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "100") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestBody(required = true) ProductModelCostDTO dto ) throws Exception {

		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "pmc.pmc_id";
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

		ProductModelCost searchModel = ProductModelCost.toEntity( dto );
		List<ProductModelCost> models = null;

		models = this.service.find(searchModel, pageReq);

		return ResponseEntity.ok( ProductModelCostDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Find a product model cost" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Products Models Costs", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ProductModelCostDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/productmodelcost/find/duplicatemultiple/validate")
	public @ResponseBody ResponseEntity<List<ProductModelCostDTO>> findDuplicateMultipleValidate(@RequestBody(required = true) List<ProductModelCostDTO> dtos ) throws Exception {

		List<ProductModelCost> searchModel = ProductModelCost.toEntity( dtos );
		List<ProductModelCost> models = null;

		models = this.service.findDuplicateMultipleValidate(searchModel);

		return ResponseEntity.ok( ProductModelCostDTO.toDTO( models ) ) ;
	}

	@Operation( summary = "Find a product model cost" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Products Models Costs", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ProductModelCostDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/productmodelcost/search")
	public @ResponseBody ResponseEntity<List<ProductModelCostDTO>> search(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "100") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) ProductModelCostDTO dto ) throws Exception {

		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "prm_id";
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

		ProductModelCost searchModel = ProductModelCost.toEntity( dto );
		List<ProductModelCost> models = null;

		models = this.service.search(searchModel, pageReq);

		return ResponseEntity.ok( ProductModelCostDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Save new product model cost record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the Product Model Cost", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductModelCostDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/productmodelcost", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<ProductModelCostDTO> save( @RequestBody(required = true) ProductModelCostDTO dto ) throws Exception {
		Optional<ProductModelCost> model = this.service.save( ProductModelCost.toEntity( dto ) , this.getUserProfile());

		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body( ProductModelCostDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.badRequest().body(new ProductModelCostDTO());
		}
	}
	
	@Operation( summary = "Save new list product model cost record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the Product Model Cost", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductModelCostDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/productmodelcost/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<List<ProductModelCostDTO>> saveBulk( @RequestBody(required = true) List<ProductModelCostDTO> dtos ) throws Exception {
		List<ProductModelCost> models = this.service.saveBulk( ProductModelCost.toEntity( dtos ) , this.getUserProfile());

		if( models != null && ! models.isEmpty() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body( ProductModelCostDTO.toDTO( models ) );
		} else {
			return ResponseEntity.badRequest().body(new ArrayList<ProductModelCostDTO>());
		}
	}

	@Operation( summary = "Update product model cost record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the Product Model Cost" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/productmodelcost" )
	public ResponseEntity<Void> update( @RequestBody(required = true) ProductModelCostDTO dto) throws Exception {
		Optional<ProductModelCost> model = this.service.update( ProductModelCost.toEntity( dto ) , this.getUserProfile());

		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Update list product model cost record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the Product Model Cost" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/productmodelcost/bulk" )
	public ResponseEntity<Void> updateBulk( @RequestBody(required = true) List<ProductModelCostDTO> dtos ) throws Exception {
		List<ProductModelCost> models = this.service.updateBulk( ProductModelCost.toEntity( dtos ) , this.getUserProfile());

		if( models != null && ! models.isEmpty() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@Operation( summary = "Delete product model cost by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the Product Model Cost" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/productmodelcost/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "Product Model Cost ID to be deleted" ) int id ) throws Exception {
		this.service.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}
}