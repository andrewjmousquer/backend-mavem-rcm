package com.portal.security;

import org.hibernate.validator.constraints.NotEmpty;

public class UserAuthDTO {
	
	private String username;
	private String password;

	public UserAuthDTO() {
	}

	@NotEmpty(message = "{error.authentication.usernotempty}")
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@NotEmpty(message = "{error.authentication.passnotempty}")
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "JwtAuthenticationRequestDto [username=" + username + ", password=" + password + "]";
	}

}
