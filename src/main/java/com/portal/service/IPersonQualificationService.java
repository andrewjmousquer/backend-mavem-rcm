package com.portal.service;

import java.util.List;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Person;
import com.portal.model.PersonQualification;

public interface IPersonQualificationService {

	public List<PersonQualification> findByPerson( Integer perId ) throws AppException, BusException;
	
	public List<PersonQualification> findByQualification( Integer qlfId ) throws AppException, BusException;
	
	public List<PersonQualification> find( PersonQualification qualification ) throws AppException, BusException;
	
	public void save( PersonQualification qualification ) throws AppException, BusException;
	
	public void delete( Integer perId, Integer qlfId  ) throws AppException, BusException;
	
	public void deleteByPerson( Integer perId ) throws AppException, BusException;

    public void deleteByPerson(PersonQualification modelDelete, UserProfileDTO userProfile) throws AppException;

    public void update(PersonQualification entity, UserProfileDTO userProfile);

}

