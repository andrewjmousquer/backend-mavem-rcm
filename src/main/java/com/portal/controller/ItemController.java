package com.portal.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
import org.springframework.web.multipart.MultipartFile;

import com.portal.dto.ImageDTO;
import com.portal.dto.ItemDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Item;
import com.portal.service.imp.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/protected")
@Tag(name = "Item Controller", description = "CRUD for item Entity")
public class ItemController extends BaseController {
	public static final String FORBIDDEN = "A imagem deve ser menor que 5MB";
	@Autowired
	private ItemService service;
	
	@Value("${store.location.item}")
	private String locationItem;
	
	@Operation( summary = "Get a list of itens" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of itens", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ItemDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/item")
	public @ResponseBody ResponseEntity<List<ItemDTO>> listAll(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "itm_id";
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
		
		PageRequest pageReq = PageRequest.of(page, size, Sort.by( Direction.fromString( sortDir ), sortBy ) );
		
		List<Item> models = this.service.listAll( pageReq );
		return ResponseEntity.ok( ItemDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Find a item" )
	@ApiResponse( responseCode = "200", description = "Successfully return list of itens", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema( schema = @Schema(implementation = ItemDTO.class) ) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping(path = "/item/find")
	public @ResponseBody ResponseEntity<List<ItemDTO>> find(
														@RequestParam(name = "page", required = false, defaultValue = "0") @Parameter( description = "Number of requested page" ) int page,
														@RequestParam(name = "size", required = false, defaultValue = "10000") @Parameter( description = "Size of requested page" ) int size,
														@RequestParam(name = "sortDir", required = false, defaultValue = "ASC") @Schema( allowableValues= {"ASC", "DESC"} ) String sortDir,
														@RequestParam(name = "sortBy", required = false, defaultValue = "id") @Parameter( description = "Field of database to sort by" ) String sortBy,
														@RequestParam(name = "like", required = false, defaultValue = "true") @Parameter( description = "Define if should use LIKE to search" ) boolean like,
														@RequestBody(required = true) ItemDTO dto ) throws Exception {
		
		if( sortBy == null || sortBy.equals( "id" ) ) {
			sortBy = "itm_id";
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
		
		PageRequest pageReq = PageRequest.of(page, size, Sort.by( Direction.fromString( sortDir ), sortBy ) );
		
		List<Item> models = null;
		
		Item searchModel = Item.toEntity( dto );
		if( like ) {
			models = this.service.search(searchModel, pageReq);
		} else {
			models = this.service.find(searchModel, pageReq);
		}
		
		return ResponseEntity.ok( ItemDTO.toDTO( models ) ) ;
	}
	
	@Operation( summary = "Get item by ID" )
	@ApiResponse( responseCode = "200", description = "Successfully found and returned the Item by id", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ItemDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@GetMapping(path = "/item/{id}")
	public @ResponseBody ResponseEntity<ItemDTO> getById( @PathVariable( name = "id", required = true ) @Parameter( description = "Item ID to be searched" ) int id  ) throws Exception {
		
		Optional<Item> model = this.service.getById( id );
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.ok( ItemDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.status( HttpStatus.NOT_FOUND ).build();
		}
	}
	
	@Operation( summary = "Save new item record" )
	@ApiResponse( responseCode = "201", description = "Successfully saved the Item", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ItemDTO.class) ) )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PostMapping( path = "/item", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<ItemDTO> save( @RequestBody(required = true) ItemDTO dto ) throws Exception {
		Optional<Item> model = this.service.save( Item.toEntity( dto ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.CREATED ).body(  ItemDTO.toDTO( model.get() ) );
		} else {
			return ResponseEntity.badRequest().body(new ItemDTO());
		}
	}
	
	@Operation( summary = "Update item record" )
	@ApiResponse( responseCode = "200", description = "Successfully updated the Item" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@PutMapping( path = "/item" )
	public ResponseEntity<Void> update( @RequestBody(required = true) ItemDTO dto) throws Exception {
		Optional<Item> model = this.service.update( Item.toEntity( dto ) , this.getUserProfile());
		
		if( model != null && model.isPresent() ) {
			return ResponseEntity.status( HttpStatus.OK ).build();
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@Operation( summary = "Delete item by ID" )
	@ApiResponse( responseCode = "204", description = "Successfully deleted the Item" )
	@ApiResponse( responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE  ) )
	@ApiResponse( responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE ) )
	@DeleteMapping( path = "/item/{id}" )
	public ResponseEntity<Void> delete( @PathVariable( name = "id", required = true ) @Parameter( description = "Item ID to be deleted" ) int id ) throws Exception {
		this.service.delete( id, this.getUserProfile());
		return ResponseEntity.status( HttpStatus.OK ).build();
	}

	@Operation(summary = "Upload file of itens")
	@ApiResponse(responseCode = "200", description = "Successfully upload of itens", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@PostMapping(path = "/item/upload/{id}/{type}")
	public ResponseEntity<Void> upload(@RequestBody MultipartFile file, 
										@PathVariable(name = "id", required = true) @Parameter(description = "Item ID to be saved") Integer id,
										@PathVariable(name = "type", required = true) @Parameter(description = "Item type to be save") String type) throws Exception {

		try {
			this.service.store(file, id, type, this.getUserProfile());
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (BusException e) {
			throw new BusException(e);
		}
	}

	@Operation(summary = "Get Image file from item")
	@ApiResponse(responseCode = "200", description = "Successfully get image", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "403", description = "Authorization fail", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@ApiResponse(responseCode = "400", description = "Wrong business logic will retorna CODE=600, otherwise it's wrong url address", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	@GetMapping(value = "/item/image/{name}")
	public @ResponseBody
	ResponseEntity<ImageDTO> getImage(@PathVariable(name = "name", required = true) @Parameter(description = "Item ID to be searched") String name) throws IOException, AppException {
		ImageDTO image = new ImageDTO(this.service.getItemImage(name));
		if (image != null) {
			return ResponseEntity.ok(image);
		} 
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}
	
	@GetMapping(path = "/item/getItemIcon/{id}")
	public @ResponseBody byte[] getImage(@PathVariable( name = "id", required = true ) @Parameter( description = "Item ID to be searched" ) int id ) throws IOException, AppException, BusException {
	    return this.service.getImageIcon(id);
	}
	
	@GetMapping(value = "/item/getItemIcon/v2/{file}")
	public void getImageAsByteArray(HttpServletResponse response,@PathVariable(name = "file", required = true) @Parameter( description = "Item ID to be searched" ) String file) throws IOException {
		File initialFile = new File(this.locationItem + "/" + file);
		if(initialFile.isFile()) {
			InputStream targetStream = new FileInputStream(initialFile);
			if(file.contains(".jpg") || file.contains(".jpeg")) {
				response.setContentType(MediaType.IMAGE_JPEG_VALUE);
			} else if(file.contains(".png")) {
				response.setContentType(MediaType.IMAGE_PNG_VALUE);
			} else if(file.contains(".gif")) {
				response.setContentType(MediaType.IMAGE_GIF_VALUE);
			}
			
			IOUtils.copy(targetStream, response.getOutputStream());
		}
	}
}
