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

import com.portal.dto.ProposalApprovalRuleDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalApprovalRule;
import com.portal.service.IProposalApprovalRuleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Proposal Approval Rule Controller", description = "CRUD for Proposal Approval Rule Entity")
public class ProposalApprovalRuleController extends BaseController{

    @Autowired
    private IProposalApprovalRuleService service;

    @Operation(summary = "Get all list of Proposal Approval Rules")
    @ApiResponse(responseCode = "200", description = "Successfully return list of Proposal Approval Rules", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/proposalapprovalrule/listAll")
    public @ResponseBody
    ResponseEntity<List<ProposalApprovalRuleDTO>> listAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy) throws Exception {

        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "par_id";
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

        List<ProposalApprovalRule> proposalApprovalRules = this.service.listAll(pageReq);

        return ResponseEntity.ok(ProposalApprovalRuleDTO.toDTO(proposalApprovalRules));
    }

    @Operation(summary = "Find a list Proposal Approval Rule")
    @ApiResponse(responseCode = "200", description = "Successfully return list ", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProposalApprovalRuleDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/proposalapprovalrule/find")
    public @ResponseBody ResponseEntity<List<ProposalApprovalRuleDTO>> find(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
            @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
            @RequestBody(required = true) ProposalApprovalRuleDTO dto) throws Exception {


        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "par_id";
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

        List<ProposalApprovalRule> proposalApprovalRules = this.service.searchForm(dto.getJob().getName(), pageReq);

        return ResponseEntity.ok(ProposalApprovalRuleDTO.toDTO(proposalApprovalRules));
    }

    @Operation(summary = "Get a Proposal Approval Rule by ID")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned  by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalApprovalRuleDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/proposalapprovalrule/{id}")
    public @ResponseBody
    ResponseEntity<ProposalApprovalRuleDTO> getById(@PathVariable(name = "id", required = true) @Parameter(description = "Proposal Approval Rule ID to be searched") int id) throws Exception {

        Optional<ProposalApprovalRule> proposalApprovalRule = this.service.getById(id);

        if (proposalApprovalRule != null && proposalApprovalRule.isPresent()) {
            return ResponseEntity.ok(ProposalApprovalRuleDTO.toDTO(proposalApprovalRule.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "save new Proposal Approval Rule record")
    @ApiResponse(responseCode = "201", description = "Successfully saved ", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalApprovalRuleDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/proposalapprovalrule", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProposalApprovalRuleDTO> save(@RequestBody ProposalApprovalRuleDTO dto, BindingResult result) throws AppException, BusException {
    	Optional<ProposalApprovalRule> proposalApprovalRule = this.service.save(ProposalApprovalRule.toEntity(dto), this.getUserProfile());
        if (proposalApprovalRule != null && proposalApprovalRule.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(ProposalApprovalRuleDTO.toDTO(proposalApprovalRule.get()));
        } else {
            return ResponseEntity.badRequest().body(new ProposalApprovalRuleDTO());
        }
    }


    @Operation(summary = "Update a Proposal Approval Rule record")
    @ApiResponse(responseCode = "204", description = "Successfully update")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping(path = "/proposalapprovalrule")
    public ResponseEntity<Void> update(@RequestBody(required = true) ProposalApprovalRuleDTO dto) throws AppException, BusException {

        Optional<ProposalApprovalRule> proposalApprovalRule = this.service.update(ProposalApprovalRule.toEntity(dto), this.getUserProfile());

        if (proposalApprovalRule != null && proposalApprovalRule.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete by ID")
    @ApiResponse(responseCode = "204", description = "Successfully deleted ")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @DeleteMapping(path = "/proposalapprovalrule/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id", required = true) @Parameter(description = " ID to be deleted") int id) throws AppException, BusException {

        this.service.delete(id, this.getUserProfile());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
