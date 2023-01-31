package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.PassHistModel;
import com.portal.model.UserModel;

public interface IPassHistDAO {
	
	public List<PassHistModel> getPassHistByUser( UserModel user ) throws AppException;
	
	public List<PassHistModel> getPassHistDescLimit( UserModel user, int limit ) throws AppException;
	
	public Optional<PassHistModel> save( PassHistModel model ) throws AppException;
	
	public void deleteByUser(Integer usrId) throws AppException;
		
}
