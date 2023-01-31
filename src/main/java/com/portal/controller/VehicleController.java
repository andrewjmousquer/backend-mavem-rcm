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
import org.springframework.web.bind.annotation.CrossOrigin;
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

import com.portal.dto.VehicleDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.VehicleModel;
import com.portal.service.IVehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/protected/vehicle")
@CrossOrigin(origins = "*")
public class VehicleController extends BaseController {

    @Autowired
    private IVehicleService service;

    @Operation( summary = "Get a list of vehicle" )
    @ApiResponse( responseCode = "200", description = "Successfully return list of vehicles", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = VehicleDTO.class) ) ) )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @GetMapping(value = "/listAll")
    public @ResponseBody ResponseEntity<List<VehicleDTO>> listAll(
                                                            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
                                                            @RequestParam(name = "size", required = false, defaultValue = "1000") @Parameter(description = "Size of requested page") int size,
                                                            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
                                                            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy) throws Exception {
        PageRequest pageReq = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        List<VehicleModel> vehicle = this.service.listAll(pageReq);
        return ResponseEntity.ok(VehicleDTO.toDTO(vehicle));
    }

    @Operation( summary = "Find a vehicle" )
    @ApiResponse( responseCode = "200", description = "Successfully return list of vehicles", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = VehicleDTO.class) ) ) )
    @ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
    @ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
    @PostMapping(path = "/find")
    public @ResponseBody
    ResponseEntity<List<VehicleDTO>> find(
            @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
            @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,

            @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,

            @RequestBody(required = true) String searchText) throws Exception {


        if( sortBy == null || sortBy.equals( "id" ) ) {
            sortBy = "vhe_id";
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

        if(searchText.equals("%"))
        	searchText = "";
        
        PageRequest pageReq = PageRequest.of(page, size, Sort.by( Sort.Direction.fromString( sortDir ), sortBy ) );


        List<VehicleModel> models = null;
        
        if(searchText.equals("") || searchText == null)
			return this.listAll(page, size, sortDir, sortBy);
        else
            models = this.service.searchForm(searchText, pageReq);

        return ResponseEntity.ok(VehicleDTO.toDTO(models));
    }
    @Operation(summary = "Get vehicle by Brand")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned the vehicle ", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/getByBrand/{brand}")
    public @ResponseBody
    ResponseEntity<List<VehicleDTO>> getByBrand( @RequestParam(name = "page", required = false, defaultValue = "0") @Parameter(description = "Number of requested page") int page,
                                           @RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter(description = "Size of requested page") int size,
                                           @RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema(allowableValues = {"ASC", "DESC"}) String sortDir,
                                           @RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter(description = "Field of database to sort by") String sortBy,
                                           @RequestParam(name = "like", required = false, defaultValue = "true") @Parameter(description = "Define if should use LIKE to search") boolean like,
                                           @PathVariable(name = "brand", required = true) @Parameter(description = "Vehicle ID to be searched") String brand) throws AppException, BusException {


        if( sortBy == null || sortBy.equals( "id" ) ) {
            sortBy = "vhe_id";
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

        List<VehicleModel> vehicleModel = this.service.getByBrand(brand, pageReq);

        if (vehicleModel != null) {
            return ResponseEntity.ok(VehicleDTO.toDTO(vehicleModel));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @Operation(summary = "Get vehicle by ID")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned the vehicle by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/{id}")
    public @ResponseBody
    ResponseEntity<VehicleDTO> getById(@PathVariable(name = "id", required = true) @Parameter(description = "Vehicle ID to be searched") int id) throws Exception {

        Optional<VehicleModel> vehicleModel = this.service.getById(id);

        if (vehicleModel != null && vehicleModel.isPresent()) {
            return ResponseEntity.ok(VehicleDTO.toDTO(vehicleModel.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get vehicle by Chassi")
    @ApiResponse(responseCode = "200", description = "Successfully found and returned the vehicle by chassi", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping(path = "/chassi/{chassi}")
    public @ResponseBody
    ResponseEntity<VehicleDTO> getByChassi(@PathVariable(name = "chassi", required = true) @Parameter(description = "Vehicle Chassi to be searched") String chassi) throws Exception {

        Optional<VehicleModel> vehicleModel = this.service.getByChassi(chassi);

        if (vehicleModel != null && vehicleModel.isPresent()) {
            return ResponseEntity.ok(VehicleDTO.toDTO(vehicleModel.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Save new vehicle record")
    @ApiResponse(responseCode = "201", description = "Successfully saved the vehicle", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = VehicleDTO.class)))
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehicleDTO> save(@RequestBody VehicleDTO vehicleDTO, BindingResult result) throws AppException, BusException {
        Optional<VehicleModel> vehicleModel = this.service.save(VehicleModel.toEntity(vehicleDTO), this.getUserProfile());
        if (vehicleModel != null && vehicleModel.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(VehicleDTO.toDTO(vehicleModel.get()));
        } else {
            return ResponseEntity.badRequest().body(new VehicleDTO());
        }
    }

    @Operation(summary = "Update vehicle record")
    @ApiResponse(responseCode = "204", description = "Successfully update the Vechicle")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PutMapping
    public ResponseEntity<Void> update(@RequestBody(required = true) VehicleDTO vehicleDTO) throws AppException, BusException {
        Optional<VehicleModel> vehicleModel = this.service.update(VehicleModel.toEntity(vehicleDTO), this.getUserProfile());
        if (vehicleModel != null && vehicleModel.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete vehicle by ID")
    @ApiResponse(responseCode = "204", description = "Successfully deleted the Vechicle")
    @ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id", required = true) @Parameter(description = "Vehicle ID to be deleted") int id) throws AppException, BusException {
        this.service.delete(id, this.getUserProfile());
        return ResponseEntity.status(HttpStatus.OK).build();
    }



}
