package com.portal.dao;

import java.util.List;

import com.portal.exceptions.AppException;
import com.portal.model.Contact;

public interface IContactDAO extends IBaseDAO<Contact> {

	public List<Contact> findByPerson(Integer id) throws AppException;
}
