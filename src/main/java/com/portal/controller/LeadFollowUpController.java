package com.portal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.portal.dto.LeadFollowUpDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.LeadFollowUp;
import com.portal.service.ILeadFollowUpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "LeadFollowUp Controller", description = "CRUD for lead entity")
public class LeadFollowUpController extends BaseController {

	@Autowired
	private ILeadFollowUpService service;

	@Operation(summary = "List followUps by leadId")
	@ApiResponse(responseCode = "200", description = "Successfully return list a Lead Followups", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LeadFollowUpDTO.class))))
	@ApiResponse(responseCode = "403", description = " Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Wrong business logic will return CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@GetMapping(path = "/leadfup", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<List<LeadFollowUpDTO>> listByLeadId(@RequestParam(required = true) Integer leadId) throws Exception {
		List<LeadFollowUp> leadFollowUp = this.service.findByLeadId(leadId);
		return ResponseEntity.ok( LeadFollowUpDTO.toDTO(leadFollowUp) );
	}

	@Operation(summary = "Save new lead followUp")
	@ApiResponse(responseCode = "201", description = "Successfully 'save' the Lead FollowUp", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LeadFollowUpDTO.class)))
	@ApiResponse(responseCode = "403", description = " Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Wrong business logic will return CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@PostMapping(path = "/leadfup", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<LeadFollowUp> save(
			@RequestBody(required = true) LeadFollowUpDTO leadFollowUpDTO) throws Exception {
		Optional<LeadFollowUp> leadFollowUp = this.service.save(leadFollowUpDTO, this.getUserProfile());
		if (leadFollowUp != null && leadFollowUp.isPresent()) {
			return ResponseEntity.status(HttpStatus.CREATED).body(leadFollowUp.get());
		} else {
			return ResponseEntity.badRequest().body(new LeadFollowUp());
		}
	}

	@Operation(summary = "Delete lead followup by ID")
    @ApiResponse(responseCode = "204", description = "Successfully delete")
    @ApiResponse(responseCode = "403", description = " Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @DeleteMapping(path = "/leadfup/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id", required = true) @Parameter(description = "Lead FollowUp ID to be deleted") int id) throws AppException, BusException {
        this.service.delete(id, this.getUserProfile());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

	@Operation(summary = "Update lead followup")
    @ApiResponse(responseCode = "201", description = "Successfully updated the Lead Follow Up")
    @ApiResponse(responseCode = "403", description = " Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping(path = "/leadfup")
    public @ResponseBody ResponseEntity<Void> update(@RequestBody(required = true) LeadFollowUpDTO leadFollowUpDTO) throws Exception {
        Optional<LeadFollowUp> lead = this.service.update(leadFollowUpDTO, this.getUserProfile());
        if (lead != null && lead.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
