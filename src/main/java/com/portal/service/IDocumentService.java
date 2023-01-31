package com.portal.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Document;

public interface IDocumentService extends IBaseService<Document> {
	
	public List<Document> listAll( Pageable pageable ) throws AppException, BusException;
	
	public List<Document> find( Document model, Pageable pageable ) throws AppException, BusException;
	
	public List<Document> search( Document model, Pageable pageable ) throws AppException, BusException;
	
}
