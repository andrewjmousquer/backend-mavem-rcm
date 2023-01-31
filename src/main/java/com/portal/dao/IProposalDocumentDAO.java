package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.Document;
import com.portal.model.Proposal;

public interface IProposalDocumentDAO {
	
	public Optional<Document> getDocument( Integer ppsId, Integer docId ) throws AppException;
	
	public Optional<Proposal> getProposal( Integer ppsId, Integer docId ) throws AppException;
	
	public List<Proposal> findByDocument( Integer docId ) throws AppException;
	
	public List<Document> findByProposal( Integer ppsId ) throws AppException;
	
	public void save( Integer ppsId, Integer docId ) throws AppException;
	
	public void delete( Integer ppsId, Integer docId  ) throws AppException;
	
	public void deleteByProposal( Integer ppsId  ) throws AppException;

	public String getDocumentUrl(Integer id) throws AppException;
}
