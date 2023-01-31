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

import com.portal.dto.ModelItemCostDTO;
import com.portal.exceptions.BusException;
import com.portal.model.ModelItemCost;
import com.portal.service.IModelItemCostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Model Item Cost Rules Controller", description = "CRUD for Model Item Cost method Entity")
public class ModelItemCostController extends BaseController {

    @Autowired
    private IModelItemCostService service;

    @Operation(summary = "Get a list of all Model Item Cost")
    @ApiResponse(responseCode = "200", description = "Successfully return list of Model Item Cost", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ModelItemCostDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/modelItemCost")
    public @ResponseBody
    ResponseEntity<List<ModelItemCostDTO>> listAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy) throws Exception {

        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "start_date";
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

        List<ModelItemCost> models = this.service.listAll(pageReq);
        return ResponseEntity.ok(ModelItemCostDTO.toDTO(models));
    }
    
    @Operation(summary = "Get Model Item Cost by ID")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned the ModelItemCost by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ModelItemCostDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/modelItemCost/{id}")
    public @ResponseBody
    ResponseEntity<ModelItemCostDTO> getById(@PathVariable(name = "id", required = true) @Parameter(description = "Model Item Cost ID to be searched") int id) throws Exception {

        Optional<ModelItemCost> model = this.service.getById(id);

        if (model != null && model.isPresent()) {
            return ResponseEntity.ok(ModelItemCostDTO.toDTO(model.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @Operation(summary = "Save new Model Item Cost")
    @ApiResponse(responseCode = "201", description = "Successfully saved the Model Item Cost", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ModelItemCostDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/modelItemCost", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<ModelItemCostDTO> save(@RequestBody(required = true) ModelItemCostDTO dto) throws Exception, BusException {
        Optional<ModelItemCost> model = this.service.save(ModelItemCost.toEntity(dto), this.getUserProfile());

        if (model != null && model.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(ModelItemCostDTO.toDTO(model.get()));
        } else {
            return ResponseEntity.badRequest().body(new ModelItemCostDTO());
        }
    }
    
    @Operation(summary = "Update Model Item Cost")
    @ApiResponse(responseCode = "200", description = "Successfully updated the Model Item Cost")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping(path = "/modelItemCost")
    public ResponseEntity<Void> update(@RequestBody(required = true) ModelItemCostDTO dto) throws Exception, BusException {
        Optional<ModelItemCost> model = this.service.update(ModelItemCost.toEntity(dto), this.getUserProfile());

        if (model != null && model.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete Model Item Cost by ID")
    @ApiResponse(responseCode = "204", description = "Successfully deleted the Model Item Cost")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @DeleteMapping(path = "/modelItemCost/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id", required = true) @Parameter(description = "Model Item Cost record ID to be deleted") int id) throws Exception {
        this.service.delete(id, this.getUserProfile());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Find a Model Item Cost")
    @ApiResponse(responseCode = "200", description = "Successfully return list of Model Item Cost", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ModelItemCostDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/modelItemCost/find")
    public @ResponseBody
    ResponseEntity<List<ModelItemCostDTO>> find(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
            @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
            @RequestBody(required = true) ModelItemCostDTO dto) throws Exception {

        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "start_date";
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

        List<ModelItemCost> models = null;
        models = this.service.find(ModelItemCost.toEntity(dto), pageReq);

        return ResponseEntity.ok(ModelItemCostDTO.toDTO(models));
    }
    
    @Operation(summary = "Validate a List of Model Item Cost")
    @ApiResponse(responseCode = "200", description = "Successfully validated the List of Model Item Cost")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/modelItemCost/validateList")
    public ResponseEntity<List<ModelItemCostDTO>> validateList(@RequestBody(required = true) List<ModelItemCostDTO> dtoList) throws Exception, BusException {
    	dtoList.stream().forEach(dto -> {
    		try {
    			this.service.validateHasDuplicate(ModelItemCost.toEntity(dto));
    			dto.setHasValidationError(false);
    		} catch (Exception e) {
				dto.setHasValidationError(true);
			}
    	});
    	return ResponseEntity.ok(dtoList);
    }
    
    @Operation(summary = "Save a List of Model Item Cost")
    @ApiResponse(responseCode = "200", description = "Successfully saved the List of Model Item Cost")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/modelItemCost/saveList")
    public ResponseEntity<Void> saveList(@RequestBody(required = true) List<ModelItemCostDTO> dtoList) throws Exception, BusException {
    	for (ModelItemCostDTO dto : dtoList) {
    		this.service.save(ModelItemCost.toEntity(dto), this.getUserProfile());
    	}
    	return ResponseEntity.status(HttpStatus.OK).build();
    }
    
    @Operation(summary = "Update a List of Model Item Cost")
    @ApiResponse(responseCode = "200", description = "Successfully updated the List of Model Item Cost")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will return a CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping(path = "/modelItemCost/updateList")
    public ResponseEntity<Void> updateList(@RequestBody(required = true) List<ModelItemCostDTO> dtoList) throws Exception, BusException {
    	for (ModelItemCostDTO dto : dtoList) {
    		this.service.update(ModelItemCost.toEntity(dto), this.getUserProfile());
    	}
    	return ResponseEntity.status(HttpStatus.OK).build();
    }

}
