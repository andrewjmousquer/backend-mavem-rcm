package com.portal.security;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portal.dto.UserLoginDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.UserModel;
import com.portal.service.IUserService;
import com.portal.utils.PortalJwtTokenUtil;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

	private static final String TOKEN_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    public MessageSource messageSource;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PortalJwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private IUserService userService;

	/**
	 * Gera e retorna um novo token JWT.
	 *
	 * @param authenticationDto
	 * @param result
	 * @return ResponseEntity<Response<UserLoginDTO>>
	 */
	@PostMapping
	public ResponseEntity<UserModel> auth(@Valid @RequestBody UserAuthDTO authenticationDto, BindingResult result) throws AuthenticationException, AppException, BusException {
		try {
			UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(authenticationDto.getUsername(), authenticationDto.getPassword());
			Authentication authentication = authenticationManager.authenticate(authReq);

			if(authentication.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(authentication);
				UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationDto.getUsername());

				if(userDetails != null) {
					String token = jwtTokenUtil.obterToken(userDetails);
					Optional<UserModel> userModel = this.userService.findLogin(new UserModel(userDetails.getUsername()));

					if(userModel.isPresent()) {
						this.userService.updatePasswordErrorCount(userModel.get());
						userModel.get().setPassword(null);
						userModel.get().setToken(token);

						return ResponseEntity.ok(userModel.get());
					}
				}
			}

		} catch (Exception e) {
			if(e.getMessage() != null) {
				if(e.getMessage().equals("Bad credentials")) {
					Optional<UserModel> user = this.userService.updateLoginData(authenticationDto.getUsername());
					if(user.isPresent()) {
						if(user.get().getBlocked()) {
							throw new BusException(messageSource.getMessage("error.authentication.errorCount", null, LocaleContextHolder.getLocale()));
						} 
					}
					throw new BusException(messageSource.getMessage("error.authentication.invalid", null, LocaleContextHolder.getLocale()));
					
				} else if (e.getMessage().equals("User account is locked")) {
					throw new BusException(messageSource.getMessage("error.authentication.blocked", null, LocaleContextHolder.getLocale()));
					
				} else if (e.getMessage().equals("User is disabled")) {
					throw new BusException(messageSource.getMessage("error.authentication.notEnabled", null, LocaleContextHolder.getLocale()));
					
				} else {
					throw new BusException(messageSource.getMessage("error.authentication.genericError", null, LocaleContextHolder.getLocale()));
				}
			} else {
				throw new BusException(messageSource.getMessage("error.authentication.genericError", null, LocaleContextHolder.getLocale()));
			}
		}

		return null;
	}

	/**
	 * Gera um novo token com uma nova data de expiração.
	 *
	 * @param request
	 * @return ResponseEntity<Response<UserLoginDTO>>
	 * @throws BusException
	 * @throws NoSuchMessageException
	 */
	@PostMapping(value = "/refresh")
	public ResponseEntity<UserLoginDTO> gerarRefreshTokenJwt(HttpServletRequest request) throws NoSuchMessageException, BusException {
		Optional<String> token = Optional.ofNullable(request.getHeader(TOKEN_HEADER));

		if (token.isPresent() && token.get().startsWith(BEARER_PREFIX)) {
			token = Optional.of(token.get().substring(7));
        }
		if (!token.isPresent()) {
			throw new BusException(messageSource.getMessage("error.authentication.tokennotpreset", null, LocaleContextHolder.getLocale()));
		} else if (!jwtTokenUtil.tokenValido(token.get())) {
			throw new BusException(messageSource.getMessage("error.authentication.tokenInvalid", null, LocaleContextHolder.getLocale()));
		}

		String refreshedToken = jwtTokenUtil.refreshToken(token.get());
		return ResponseEntity.ok(new UserLoginDTO(refreshedToken, null));
	}

}
