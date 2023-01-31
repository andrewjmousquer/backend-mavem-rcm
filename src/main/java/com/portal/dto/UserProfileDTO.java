package com.portal.dto;

import java.io.Serializable;

import com.portal.model.CustomerModel;
import com.portal.model.HoldingModel;
import com.portal.model.UserModel;

public class UserProfileDTO implements Serializable {

	private static final long serialVersionUID = 5945774641729986795L;

	private UserModel user;
	private HoldingModel selectedHolding;
	private CustomerModel selectedCustomer;
	
	public UserProfileDTO() {}
	
	public UserProfileDTO(UserModel user) {
		this.user = user;
	}
	
	public UserModel getUser() {
		return user;
	}
	public void setUser(UserModel user) {
		this.user = user;
	}
	public HoldingModel getSelectedHolding() {
		return selectedHolding;
	}
	public void setSelectedHolding(HoldingModel selectedHolding) {
		this.selectedHolding = selectedHolding;
	}
	public CustomerModel getSelectedCustomer() {
		return selectedCustomer;
	}
	public void setSelectedCustomer(CustomerModel selectedCustomer) {
		this.selectedCustomer = selectedCustomer;
	}
}
