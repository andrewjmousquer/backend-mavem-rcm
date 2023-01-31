package com.portal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.portal.dto.ProductModelDTO;
import com.portal.model.ProductModel;
import com.portal.service.IProductModelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Product Model Controller", description = "CRUD for product Entity")
public class ProductModelController extends BaseController  {

    @Autowired
    private IProductModelService service;

    @Operation( summary = "Find a product model" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Products Models", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ProductModelDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/productmodel/find")
	public @ResponseBody ResponseEntity<List<ProductModelDTO>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) ProductModelDTO dto ) throws Exception {
		
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
		
		ProductModel searchModel = ProductModel.toEntity( dto );
		List<ProductModel> models = null;
		models = this.service.find(searchModel, pageReq);
		
		return ResponseEntity.ok( ProductModelDTO.toDTO( models ) ) ;
	}

    @Operation( summary = "Delete productmodel by ID" )
    @ApiResponse( responseCode = "204", description = "Successfully deleted the Product" )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @DeleteMapping( path = "/productmodel/{id}" )
    public ResponseEntity<Void> delete(@PathVariable( name = "id", required = true ) @Parameter( description = "Product Model ID to be deleted" ) int id ) throws Exception {
        this.service.delete( id, this.getUserProfile());
        return ResponseEntity.status( HttpStatus.OK ).build();
    }



}
