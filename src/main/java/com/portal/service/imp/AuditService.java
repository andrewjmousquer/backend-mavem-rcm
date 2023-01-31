package com.portal.service.imp;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IAuditDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AuditModel;
import com.portal.service.IAuditService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AuditService implements IAuditService {

	@Autowired
	private IAuditDAO dao;
	
	@Autowired
	private HttpServletRequest request;

	public void save( AuditModel model, UserProfileDTO userProfile ) throws AppException, BusException {
		this.dao.save( model );
	}

	public void save( String details, AuditOperationType operation, UserProfileDTO profileDTO ) throws AppException, BusException {
		
		if( profileDTO == null || profileDTO.getUser() == null || profileDTO.getUser().getUsername() == null ) {
			throw new BusException( "As informações do usuário logado estão inválidas." );
		}
		
		this.save( new AuditModel( new Date(), request.getRemoteAddr(), request.getRemoteHost(), profileDTO.getUser().getUsername(), details, operation ), profileDTO );
	}
	
}
