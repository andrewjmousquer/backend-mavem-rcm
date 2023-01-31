package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.ILeadFollowUpDAO;
import com.portal.dto.LeadFollowUpDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Lead;
import com.portal.model.LeadFollowUp;
import com.portal.service.IAuditService;
import com.portal.service.ILeadFollowUpService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class LeadFollowUpService implements ILeadFollowUpService {
	
	@Autowired
    private Validator validator;
	
	@Autowired
	private ILeadFollowUpDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "lfp_id");

	/**
	 * Busca leads followups que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(LeadFollowUp, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "lfp_id")
	 * 
	 * @param model objeto leadFollowUp para ser buscado
	 */
	@Override
	public Optional<LeadFollowUp> find(LeadFollowUp model) throws AppException, BusException {
		List<LeadFollowUp> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca um lead followup pelo seu ID
	 * 
	 * @param id ID de lead
	 */
	@Override
	public Optional<LeadFollowUp> getById(Integer id) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}

			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao consultar um lead followup pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<LeadFollowUp> list() throws AppException, BusException {
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public List<LeadFollowUp> search(LeadFollowUp model) throws AppException, BusException {
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Optional<LeadFollowUp> saveOrUpdate(LeadFollowUp model, UserProfileDTO userProfile)
			throws AppException, BusException {
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Optional<LeadFollowUp> save(LeadFollowUpDTO dto, UserProfileDTO userProfile)
			throws AppException, BusException {
		LeadFollowUp model = LeadFollowUp.toEntity(dto);
		
		this.validateEntity(model, OnSave.class);
		
		Optional<LeadFollowUp> saved = this.dao.save(model);
		this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.LEAD_FOLLOWUP_INSERTED, userProfile);
		
		return saved;
	}

	@Override
	public Optional<LeadFollowUp> update(LeadFollowUp model, UserProfileDTO userProfile)
			throws AppException, BusException {
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}

			Optional<LeadFollowUp> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O Lead Follow Up a ser excluído não existe.");
			}

			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.LEAD_FOLLOWUP_DELETED, userProfile);

			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão de lead.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
		
	}

	@Override
	public void audit(LeadFollowUp model, AuditOperationType operationType, UserProfileDTO userProfile)
			throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}

	
	/**
	 * Busca os leads followups que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto leadFollowUp para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "lfp_id");
	 */
	@Override
	public List<LeadFollowUp> find(LeadFollowUp model, Pageable pageable) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar os leads.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca os followups de um lead pelo seu ID
	 * 
	 * @param id ID de lead
	 */
	@Override
	public List<LeadFollowUp> findByLeadId(Integer leadId) throws NoSuchMessageException, AppException, BusException {		
		if( leadId == null ) {
			throw new BusException( "ID de busca inválido." );
		}
		
		try {
			return dao.findByLeadId(leadId);
		} catch (Exception e) {
			log.error( "Erro ao consultar followups pelo ID do lead: {}", leadId, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public Optional<LeadFollowUp> save(LeadFollowUp model, UserProfileDTO userProfile)
			throws AppException, BusException {
		throw new NotImplementedException("Not implemented");
	}
	
	/**
	 * Atualiza um lead followUp
	 * 
	 * @param model objeto lead followUp que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<LeadFollowUp> update(LeadFollowUpDTO dto, UserProfileDTO userProfile)
			throws AppException, BusException {
		try {
			LeadFollowUp model = LeadFollowUp.toEntity(dto);

			this.validateEntity(model, OnUpdate.class);

			Optional<LeadFollowUp> modelDB = this.getById(model.getId());
			if (!modelDB.isPresent()) {
				throw new BusException("O Lead Follow Up a ser atualizado não existe.");
			}

			Optional<LeadFollowUp> saved = this.dao.update(model);

			this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.LEAD_FOLLOWUP_UPDATED, userProfile);

			return saved;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error("Erro no processo de atualização de Lead FollowUp: {}", e);
			throw new AppException(this.messageSource.getMessage("error.generic.update",
					new Object[] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 *	 
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( LeadFollowUp model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
		
		if( model.getLead() == null ||  model.getLead().getId() == null ) {
			throw new BusException( "Não é permitido salvar o Lead Follow Up sem definir o Lead." );
		}
	}
	
}
