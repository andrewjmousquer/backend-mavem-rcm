package com.portal.dao;

import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.UserModel;

public interface IUserDAO extends IBaseDAO<UserModel>{

	public Optional<UserModel> saveUserConfig(UserModel model) throws AppException;

	public Optional<UserModel> findLogin(UserModel model) throws AppException;

	public Optional<UserModel> changePassword(UserModel model) throws AppException;
	
}
