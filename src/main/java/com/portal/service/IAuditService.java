package com.portal.service;

import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AuditModel;

public interface IAuditService {
	
	public void save( AuditModel model, UserProfileDTO userProfile ) throws AppException, BusException;
	
	public void save( String details, AuditOperationType operation, UserProfileDTO userProfile ) throws AppException,BusException;
	
}
