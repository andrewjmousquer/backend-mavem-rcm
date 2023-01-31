package com.portal.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Person;

public interface IPersonService extends IBaseService<Person> {

	List<Person> listAll(Pageable pageable) throws AppException, BusException;

	List<Person> find(Person model, Pageable pageable) throws AppException, BusException;

	List<Person> search(Person model, Pageable pageable) throws AppException, BusException;

	List<Person> fillContact(List<Person> models) throws AppException, BusException;

	Person fillContact(Person model) throws AppException, BusException;

	void fillQualification(Person person) throws AppException, BusException;

	void fillQualification(List<Person> persons) throws AppException, BusException;

	void fillBankAccount(Person person) throws AppException, BusException;

	void fillBankAccount(List<Person> persons) throws AppException, BusException;

	void fillAddress(Person person) throws AppException, BusException;
	
	void fillAddress(List<Person> persons) throws AppException, BusException;

	void fillPersonRelated(Person person) throws AppException, BusException;
	
	void fillPersonRelated(List<Person> persons) throws AppException, BusException;

    Person fillEntity(Person entity) throws AppException, BusException;

    List<Person> searchForm(String searchText, Pageable pageable) throws AppException;
    
	List<Person> searchByContact(String contact) throws AppException;

	Optional<Person> searchByDocument(String document) throws AppException;

}
