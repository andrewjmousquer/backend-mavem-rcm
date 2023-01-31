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

import com.portal.dto.ProposalApprovalDTO;
import com.portal.dto.ProposalApprovalDetailDTO;
import com.portal.dto.ProposalApprovalFilterDTO;
import com.portal.dto.ProposalApprovalListDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ProposalApproval;
import com.portal.service.IProposalApprovalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected/proposalapproval")
@Tag(name = "Proposal Approval Controller", description = "CRUD for Proposal Approval Entity")
public class ProposalApprovalController extends BaseController {

    @Autowired
    private IProposalApprovalService service;

    @Operation(summary = "Get all list of Proposal Approval")
    @ApiResponse(responseCode = "200", description = "Successfully return list of Proposal Approval", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/listAll")
    public @ResponseBody
    ResponseEntity<List<ProposalApprovalListDTO>> listAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy) throws Exception {

        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "pps_id";
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

        List<ProposalApprovalListDTO> proposalApprovals = this.service.listAll(pageReq);

        return ResponseEntity.ok(proposalApprovals);
    }

    @Operation(summary = "Find a list Proposal Approval")
    @ApiResponse(responseCode = "200", description = "Successfully return list ", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ProposalApprovalDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/find")
    public @ResponseBody
    ResponseEntity<List<ProposalApprovalListDTO>> find(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
            @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
            @RequestBody(required = true) ProposalApprovalFilterDTO filterDTO) throws Exception {


        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "pps_id";
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

        List<ProposalApprovalListDTO> proposalApprovals = this.service.search(filterDTO, getUserProfile(), pageReq);

        return ResponseEntity.ok(proposalApprovals);
    }

    @Operation(summary = "Get a Proposal Approval by ID")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned  by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalApprovalDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/{id}")
    public @ResponseBody
    ResponseEntity<ProposalApprovalDetailDTO> getById(@PathVariable(name = "id", required = true) @Parameter(description = "Proposal Approval ID to be searched") int id) throws Exception {

        Optional<ProposalApprovalDetailDTO> proposalApproval = this.service.getByIdProposalAppoval(id);

        if (proposalApproval != null && proposalApproval.isPresent()) {
            return ResponseEntity.ok(proposalApproval.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "save new Proposal Approval record")
    @ApiResponse(responseCode = "201", description = "Successfully saved ", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProposalApprovalDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProposalApprovalDTO> save(@RequestBody ProposalApprovalDTO dto, BindingResult result) throws AppException, BusException {

        Optional<ProposalApproval> proposalApproval = this.service.save(ProposalApproval.toEntity(dto), this.getUserProfile());

        if (proposalApproval != null && proposalApproval.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(ProposalApprovalDTO.toDTO(proposalApproval.get()));
        } else {
            return ResponseEntity.badRequest().body(new ProposalApprovalDTO());
        }

    }


    @Operation(summary = "Update a Proposal Approval record")
    @ApiResponse(responseCode = "204", description = "Successfully update")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping(path = "/")
    public ResponseEntity<Void> update(@RequestBody(required = true) ProposalApprovalDTO dto) throws AppException, BusException {

        Optional<ProposalApproval> proposalApproval = this.service.update(ProposalApproval.toEntity(dto), this.getUserProfile());

        if (proposalApproval != null && proposalApproval.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete by ID")
    @ApiResponse(responseCode = "204", description = "Successfully deleted ")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id", required = true) @Parameter(description = " ID to be deleted") int id) throws AppException, BusException {

        this.service.delete(id, this.getUserProfile());

        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
