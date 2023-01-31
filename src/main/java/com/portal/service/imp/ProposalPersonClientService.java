package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IProposalPersonClientDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PaymentMethod;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalPerson;
import com.portal.service.IPersonService;
import com.portal.service.IProposalPersonClientService;
import com.portal.service.IProposalService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalPersonClientService implements IProposalPersonClientService {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private IProposalService proposalService;
	
	@Autowired
	private IProposalPersonClientDAO dao; 
	
	@Autowired
	private IPersonService personService;
	
	@Override
	public void save(Integer proposalId, ProposalPerson proposalPerson, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			
			//TODO PRECISA SER AUDITADO

			Optional<Person> savedPerson = this.personService.saveOrUpdate(proposalPerson.getPerson(), userProfile);
			if(savedPerson.isPresent()) {
				proposalPerson.getPerson().setId(savedPerson.get().getId());
			}
			
			// REGRA: PPS-PER-I1, PPS-PER-U1, PPS-PER-I3, PPS-PER-U3
			if( proposalId == null || proposalId.equals( 0 ) || proposalPerson.getPerson().getId() == null || proposalPerson.getPerson().getId().equals( 0 ) ) {
				throw new BusException( "Não é possível salvar o relacionamento entre proposta e pessoa com o ID de proposta e/ou pessoa está inválido." );
			}
			
			// REGRA: PPS-PER-I4, PPS-PER-U4
			Optional<Person> personDB = this.personService.getById( proposalPerson.getPerson().getId() );
			if( !personDB.isPresent() ) {
				throw new BusException( "Não é possível salvar o relacionamento entre proposta e pessoa com a pessoa inexistente." );
			}
			
			// REGRA: PPS-PER-I2, PPS-PER-U2
			Optional<Proposal> proposalDB = this.proposalService.getById( proposalId );
			if( !proposalDB.isPresent() ) {
				throw new BusException( "Não é possível salvar o relacionamento entre proposta e pessoa com a proposta inexistente." );
			}

			this.dao.save( proposalId, proposalPerson );
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo salvar o relacionamento entre proposta e pessoa.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}		
	}

	@Override
	public void delete(Integer proposalId, Integer personId) throws AppException, BusException {
		if( proposalId == null || proposalId.equals( 0 ) || personId == null || personId.equals( 0 ) ) {
			throw new BusException( "Não é possível excluir o relacionamento entre proposta e pessoa com o ID de proposta e/ou pessoa está inválido." );
		}
		
		this.dao.delete(proposalId, personId);
	}

	@Override
	public void deleteByProposal(Integer proposalId) throws AppException, BusException {
		if( proposalId == null || proposalId.equals( 0 ) ) {
			throw new BusException( "Não é possível excluir o relacionamento entre proposta e pessoa com o ID de proposta está inválido." );
		}
		
		this.dao.deleteByProposal(proposalId);
	}

	@Override
	public Optional<Person> getPerson(Integer proposalId, Integer personId) throws AppException, BusException {
		if( personId == null || personId.equals( 0 ) || proposalId == null || proposalId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre proposta e pessoa com o ID da proposta e/ou da pessoa inválido." );
		}
		
		return this.dao.getPerson( proposalId, personId);
	}

	@Override
	public Optional<Proposal> getProposal(Integer proposalId, Integer personId) throws AppException, BusException {
		if( personId == null || personId.equals( 0 ) || proposalId == null || proposalId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre proposta e pessoa com o ID da proposta e/ou da pessoa inválido." );
		}
		
		return this.dao.getProposal( proposalId, personId);
	}

	@Override
	public List<ProposalPerson> findByProposal(Integer proposalId) throws AppException, BusException {
		if( proposalId == null || proposalId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre proposta e pessoa com o ID da proposta inválida." );
		}
		
		List<ProposalPerson> proposalPersons = this.dao.findByProposal(proposalId);
		
		if(proposalPersons.size() > 0) {
			
			proposalPersons.forEach(proposalPerson -> {
				
				try {
					
					proposalPerson.setPerson(this.personService.getById(proposalPerson.getPerson().getId()).get());
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
		}
		
		return proposalPersons;
	}
	
	@Override
	public List<Proposal> findByPerson(Integer personId) throws AppException, BusException {
		if( personId == null || personId.equals( 0 ) ) {
			throw new BusException( "Não é possível buscar o relacionamento entre proposta e pessoa com o ID da pessoa inválida." );
		}
		
		return this.dao.findByPerson(personId);
	}

	@Override
	public void update(Integer proposalId, ProposalPerson proposalPerson, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			//TODO PRECISA SER AUDITADO
			
			// REGRA: PPS-PER-I1, PPS-PER-U1, PPS-PER-I3, PPS-PER-U3
			if( proposalId == null || proposalId.equals( 0 ) || proposalPerson.getPerson().getId() == null || proposalPerson.getPerson().getId().equals( 0 ) ) {
				throw new BusException( "Não é possível salvar o relacionamento entre proposta e pessoa com o ID de proposta e/ou pessoa está inválido." );
			}
			
			// REGRA: PPS-PER-I4, PPS-PER-U4
			Optional<Person> personDB = this.personService.getById( proposalPerson.getPerson().getId() );
			if( !personDB.isPresent() ) {
				throw new BusException( "Não é possível salvar o relacionamento entre proposta e pessoa com a pessoa inexistente." );
			}
			
			// REGRA: PPS-PER-I2, PPS-PER-U2
			Optional<Proposal> proposalDB = this.proposalService.getById( proposalId );
			if( !proposalDB.isPresent() ) {
				throw new BusException( "Não é possível salvar o relacionamento entre proposta e pessoa com a proposta inexistente." );
			}

			this.dao.update( proposalId, proposalPerson );
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo salvar o relacionamento entre proposta e pessoa.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
}
