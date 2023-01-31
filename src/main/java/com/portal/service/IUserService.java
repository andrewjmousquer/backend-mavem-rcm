package com.portal.service;

import java.util.Optional;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.UserModel;

public interface IUserService extends IBaseService<UserModel> {

	public Optional<UserModel> saveUserConfig(UserModel model) throws AppException, BusException;
	
	public Optional<UserModel> findLogin(UserModel model) throws AppException, BusException;
	
	public Optional<UserModel> findByUsername(UserModel model) throws AppException, BusException;
	
	public Optional<UserModel> updateLoginData(String username) throws AppException, BusException;
	
	public void updatePasswordErrorCount(UserModel user) throws AppException, BusException;
	
	public void resetPassword(Integer id) throws AppException, BusException;

	public Optional<UserModel> changePassword(UserModel model, UserProfileDTO userProfile) throws AppException, BusException;

}
