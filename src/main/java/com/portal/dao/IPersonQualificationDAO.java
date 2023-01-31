package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.PersonQualification;

public interface IPersonQualificationDAO {

	public List<PersonQualification> find( PersonQualification qualification ) throws AppException;
	
	public void save( PersonQualification qualification ) throws AppException;
	
	public void delete( Integer perId, Integer qlfId  ) throws AppException;
	
	public void deleteByPerson( Integer perId  ) throws AppException;
}
