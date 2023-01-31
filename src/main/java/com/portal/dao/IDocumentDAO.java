package com.portal.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.model.Document;

public interface IDocumentDAO extends IBaseDAO<Document> {
	
	public List<Document> listAll( Pageable pageable ) throws AppException;
	
	public List<Document> find( Document model, Pageable pageable ) throws AppException;
	
	public List<Document> search( Document model, Pageable pageable ) throws AppException;
	
	/**
	 * Usar a função {@link #listAll(Pageable)}
	 */
	@Deprecated
	public List<Document> list() throws AppException;
	
	/**
	 * Usar a função {@link #find(Document, Pageable)}
	 */
	@Deprecated
	public Optional<Document> find( Document model ) throws AppException; 
	
	/**
	 * Usar a função {@link #search(Document, Pageable)}
	 */
	@Deprecated
	public List<Document> search( Document model ) throws AppException;

	public boolean hasProposalRelationship(Integer docId) throws AppException;
}
