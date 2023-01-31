package com.portal.controller.form;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.portal.controller.BaseController;
import com.portal.dto.BrandDTO;
import com.portal.dto.PartnerDTO;
import com.portal.dto.ProductWithPriceListIdDTO;
import com.portal.dto.ProposalSearchDTO;
import com.portal.dto.form.ProductItemFormDTO;
import com.portal.dto.proposal.ProposalDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Partner;
import com.portal.model.Product;
import com.portal.model.Proposal;
import com.portal.model.ProposalFormProduct;
import com.portal.model.ProposalFrontForm;
import com.portal.model.Seller;
import com.portal.service.imp.ProposalService;
import com.portal.service.imp.form.ProposalFormService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected/proposal")
@Tag(name = "Proposal Controller", description = "CRUD for Proposal Entity")
public class ProposalFormController extends BaseController {
	
	@Autowired
	private ProposalFormService proposalFormService;
	
	@Autowired
	private ProposalService proposalService;
	
	@Operation( summary = "Get a list for frontend Proposal" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of Proposal", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ProposalFrontForm.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listFront")
	public @ResponseBody ResponseEntity<List<ProposalFrontForm>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "100") @Parameter( description = "Size of requested page" ) int size,
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
		
		List<ProposalFrontForm> list = this.proposalFormService.getListProposalFrontForm( this.getUserProfile());
		return ResponseEntity.ok( list ) ;
	}
	
	@Operation( summary = "Find a proposal" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of proposals", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ProposalSearchDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/find")
	public @ResponseBody ResponseEntity<List<ProposalFrontForm>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "100") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) ProposalSearchDTO dto ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "prd_id";
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
		
		List<ProposalFrontForm> list = null;
		
		list = this.proposalFormService.getListProposalFrontForm(dto,  this.getUserProfile());
		
		return ResponseEntity.ok( list ) ;
	}
	
	@Operation( summary = "Get partner by Channel ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Partner by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PartnerDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listPartnerByChannel/{id}")
	public @ResponseBody ResponseEntity<List<PartnerDTO>> getlistPartnerByChannel( @PathVariable( name = "id", required = true ) @Parameter( description = "Channel ID to be searched" ) Integer id  ) throws Exception {
		
		List<Partner> partners = this.proposalFormService.getListPartnerByChannel( id );
		
		if( partners != null ) {
			return ResponseEntity.ok( PartnerDTO.toDTO( partners ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Get partner" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Partner by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PartnerDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listPartner")
	public @ResponseBody ResponseEntity<List<PartnerDTO>> getlistPartner( ) throws Exception {
		
		List<Partner> partners = this.proposalFormService.getListPartner( );
		
		if( partners != null ) {
			return ResponseEntity.ok( PartnerDTO.toDTO( partners ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Get Executive List" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Seller by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Seller.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/getExecutiveList")
	public @ResponseBody ResponseEntity<List<Seller>> getExecutiveList() throws Exception {
		
		List<Seller> sellers = this.proposalFormService.getExecutiveList( getUserProfile() );
		
		if( sellers != null ) {
			return ResponseEntity.ok( sellers );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Get Executive list" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Seller by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Seller.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listExecutive")
	public @ResponseBody ResponseEntity<List<Seller>> getlistExecutive() throws Exception {
		
		List<Seller> sellers = this.proposalFormService.getlistExecutive( );
		
		if( sellers != null ) {
			return ResponseEntity.ok( sellers );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Get Seller by Executive ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Seller by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Seller.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listSellerByExecutive/{id}")
	public @ResponseBody ResponseEntity<List<Seller>> getlistSellerByExecutive( @PathVariable( name = "id", required = true ) @Parameter( description = "Executive ID to be searched" ) Integer id  ) throws Exception {
		
		List<Seller> sellers = this.proposalFormService.getlistSellerByExecutive( id );
		
		if( sellers != null ) {
			return ResponseEntity.ok( sellers );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Get Brand by Partner ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Brand by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BrandDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listBrandByPartner/{id}/{chnId}")
	public @ResponseBody ResponseEntity<List<BrandDTO>> getlistBrandByPartner( @PathVariable( name = "id", required = false)
																			   @Parameter( description = "Partner ID to be searched" ) String id,
																			   @PathVariable( name = "chnId", required = true )
																			   @Parameter( description = "Channel ID to be searched" ) String chnId) throws Exception {

		List<Brand> brands = this.proposalFormService.getlistBrandByPartner( id, chnId);
		
		if( brands != null ) {
			return ResponseEntity.ok( BrandDTO.toDTO( brands ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Get Product by Model" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Product by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Product.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listProductByModel/{id}/{year}")
	public @ResponseBody ResponseEntity<List<Product>> getlistProductByModel( @PathVariable( name = "id", required = true )
																			  @Parameter( description = "Model ID to be searched" ) Integer id,
																			  @PathVariable( name = "year", required = true )
																			  @Parameter( description = "Model ID to be searched" ) Integer year  ) throws Exception {
		
		List<Product> product = this.proposalFormService.getlistProductByModel( id , year );
		
		if( product != null ) {
			return ResponseEntity.ok( product );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}


	@Operation( summary = "Get Product by Model V1" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Product by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Product.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listProductByModel/v1")
	public @ResponseBody ResponseEntity<List<ProductWithPriceListIdDTO>> getlistProductByModelV1(@RequestParam( name = "id", required = true )
																				                 @Parameter( description = "Model ID to be searched" ) Integer id,
																								 @RequestParam( name = "year", required = true )
																								 @Parameter( description = "Model ID to be searched" ) Integer year,
																								 @RequestParam( name = "ptnId", required = false)
																				                 @Parameter( description = "Partner ID to be searched" ) String ptnId,
																								 @RequestParam( name = "chnId", required = false)
																								 @Parameter( description = "Channel ID to be searched" ) String chnId) throws Exception {


		Integer ptnIdParam = ptnId != null? Integer.parseInt(ptnId): null;
		Integer chnIdParam = chnId != null? Integer.parseInt(chnId): null;
		List<ProductWithPriceListIdDTO> product = this.proposalFormService.getlistProductByModelV1( id , year, ptnIdParam, chnIdParam );

		if( product != null ) {
			return ResponseEntity.ok( product );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}



	
	@Operation( summary = "Get List of Items and product" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the ProposalFormProduct by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalFormProduct.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/listProductItems")
	public @ResponseBody ResponseEntity<ProposalFormProduct> getProductItems( @RequestBody(required = true) ProductItemFormDTO productItemFormDTO ) throws Exception {
		
		ProposalFormProduct productItems = this.proposalFormService.getProductItems( productItemFormDTO );
		
		if( productItems != null ) {
			return ResponseEntity.ok( productItems );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Get Proposal" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Proposal by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/getProposal/{id}")
	public @ResponseBody ResponseEntity<ProposalDTO> getProposal( @PathVariable( name = "id", required = true ) @Parameter( description = "Proposal ID" ) Integer id  ) throws Exception {
		try {
			ProposalDTO proposal = proposalService.getProposal(id);
			
			if( proposal != null ) {
				return ResponseEntity.ok( proposal );
			} else {
				return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
			}
		} catch (BusException e) {
			throw new BusException(e.getMessage());
		} catch (AppException e) {
			throw new AppException(e.getMessage());
		}
	}
	
	@Operation( summary = "Save proposal" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the proposal", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<ProposalDTO> save( @RequestBody(required = true) ProposalDTO dto ) throws Exception {
		
		try {
			Optional<Proposal> proposal = this.proposalService.save( dto.getProposal(), this.getUserProfile());
			
			ProposalDTO dtoSaved = new ProposalDTO();
			dtoSaved.setProposal(proposal.get());
			
			if( proposal != null && proposal.isPresent() ) {
				return ResponseEntity.status( HttpStatus.OK ).body( dtoSaved );
			} 
		} catch (BusException e) {
			throw new BusException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ResponseEntity.badRequest().build();
	}
	
	@Operation( summary = "Update proposal" )
	@ApiResponse( responseCode = "201", description = "Successfully update the proposal", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<ProposalDTO> update( @RequestBody(required = true) ProposalDTO dto ) throws Exception {
		Optional<Proposal> proposal = this.proposalService.update( dto.getProposal(), this.getUserProfile());
		
		ProposalDTO dtoSaved = new ProposalDTO();
		dtoSaved.setProposal(proposal.get());
		
		if( proposal != null && proposal.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).body( dtoSaved );
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@Operation( summary = "Get Internal Seller List" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Internal Seller List", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Seller.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listInternalSeller")
	public @ResponseBody ResponseEntity<List<Seller>> listInternalSeller() throws Exception {

		List<Seller> sellers = this.proposalFormService.getlistInternalSeller();

		if( sellers != null ) {
			return ResponseEntity.ok( sellers );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}


	@Operation( summary = "Get partner by Channel ID and Seller ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Partner by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PartnerDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/listPartnerByChannel/{channelId}/seller/{sellerId}")
	public @ResponseBody ResponseEntity<List<PartnerDTO>> listPartnerByChannel( @PathVariable( name = "channelId", required = true ) @Parameter( description = "Channel ID to be searched" ) Integer channelId,
			@PathVariable( name = "sellerId", required = true ) @Parameter( description = "Seller ID to be searched" ) Integer sellerId ) throws Exception {

		List<Partner> partners = this.proposalFormService.getListPartnerByChannelAndSeller( channelId, sellerId );

		if( partners != null ) {
			return ResponseEntity.ok( PartnerDTO.toDTO( partners ) );

		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}

	@RequestMapping(path = "/getProposalReport/{proposalNumber}", method = RequestMethod.POST)
    public ResponseEntity<ByteArrayResource> getProposalReport( @PathVariable( name = "proposalNumber", required = true ) @Parameter( description = "Proposal Number" )  String proposalNumber) throws Exception {
    	try {
    		final byte[] data = this.proposalFormService.generateProposalReport(proposalNumber);
            final ByteArrayResource resource = new ByteArrayResource(data);

	        return ResponseEntity.ok()
                    .contentLength(data.length)
	                .contentType(MediaType.APPLICATION_PDF)
	                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + proposalNumber+ ".pdf" + "\"")
	                .body(resource);
    	} catch (Exception e) {
			throw new AppException("Ocorreu um erro efetuar o download do arquivo");
		}
    }
	
}
