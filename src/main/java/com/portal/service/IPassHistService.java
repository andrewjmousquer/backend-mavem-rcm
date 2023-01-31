package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PassHistModel;
import com.portal.model.UserModel;

public interface IPassHistService {
	
public List<PassHistModel> getPassHistByUser( UserModel user ) throws AppException, BusException;
	
	public List<PassHistModel> getPassHistDescLimit( UserModel user, int limit ) throws AppException, BusException;
	
	public Optional<PassHistModel> save( PassHistModel model, UserProfileDTO userProfile) throws AppException, BusException;
	
	public void deleteByUser(Integer usrId, UserProfileDTO userProfile) throws AppException, BusException;
	
}