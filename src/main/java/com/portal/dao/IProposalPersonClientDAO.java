package com.portal.dao;

import java.util.List;
import java.util.Optional;

import com.portal.exceptions.AppException;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalPerson;

public interface IProposalPersonClientDAO {
	
	public Optional<Person> getPerson( Integer ppsId, Integer perId ) throws AppException;
	
	public Optional<Proposal> getProposal( Integer ppsId, Integer perId ) throws AppException;
	
	public List<Proposal> findByPerson( Integer perId ) throws AppException;
	
	public List<ProposalPerson> findByProposal( Integer ppsId ) throws AppException;
	
	public void delete( Integer ppsId, Integer perId  ) throws AppException;
	
	public void deleteByProposal( Integer ppsId  ) throws AppException;

	public void save(Integer ppsId, ProposalPerson proposalPerson) throws AppException;
	
	public void update(Integer ppsId, ProposalPerson proposalPerson) throws AppException;
}
