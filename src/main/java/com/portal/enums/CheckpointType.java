package com.portal.enums;

public enum CheckpointType {

	USER_TYPE("Usuário"),
	
	PRODUCT_TYPE("Produto");
	
	private String i18n;
	
	private CheckpointType(String i18n) {
		this.i18n = i18n;
	}

	public String getI18n() {
		return i18n;
	}
}

