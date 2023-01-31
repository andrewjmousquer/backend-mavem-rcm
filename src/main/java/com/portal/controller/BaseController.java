package com.portal.controller;

import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BadRequestException;
import com.portal.exceptions.BusException;
import com.portal.model.UserModel;
import com.portal.service.IUserService;
import com.portal.utils.PortalJwtTokenUtil;

@Controller
@CrossOrigin(origins = "*")
public abstract class BaseController {

	@Autowired
	public PortalJwtTokenUtil tokenUtils;
	
    @Autowired
    public MessageSource messageSource;
    
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private IUserService userService;
	
	public UserProfileDTO getUserProfile() {
		return getUserProfileGen( true );
	}
	
	public UserProfileDTO getUserProfile( boolean loadDefaultHolCust) {
		return getUserProfileGen(loadDefaultHolCust);
	}
	
	private UserProfileDTO getUserProfileGen(boolean loadDefaultHolCust) {
		try {
			String authorizationHeader = request.getHeader("Authorization");
			String language = request.getHeader("Language");
			String country = request.getHeader("Country");
			
			if( authorizationHeader != null)  {
				if( authorizationHeader.length() < 7 ) {
					throw new BadRequestException( "Invalid authorization tokenf." );
				}
				
				String token = authorizationHeader.substring(7); // The part after "Bearer"
				UserProfileDTO profileDTO = new UserProfileDTO();
				Optional<UserModel> userModel = userService.findByUsername(new UserModel(tokenUtils.getUsernameFromToken(token)));
			
				if(userModel.isPresent()) {
					profileDTO.setUser( this.userService.getById(userModel.get().getId()).get());
					
					if( language != null && country != null) {
						LocaleContextHolder.setLocale(new Locale(language, country));
					}
					
					//TODO REVER A REGRA DE HOLDING E CUSTOMER PADRAO
					
					return profileDTO;
				}
			
			} else {
				throw new BusException("Usuário não encontrado");
			}
		} catch (AppException e1) {
			e1.printStackTrace();
		} catch (BusException e1) {
			e1.printStackTrace();
		}	
		
		return null;
	}
	
}
