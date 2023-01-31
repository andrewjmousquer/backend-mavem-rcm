package com.portal.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.UserModel;
import com.portal.service.imp.UserService;
import com.portal.utils.FileUtils;
import com.portal.utils.PortalJwtTokenUtil;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/image")
@Tag(name = "Image Controller", description = "CRUD for item Entity")
public class ImageController  {

	@Autowired
	public PortalJwtTokenUtil tokenUtils;
	
	@Autowired
	private UserService userService;
	
	@Value("${store.location.item}")
	private String locationItem;
		
	@GetMapping(value = "/getItemIcon/{file}/{token}")
	public void getImageAsByteArray(HttpServletResponse response, 
									@PathVariable(name = "file", required = true) @Parameter( description = "Item ID to be searched" ) String file,
									@PathVariable(name = "token", required = true) @Parameter( description = "Hash to get image" ) String token) throws IOException {
		try {
			if(isValidUser(token)) {
				response.setContentType(FileUtils.getMediaType(file));
				IOUtils.copy(FileUtils.getFileInputStream(this.locationItem + "/" + file), response.getOutputStream());
			}
		} catch (Exception e) {
		}
	}
	
	private boolean isValidUser(String token) {
		if(token != null && !token.isEmpty()) {
			try {
				Optional<UserModel> userModel = userService.findByUsername(new UserModel(tokenUtils.getUsernameFromToken(token)));
				if(userModel.isPresent()) {
					return true;
				}
			} catch (AppException e1) {
				e1.printStackTrace();
			} catch (BusException e1) {
				e1.printStackTrace();
			}	
		}
		
		return false;
	}
}
