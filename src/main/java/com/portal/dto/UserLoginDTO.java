package com.portal.dto;

import com.portal.model.UserModel;

public class UserLoginDTO {

	private Integer id;
	private String username;
	private String personname;
	private String password;
	private String token;
	
	public UserLoginDTO(String token, UserModel user) {
		this.token = token;
		this.id = user.getId();
		this.username = user.getUsername();
		this.personname = user.getPerson().getName();
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPersonname() {
		return personname;
	}
	public void setPersonname(String personname) {
		this.personname = personname;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
}
