package com.portal.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtUser implements UserDetails {

	private static final long serialVersionUID = -268046329085485932L;

	private Integer id;
	private String username;
	private String password;
	private boolean enabled;
	private boolean blocked;
	
	private Collection<? extends GrantedAuthority> authorities;

	public JwtUser(Integer id, String username, String password, boolean enabled, boolean blocked, Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.blocked = blocked;
		this.authorities = authorities;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public String getUsername() {
		return username;
	}
	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	@Override
	public boolean isAccountNonLocked() {
		return !this.blocked;
	}
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	@Override
	public String getPassword() {
		return password;
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}
}
