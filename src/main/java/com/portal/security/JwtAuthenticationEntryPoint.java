package com.portal.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    public MessageSource messageSource;
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
		if(authException != null) {
			if(authException.getMessage().equals("Bad credentials")) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, messageSource.getMessage("error.authentication.invalid", null, LocaleContextHolder.getLocale()));
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, messageSource.getMessage("error.authentication.denied", null, LocaleContextHolder.getLocale()));
			}
		}
		
	}

}
