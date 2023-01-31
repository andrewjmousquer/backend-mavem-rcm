package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PersonRelated;

public interface IPersonRelatedService extends IBaseService<PersonRelated> {
	
	public List<PersonRelated> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<PersonRelated> find( PersonRelated model, Pageable pageable ) throws AppException, BusException;
	
	public List<PersonRelated> search( PersonRelated model, Pageable pageable ) throws AppException, BusException;
	
	public Optional<PersonRelated> getById(Integer id, boolean withRule) throws AppException, BusException;

    List<PersonRelated> findByPerson(Integer id) throws AppException;
}
