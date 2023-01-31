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

import com.portal.dto.PaymentRuleDTO;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;
import com.portal.service.IPaymentRuleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Payment Rules Controller", description = "CRUD for payment method Entity")
public class PaymentRulesController extends BaseController {

    @Autowired
    private IPaymentRuleService service;

    @Operation(summary = "Get a list of payment rules")
    @ApiResponse(responseCode = "200", description = "Successfully return list of Payment Rules", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PaymentRuleDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/paymentrule")
    public @ResponseBody
    ResponseEntity<List<PaymentRuleDTO>> listAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy) throws Exception {

        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "name";
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

        List<PaymentRule> models = this.service.listAll(pageReq);
        return ResponseEntity.ok(PaymentRuleDTO.toDTO(models));
    }

    @Operation(summary = "Find a payment rules")
    @ApiResponse(responseCode = "200", description = "Successfully return list of payment rules", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PaymentRuleDTO.class))))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/paymentrule/find")
    public @ResponseBody
    ResponseEntity<List<PaymentRuleDTO>> find(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
            @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
            @RequestBody(required = true) PaymentRuleDTO dto) throws Exception {

        if (sortBy == null || sortBy.equals("id")) {
            sortBy = "pyr_id";
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

        List<PaymentRule> models = null;
        models = this.service.search(PaymentRule.toEntity(dto, new PaymentMethod()), pageReq);

        return ResponseEntity.ok(PaymentRuleDTO.toDTO(models));
    }

    @Operation(summary = "Get payment rule by ID")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned the PaymentRule by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PaymentRuleDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/paymentrule/{id}")
    public @ResponseBody
    ResponseEntity<PaymentRuleDTO> getById(@PathVariable(name = "id", required = true) @Parameter(description = "Payment Rule ID to be searched") int id) throws Exception {

        Optional<PaymentRule> model = this.service.getById(id);

        if (model != null && model.isPresent()) {
            return ResponseEntity.ok(PaymentRuleDTO.toDTO(model.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Save new payment rule record")
    @ApiResponse(responseCode = "201", description = "Successfully saved the payment rule", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PaymentRuleDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(path = "/paymentrule", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<PaymentRuleDTO> save(@RequestBody(required = true) PaymentRuleDTO dto) throws Exception {
        Optional<PaymentRule> model = this.service.saveRule(dto, this.getUserProfile());

        if (model != null && model.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(PaymentRuleDTO.toDTO(model.get()));
        } else {
            return ResponseEntity.badRequest().body(new PaymentRuleDTO());
        }
    }

    @Operation(summary = "Update payment rule record")
    @ApiResponse(responseCode = "200", description = "Successfully updated the payment rule")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping(path = "/paymentrule")
    public ResponseEntity<Void> update(@RequestBody(required = true) PaymentRuleDTO dto) throws Exception {
        Optional<PaymentRule> model = this.service.updateRule(dto, this.getUserProfile());

        if (model != null && model.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete payment rule by ID")
    @ApiResponse(responseCode = "204", description = "Successfully deleted the payment rule")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @DeleteMapping(path = "/paymentrule/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id", required = true) @Parameter(description = "Payment rule ID to be deleted") int id) throws Exception {
        this.service.delete(id, this.getUserProfile());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
