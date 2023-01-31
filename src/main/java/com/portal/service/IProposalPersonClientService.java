package com.portal.service;

import java.util.List;
import java.util.Optional;

import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalPerson;

public interface IProposalPersonClientService  {
	
	public Optional<Person> getPerson( Integer ppsId, Integer perId ) throws AppException, BusException;
	
	public Optional<Proposal> getProposal( Integer ppsId, Integer perId ) throws AppException, BusException;
	
	public List<Proposal> findByPerson( Integer perId ) throws AppException, BusException;
	
	public List<ProposalPerson> findByProposal( Integer ppsId ) throws AppException, BusException;
	
	public void delete( Integer ppsId, Integer perId  ) throws AppException, BusException;
	
	public void deleteByProposal( Integer ppsId  ) throws AppException, BusException;

	public void save(Integer proposalId, ProposalPerson proposalPerson, UserProfileDTO userProfile) throws AppException, BusException;
	
	public void update(Integer proposalId, ProposalPerson proposalPerson, UserProfileDTO userProfile) throws AppException, BusException;
}
