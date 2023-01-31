package com.portal.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Document;
import com.portal.model.Proposal;

public interface IProposalDocumentService  {
	
	public Optional<Document> getDocument( Integer ppsId, Integer docId ) throws AppException, BusException;
	
	public Optional<Proposal> getProposal( Integer ppsId, Integer docId ) throws AppException, BusException;
	
	public List<Proposal> findByDocument( Integer docId ) throws AppException, BusException;
	
	public List<Document> findByProposal( Integer ppsId ) throws AppException, BusException;
	
	public void save( Integer ppsId, Integer docId ) throws AppException, BusException;
	
	public void delete( Integer ppsId, Integer docId  ) throws AppException, BusException;
	
	public void deleteByProposal( Integer ppsId  ) throws AppException, BusException;

	public Document store(MultipartFile multipartFile, Integer id, UserProfileDTO userProfile);

	public String findDocument(Integer id) throws AppException, BusException, IOException;
}
