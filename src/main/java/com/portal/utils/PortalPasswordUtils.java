package com.portal.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PortalPasswordUtils {

	public PortalPasswordUtils() {
	}
	
	/**
	 * Gera um hash utilizando BCrypt
	 * 
	 * @param senha
	 * @return String
	 */
	public static String geraBCrypt(String senha) {
		if (senha == null) {
			return senha;
		}
		
		BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder();
		return bCryptEncoder.encode(senha);
	}
	
	public static boolean matchPassoword(String password, String encodedPassoword) {
		BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder();
		return bCryptEncoder.matches(password, encodedPassoword);
	}
	
	public static void main(String[] args) {
		System.out.println( geraBCrypt( "heineken" ) );
	}
	
}
