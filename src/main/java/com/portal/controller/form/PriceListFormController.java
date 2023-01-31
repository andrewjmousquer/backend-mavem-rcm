package com.portal.controller.form;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import com.portal.controller.BaseController;
import com.portal.dto.PriceListDTO;
import com.portal.dto.form.PriceListDuplicateItemDTO;
import com.portal.dto.form.PriceListFormDTO;
import com.portal.dto.form.PriceListFormSearchDTO;
import com.portal.service.IPriceListFormService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected/pricelistform")
@Tag(name = "Price list form controller", description = "Custom form controller for PriceList")
public class PriceListFormController extends BaseController {

	@Autowired
	private IPriceListFormService formService;
	
	@Operation( summary = "Search a price list" )
    @ApiResponse( responseCode = "200", description = "Successfully return list of price list", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = PriceListFormSearchDTO.class) ) ) )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @PostMapping(path = "/search")
    public @ResponseBody ResponseEntity<List<PriceListFormSearchDTO>> find(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "100") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
            @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
            @RequestBody(required = true) PriceListDTO searchBody ) throws Exception {


        if( sortBy == null || sortBy.equals( "id" ) ) {
            sortBy = "prl_id";
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
        
        return ResponseEntity.ok( this.formService.search( searchBody, pageReq ) );
    }
	
	@Operation( summary = "Get pricelist by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the pricelist by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PriceListFormDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/{id}")
	public @ResponseBody ResponseEntity<PriceListFormDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "PriceList ID to be searched" ) int id  ) throws Exception {
		
		Optional<PriceListFormDTO> model = this.formService.getById( id );
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( model.get() );
			
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Save price list" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the price list", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PriceListFormDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Void> save( @RequestBody(required = true) PriceListFormDTO dto ) throws Exception {
		this.formService.save( dto, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.CREATED ).build();
	}
	
	@Operation( summary = "Update price list" )
	@ApiResponse( responseCode = "201", description = "Successfully update the price list", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PriceListFormDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Void> update( @RequestBody(required = true) PriceListFormDTO dto ) throws Exception {
		this.formService.update( dto, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.CREATED ).build();
	}
	
	@Operation( summary = "Delete price list by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the PriceList" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "PriceList ID to be deleted" ) int id ) throws Exception {
		this.formService.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}

	@Operation( summary = "Check price list duplicate itens and products" )
	@ApiResponse( responseCode = "200", description = "No duplicate found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PriceListDuplicateItemDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorn CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/checkDuplicateItens", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<List<PriceListDuplicateItemDTO>> checkDuplicateItens( @RequestBody(required = true) PriceListFormDTO dto ) throws Exception {
		return ResponseEntity.ok( this.formService.checkDuplicateItem(dto) );
	}
}
