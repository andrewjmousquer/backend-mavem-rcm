package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IPassHistDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PassHistModel;
import com.portal.model.UserModel;
import com.portal.service.IPassHistService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PassHistService implements IPassHistService {
	
	@Autowired
	private IPassHistDAO dao;
		
	@Override
	public List<PassHistModel> getPassHistByUser( UserModel user ) throws BusException, AppException {
		return this.dao.getPassHistByUser( user );
	}
	
	@Override
	public List<PassHistModel> getPassHistDescLimit( UserModel user, int limit ) throws BusException, AppException {
		return this.dao.getPassHistDescLimit(user, limit);
	}
	
	@Override
	public Optional<PassHistModel> save(PassHistModel histModel, UserProfileDTO userProfile) throws BusException, AppException {
		return this.dao.save( histModel );
	}

	@Override
	public void deleteByUser(Integer usrId, UserProfileDTO userProfile) throws BusException, AppException {
		this.dao.deleteByUser(usrId);
	}
	
}
