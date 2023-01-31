package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.IProposalCommissionDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PartnerPerson;
import com.portal.model.ProposalCommission;
import com.portal.service.IAuditService;
import com.portal.service.IBankAccountService;
import com.portal.service.IPartnerPersonService;
import com.portal.service.IPersonService;
import com.portal.service.IProposalCommissionService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProposalCommissionService implements IProposalCommissionService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IProposalCommissionDAO dao;
	
	@Autowired 
	private IPersonService personService;
	
	@Autowired
	private IBankAccountService bankAccountService;
	
	@Autowired
	private IPartnerPersonService partnerPersonService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");

	@Override
	public List<ProposalCommission> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar comissao da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { ProposalCommission.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public Optional<ProposalCommission> saveOrUpdate(ProposalCommission model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	@Override
	public Optional<ProposalCommission> save( ProposalCommission model, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate( model );
			
			Optional<ProposalCommission> saved = this.dao.save( model );
			
			//this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_COMMISSION_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro da comissao da proposta: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ProposalCommission.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public Optional<ProposalCommission> update(ProposalCommission model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			//this.validateHasDuplicate( model );
			
			Optional<ProposalCommission> saved = this.dao.update(model);
			
			//this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PROPOSAL_COMMISSION_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de atualização da comissao da proposta: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { ProposalCommission.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<ProposalCommission> find( ProposalCommission model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<ProposalCommission> proposalCommissions = this.dao.find( model, pageable ); 
			
			proposalCommissions.forEach(commission -> {
				
				try {
					
					commission.setPerson(this.personService.getById(commission.getPerson().getId()).get());
					commission.setBankAccount(this.bankAccountService.getById(commission.getBankAccount().getId()).get());
					
					PartnerPerson partnerPerson = new PartnerPerson(commission.getPerson());
					/*
					if(model.getProposalDetail().getPartner() != null) {
						partnerPerson.setPartner(model.getProposalDetail().getPartner());
					}
					*/
					if(partnerPerson != null) {
						
						Optional<PartnerPerson> partnerPersonRet = this.partnerPersonService.getPartnerPerson(partnerPerson);
						
						if(partnerPersonRet != null) {
							if(partnerPersonRet.isPresent()) {
								commission.setPartnerPerson(partnerPersonRet.get());
							}
						}
					}
					
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
			
			return proposalCommissions;
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar comissao da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { ProposalCommission.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<ProposalCommission> search( ProposalCommission model, Pageable pageable ) throws AppException, BusException {
		return this.find(model, pageable);
	}
	
	@Override
	public Optional<ProposalCommission> find(ProposalCommission model) throws AppException, BusException {
		List<ProposalCommission> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	@Override
	public List<ProposalCommission> search(ProposalCommission model) throws AppException, BusException {
		return this.search( model, null );
	}

	@Override
	public Optional<ProposalCommission> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a comissao da proposta pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { ProposalCommission.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<ProposalCommission> list() throws AppException, BusException {
		return this.listAll( null );
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}

			//Optional<ProposalCommission> entityDB = this.getById(id);
			
			//this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PROPOSAL_COMMISSION_DELETED, userProfile);
			
			this.dao.delete( id );


		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão da comissao da proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ProposalCommission.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(ProposalCommission model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	private void validateEntity( ProposalCommission model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	private void validateHasDuplicate( ProposalCommission model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível executar a validação de duplicado pois a comissao da proposta está nula ou inválida." );
		}
		
		ProposalCommission search = ProposalCommission.builder()
										.proposalDetail( model.getProposalDetail() )
										.person( model.getPerson() )
										.commissionType( model.getCommissionType() )
										.build();

		List<ProposalCommission> commission = this.find(search, DEFAULT_PAGINATION);

		if( commission.size() > 0 ) {
			throw new BusException( "Já existe comissão igual para a proposta.");
		}
	}
}
