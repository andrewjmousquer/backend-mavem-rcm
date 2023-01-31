package com.portal.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.UserModel;
import com.portal.service.IUserService;

@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IUserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		UserModel userSearch = new UserModel(username);
		Optional<UserModel> model = null;
		try {
			model = userService.findByUsername(userSearch);
		} catch (AppException e) {
			e.printStackTrace();
		} catch (BusException e) {
			e.printStackTrace();
		}

		if (model.isPresent()) {
			return JwtUserFactory.create(model.get());
		}

		throw new UsernameNotFoundException(messageSource.getMessage("error.authentication.usernotfound", null, LocaleContextHolder.getLocale()));
	}

}
