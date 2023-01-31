package com.portal.controller;

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

import com.portal.dto.LeadDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Lead;
import com.portal.service.ILeadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Lead Controller", description = "CRUD for lead entity")
public class LeadController extends BaseController {

    @Autowired
    private ILeadService service;

    @Operation(summary = "Get a list ALL leads")
    @ApiResponse(responseCode = "200", description = "Successfully return list of leads", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LeadDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/lead")
    public @ResponseBody
    ResponseEntity<List<LeadDTO>> listAll(@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
                                          @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
                                          @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
                                          @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy) throws AppException, BusException {
        if( sortBy == null || sortBy.equals( "id" ) ) {
            sortBy = "led_id";
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
        PageRequest pageReq = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        List<Lead> leads = this.service.listAll(pageReq);
        return ResponseEntity.ok(LeadDTO.toDTO(leads));
    }

    @Operation(summary = "Find a lead")
    @ApiResponse(responseCode = "200", description = "Successfully return list a Lead", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LeadDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retrun CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/lead/find")
    public @ResponseBody ResponseEntity<List<LeadDTO>> find(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
            @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
            @RequestBody(required = true) LeadDTO leadDTO) throws Exception {

        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "led_id";
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

        PageRequest pageReq = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Lead searchModel = Lead.toEntity(leadDTO);
        List<Lead> leads = null;
        if (like) {
        	if(leadDTO.getSearchText() != null && !leadDTO.getSearchText().isEmpty()) {
        		searchModel.setName(leadDTO.getSearchText());
        		searchModel.setEmail(leadDTO.getSearchText());
        		searchModel.setPhone(leadDTO.getSearchText());
        	}
            leads = this.service.search(searchModel, pageReq);
        } else {
            leads = this.service.find(searchModel, pageReq);
        }
        return ResponseEntity.ok(LeadDTO.toDTO(leads));
    }

    @Operation(summary = "Get lead by ID")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned the Lead by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Lead.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong businnes logic will return CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/lead/{id}")
    public @ResponseBody ResponseEntity<LeadDTO> getById(@PathVariable(name = "id", required = true) @Parameter(description = "Lead ID to be searched") int id) throws Exception {
        Optional<Lead> lead = this.service.getById(id);

        if (lead != null && lead.isPresent()) {
            return ResponseEntity.ok(LeadDTO.toDTO(lead.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Save new lead")
    @ApiResponse(responseCode = "201", description = "Successfully 'save' the Lead", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LeadDTO.class)))
    @ApiResponse(responseCode = "403", description = " Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/lead", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<Lead> save(@RequestBody(required = true) LeadDTO leadDTO) throws Exception {
        Optional<Lead> lead = this.service.save(leadDTO, this.getUserProfile());
        if (lead != null && lead.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(lead.get());
        } else {
            return ResponseEntity.badRequest().body(new Lead());
        }
    }

    @Operation(summary = "Update lead")
    @ApiResponse(responseCode = "201", description = "Successfully updated the Lead")
    @ApiResponse(responseCode = "403", description = " Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping(path = "/lead")
    public @ResponseBody ResponseEntity<Void> update(@RequestBody(required = true) LeadDTO leadDTO) throws Exception {
        Optional<Lead> lead = this.service.update(leadDTO, this.getUserProfile());
        if (lead != null && lead.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete lead by ID")
    @ApiResponse(responseCode = "204", description = "Successfully delete")
    @ApiResponse(responseCode = "403", description = " Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @DeleteMapping(path = "/lead/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id", required = true) @Parameter(description = "Lead ID to be delete") int id) throws AppException, BusException {
        this.service.delete(id, this.getUserProfile());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
