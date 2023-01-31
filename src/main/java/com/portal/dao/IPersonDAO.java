package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Person;

public interface IPersonDAO extends IBaseDAO<Person>{
	
	public List<Person> listAll( Pageable pageable ) throws AppException;
	
	public List<Person> find( Person model, Pageable pageable ) throws AppException;
	
	public List<Person> search( Person model, Pageable pageable ) throws AppException;
	
	public boolean hasPartnerRelationship( Integer perId ) throws AppException;
	
	public boolean hasPartnerPersonRelationship( Integer perId ) throws AppException;
	
	public boolean hasProposalRelationship( Integer perId ) throws AppException;
	
	public boolean hasProposalDetailRelationship( Integer perId ) throws AppException;
	
	public boolean hasCommissionRelationship( Integer perId ) throws AppException;
	
	public boolean hasLeadRelationship( Integer perId ) throws AppException;
	
	public boolean hasHoldingRelationship( Integer perId ) throws AppException;
	
	public boolean hasUserRelationship( Integer perId ) throws AppException;
	
	public boolean hasSellerRelationship( Integer perId ) throws AppException;

	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Person> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Person, Pageable)}
	 */
	@Deprecated
	public Optional<Person> find( Person model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Person, Pageable)}
	 */
	@Deprecated
	public List<Person> search( Person model ) throws AppException;

	public  List<Person> searchForm(String searchText, Pageable pageable) throws AppException;
	
    public List<Person> findByContact(String contact) throws AppException;
	
    public Optional<Person> searchByDocument(String searchText) throws AppException;

}
