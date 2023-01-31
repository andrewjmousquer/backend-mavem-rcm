package com.portal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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

import com.portal.dto.ProposalFollowUpDTO;
import com.portal.model.ProposalFollowUp;
import com.portal.service.IProposalFollowUp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected/followup")
@Tag(name = "Follow Up Controller", description = "CRUD for Follow Up Entity")
public class ProposalFollowUpController extends BaseController{

    @Autowired
    private IProposalFollowUp service;


    @Operation( summary = "Get a list of follow up" )
    @ApiResponse( responseCode = "200", description = "Successfully return list of Follow Up", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @GetMapping(path = "/listAll")
    public @ResponseBody ResponseEntity<List<ProposalFollowUpDTO>> listAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {

        if( sortBy == null || sortBy.equals( "id" ) ) {
            sortBy = "pfp_id";
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

        PageRequest pageReq = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        List<ProposalFollowUp> followUp = this.service.listAll(pageReq);
        return ResponseEntity.ok(ProposalFollowUpDTO.toDTO(followUp)) ;
    }

    @Operation( summary = "Get follow up by ID" )
    @ApiResponse( responseCode = "200", description = "Successfully found and returned the Follow Up by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalFollowUpDTO.class) ) )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @GetMapping(path = "/{id}")
    public @ResponseBody ResponseEntity<ProposalFollowUpDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Follow Up ID to be searched" ) int id  ) throws Exception {

        Optional<ProposalFollowUp> followUp = this.service.getById( id );

        if( followUp != null && followUp.isPresent() ) {
            return ResponseEntity.ok( ProposalFollowUpDTO.toDTO( followUp.get() ) );
        } else {
            return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
        }
    }

    @Operation( summary = "Save new follow up record" )
    @ApiResponse( responseCode = "201", description = "Successfully saved the Follow up", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalFollowUpDTO.class) ) )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @PostMapping( path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<ProposalFollowUpDTO> save(@RequestBody ProposalFollowUpDTO followUpDTO, BindingResult result ) throws Exception {
        Optional<ProposalFollowUp> followUpModel = this.service.save(ProposalFollowUp.toEntity(followUpDTO), this.getUserProfile());

        if(followUpModel != null && followUpModel.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(ProposalFollowUpDTO.toDTO(followUpModel.get()));
        } else {
            return ResponseEntity.badRequest().body(new ProposalFollowUpDTO());
        }
    }

    @Operation( summary = "Update follow up record" )
    @ApiResponse( responseCode = "200", description = "Successfully updated the Follow Up" )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @PutMapping( path = "/" )
    public ResponseEntity<ProposalFollowUpDTO> update( @RequestBody(required = true) ProposalFollowUpDTO followUpDTODTO) throws Exception {
        Optional<ProposalFollowUp> followUp = this.service.update( ProposalFollowUp.toEntity( followUpDTODTO ) , this.getUserProfile());

        if( followUp != null && followUp.isPresent() ) {
            return ResponseEntity.status( HttpStatus.OK ).body(ProposalFollowUpDTO.toDTO(followUp.get()));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation( summary = "Delete follow up by ID" )
    @ApiResponse( responseCode = "204", description = "Successfully deleted the Follow Up" )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @DeleteMapping( path = "/{id}" )
    public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "Follow Up ID to be deleted" ) int id ) throws Exception {
        this.service.delete( id, this.getUserProfile());
        return ResponseEntity.status( HttpStatus.OK ).build();
    }
}
