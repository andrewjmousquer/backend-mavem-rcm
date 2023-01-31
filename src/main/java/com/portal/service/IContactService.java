package com.portal.service;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Contact;

public interface IContactService extends IBaseService<Contact> {
	
	public List<Contact> findByPerson(Integer id) throws AppException, BusException;
    
}