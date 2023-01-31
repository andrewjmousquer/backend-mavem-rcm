package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AddressModel;

public interface IAddressService extends IBaseService<AddressModel> {
	
	public List<AddressModel> fillCity( List<AddressModel> addresses ) throws AppException, BusException;
	
	public Optional<AddressModel> fillCity( AddressModel address ) throws AppException, BusException;
	
}
