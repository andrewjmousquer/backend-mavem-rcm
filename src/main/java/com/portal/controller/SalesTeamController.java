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

import com.portal.dto.SalesTeamDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.SalesTeam;
import com.portal.service.ISalesTeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/protected/salesteam")
public class SalesTeamController extends BaseController {

    @Autowired
    private ISalesTeamService salesTeamService;

    @Operation(summary = "Get a list of Sales Team")
    @ApiResponse(responseCode = "200", description = "Successfully return list of sales team", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SalesTeamDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/listAll")
    public @ResponseBody ResponseEntity<List<SalesTeamDTO>> listAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = { "ASC",
                    "DESC" }) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy)
            throws Exception {

        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "slt_id";
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

        List<SalesTeam> salesTeams = this.salesTeamService.listAll(pageReq);
        return ResponseEntity.ok(SalesTeamDTO.toDTO(salesTeams));
    }

    @Operation(summary = "Find a Sales Team")
    @ApiResponse(responseCode = "200", description = "Successfully return list ", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SalesTeamDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/find")
    public @ResponseBody
    ResponseEntity<List<SalesTeamDTO>> find(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
            @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
            @RequestBody(required = true) SalesTeamDTO dto) throws Exception {


        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "slt_id";
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

        List<SalesTeam> models = this.salesTeamService.find(SalesTeam.toEntity(dto), pageReq);

        return ResponseEntity.ok(SalesTeamDTO.toDTO(models));
    }

    @Operation(summary = "Get sales team by ID")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned  by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SalesTeamDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/{id}")
    public @ResponseBody
    ResponseEntity<SalesTeamDTO> getById(@PathVariable(name = "id", required = true) @Parameter(description = "Sales team ID to be searched") int id) throws Exception {

        Optional<SalesTeam> salesTeam = this.salesTeamService.getById(id);

        if (salesTeam != null && salesTeam.isPresent()) {
            return ResponseEntity.ok(SalesTeamDTO.toDTO(salesTeam.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Save new sales team DTO record")
    @ApiResponse(responseCode = "201", description = "Successfully saved ", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SalesTeamDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path="/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SalesTeamDTO> save(@RequestBody SalesTeamDTO salesTeamDTO, BindingResult result) throws AppException, BusException {

        Optional<SalesTeam> salesTeam = this.salesTeamService.save(SalesTeam.toEntity(salesTeamDTO), this.getUserProfile());

        if (salesTeam != null && salesTeam.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(SalesTeamDTO.toDTO(salesTeam.get()));
        } else {
            return ResponseEntity.badRequest().body(new SalesTeamDTO());
        }
    }

    @Operation(summary = "Update record")
    @ApiResponse(responseCode = "204", description = "Successfully update")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping(path = "/")
    public ResponseEntity<Void> update(@RequestBody(required = true) SalesTeamDTO salesTeamDTO) throws AppException, BusException {

        Optional<SalesTeam> salesTeam = this.salesTeamService.update(SalesTeam.toEntity(salesTeamDTO), this.getUserProfile());

        if (salesTeam != null && salesTeam.isPresent()) {
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
        this.salesTeamService.delete(id, this.getUserProfile());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
