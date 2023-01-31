package com.portal.dao;

import com.portal.exceptions.AppException;
import com.portal.model.AuditModel;

public interface IAuditDAO {

	public void save(AuditModel model) throws AppException;
	
}
