package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.ILeadDAO;
import com.portal.dao.ILeadFollowUpDAO;
import com.portal.dto.LeadDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.enums.LeadEvents;
import com.portal.enums.LeadState;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.exceptions.StateWorkflowException;
import com.portal.model.Lead;
import com.portal.model.Person;
import com.portal.service.IAuditService;
import com.portal.service.ILeadService;
import com.portal.service.IPersonService;
import com.portal.utils.LeadStateBuilder;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class LeadService implements ILeadService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private ILeadDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;

	@Autowired
	private LeadStateBuilder leadStateBuilder;

	@Autowired
	private IPersonService personService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private ILeadFollowUpDAO followUpDAO;

	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id");


	/**
	 * Lista todos os leads.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id");
	 */
	@Override
	public List<Lead> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os leads.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Lead> saveOrUpdate(Lead model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}

	@Override
	public Optional<Lead> save(Lead model, UserProfileDTO userProfile) throws AppException, BusException {
		return Optional.empty();
	}

	@Override
	public Optional<Lead> update(Lead model, UserProfileDTO userProfile) throws AppException, BusException {
		return Optional.empty();
	}

	/**
	 * Salva um novo objeto.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Lead> save(LeadDTO dto, UserProfileDTO userProfile) throws AppException, BusException {
		try {			
			Lead model = Lead.toEntity(dto);

			if(dto.getClient() != null) {
				this.personService.getById(dto.getClient().getId()).ifPresent(client -> {
					model.setClient(client);
				});			
			}
			if(dto.getSeller() != null) {
				this.personService.getById(dto.getSeller().getId()).ifPresent(seller -> {
					model.setClient(seller);
				});
			}
			
			this.validateEntity(model, OnSave.class);
			
			/**
			 * TODO
			 * Possivel  validação para alteração de STATUS em aberto apos validar o LEAD como NOVO
			 */
//			Optional<Lead> changedLead = this.changeStatus(model, LeadEvents.OPEN);	
//			if( !changedLead.isPresent() ) {
//				throw new BusException( "Objeto que será salvo está inválido." );
//			}
			
			Optional<Lead> saved = this.dao.save(model);
			//Optional<Lead> saved = this.dao.save(changedLead.get());
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.LEAD_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro de lead: {}",  e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um lead
	 * 
	 * @param model objeto lead que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Lead> update(LeadDTO dto, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			
			Lead model = Lead.toEntity(dto) ;
			
			if(dto.getClient() != null) {
				this.personService.getById(dto.getClient().getId()).ifPresent(client -> {
					model.setClient(client);
				});			
			}
			if(dto.getSeller() != null) {
				this.personService.getById(dto.getSeller().getId()).ifPresent(seller -> {
					model.setClient(seller);
				});
			}
			
			this.validateEntity(model, OnUpdate.class);
			
			Optional<Lead> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O lead a ser atualizado não existe.");
			}
			
			
			/**
			 * TODO
			 * Possivel  validação de alteração de STATUS via STATEMACHINE 
			 */
			// Criamos outro SM somente para validar se a transição de um status para o outro é permitido
			// Essa validação extra é para que não ocorra uma falha no fuxo do DEV onde chama o UPDATE antes de trocar o status via state machine.
			//StateMachine<LeadState, LeadEvents> smTemp = leadStateBuilder.buildNewStateMachine();
			//boolean validTransitition = LeadStateBuilder.isStateTransitionValid(smTemp, LeadState.getById(modelDB.get().getStatus().getId()), LeadState.getById(model.getStatus().getId()));
			//if( !validTransitition ) {
			//	throw new BusException( "A troca de status entre " + model.getStatus() + " e " + modelDB.get().getStatus() );
			//}
			
			Optional<Lead> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.LEAD_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de atualização de lead: {}",  e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * A operação de troca de status foi separada do UPDATE para que seja possível manter desacoplado os dois eventos.
	 * Isso não evita que no UPDATE seja validado a entidade.
	 */
	@Override
	public Optional<Lead> changeStatus( Lead model, LeadEvents event ) throws AppException, BusException, StateWorkflowException {
		
		if( model == null ) {
			throw new BusException( "Não é possível alterar o status com o objeto lead inválido." );
		}
		
		if( event == null ) {
			throw new BusException( "Não é possível altarar o status do lead com o evento inválido." );
		}
		
		StateMachine<LeadState, LeadEvents> sm = null;
		
		if( model.getId() != null && !model.getId().equals(0)) {
			Optional<Lead> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "Não foi possível encontrar o lead com ID " + model.getId() + " para atualizar o status.");
			}
			
			sm = leadStateBuilder.recoveryStateMachine( modelDB.get().getId().toString(), LeadState.getById(modelDB.get().getStatus().getId()) );
		
		} else {
			sm = leadStateBuilder.recoveryStateMachine( null, LeadState.OPENED );
		}
		
		if( sm != null ) {
			// Customizamos o evento para enviar o ID do lead junto
			Message<LeadEvents> msg = MessageBuilder.withPayload( event )
											.setHeader( LeadStateBuilder.LEAD_INSTANCE, model)
											.build();
			
			boolean eventFlag = sm.sendEvent( msg );
						
			if( sm.hasStateMachineError() ) {
				throw (StateWorkflowException) sm.getExtendedState().get("stateException", StateWorkflowException.class);
			}
			if( !eventFlag ) {
				throw new StateWorkflowException( MessageFormatter.format( "Fluxo inválido não é possível executar a ação de transição de {} do status {}.", msg.getPayload(), sm.getState().getId() ).getMessage() );
			}
			
			// Seta o novo status
			model.setStatus( sm.getState().getId().getType() );
			
			return Optional.of(model);
		}
		
		return Optional.empty();
	}
	
	/**
	 * Busca os leads que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto leads para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id");
	 */
	@Override
	public List<Lead> find( Lead model, Pageable pageable ) throws AppException, BusException {
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
	 * Busca os leads que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id");
	 */
	@Override
	public List<Lead> search( Lead model, Pageable pageable ) throws AppException, BusException {
		return this.find(model, pageable);
	}
	
	/**
	 * Busca leads que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Lead, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id")
	 * 
	 * @param model objeto lead para ser buscado
	 */
	@Override
	public Optional<Lead> find(Lead model) throws AppException, BusException {
		List<Lead> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca leads que respeitem os dados do objetLeadServiceo.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Lead, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id")
	 * 
	 * @param model objeto lead para ser buscado
	 */
	@Override
	public List<Lead> search(Lead model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma fonte pelo seu ID
	 * 
	 * @param id ID de lead
	 */
	@Override
	public Optional<Lead> getById(Integer id) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao consultar um lead pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos os leads.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id");
	 */
	@Override
	public List<Lead> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um lead
	 * 
	 * @param id ID de lead
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Lead> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A lead a ser excluída não existe.");
			}
			
			// Regra: LED-D1
			this.validateProposalRelationship(id);

			this.validateLeadFollowUpRelationship(id);

			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.LEAD_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão de lead.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Lead.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(Lead model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: 	LED-I1,LED-I2,LED-I3,LED-I4,LED-I5,LED-I6
	 * 			LED-U1,LED-U2,LED-U3,LED-U4,LED-U5,LED-U6
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Lead model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
		
		if( model.getModel().getBrand() == null ||  model.getModel().getBrand().getId() == null ) {
			throw new BusException( "Não é permitido salvar o lead com o modelo sem definir a marca." );
		}
	}
	
	
	/**
	 * Valida se existe algum relacionamento com parceiro.
	 *  
	 * REGRA: LED-D1
	 *  
	 * @param ledId	ID de lead que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validateProposalRelationship(Integer ledId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( ledId != null ) {
				boolean exists = this.dao.hasProposalRelationship( ledId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a fonte pois existe um relacionamento com lead." );
				}
				
			} else {
				throw new BusException( "ID de lead inválido para checar o relacionamento com lead." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre fonte e lead.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}


	/**
	 * Faz a validação da entidade para status LeadState.OPENED.
	 * 
	 * @param entity		entidade que será validada.
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	@Override
	public void validateOpenedState( Lead entity ) throws BusException, AppException {
		try {

			if( entity == null ) {
				throw new BusException( "Entidade inválida para o status " + LeadState.OPENED );
			}
			
			if( entity.getStatus() == null || !entity.getStatus().equals( LeadState.OPENED.getType() ) ) {
				throw new BusException( "O status do lead está inválido. Status: " + entity.getStatus() );
			}
			
			// TODO Definir a regra de mudança de status
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro na validação das regras para mudança de status.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Faz a validação da entidade para status LeadState.UNCONVERTED.
	 * 
	 * REGRAS: 	LED-I7,LED-I8
	 * 			LED-U7,LED-U8
	 * 
	 * @param entity		entidade que será validada.
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	@Override
	public void validateUnConvertedState( Lead entity ) throws BusException, AppException {
		try {

			if( entity == null ) {
				throw new BusException( "Entidade inválida para o status " + LeadState.UNCONVERTED );
			}
			
			if( entity.getStatus() == null || !entity.getStatus().equals( LeadState.UNCONVERTED ) ) {
				throw new BusException( "O status do lead está inválido. Status: " + entity.getStatus() );
			}
			
			// TODO Definir a regra de mudança de status
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro na validação das regras para mudança de status.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Faz a validação da entidade para status LeadState.CONVERTED.
	 * 
	 * REGRAS: 	LED-I7,LED-I8
	 * 			LED-U7,LED-U8
	 * 
	 * @param entity		entidade que será validada.
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	@Override
	public void validateConvertedState( Lead entity ) throws BusException, AppException {
		try {

			if( entity == null ) {
				throw new BusException( "Entidade inválida para o status " + LeadState.CONVERTED );
			}
			
			if( entity.getStatus() == null || !entity.getStatus().equals( LeadState.CONVERTED ) ) {
				throw new BusException( "O status do lead está inválido. Status: " + entity.getStatus() );
			}
			
			// TODO Definir a regra de mudança de status
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro na validação das regras para mudança de status.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Faz a validação da entidade para status LeadState.CANCELED.
	 * 
	 * REGRAS: 	LED-I7,LED-I8
	 * 			LED-U7,LED-U8
	 * 
	 * @param entity		entidade que será validada.
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	@Override
	public void validateCanceledState( Lead entity ) throws BusException, AppException {
		try {

			if( entity == null ) {
				throw new BusException( "Entidade inválida para o status " + LeadState.CANCELED );
			}
			
			if( entity.getStatus() == null || !entity.getStatus().equals( LeadState.CANCELED ) ) {
				throw new BusException( "O status do lead está inválido. Status: " + entity.getStatus() );
			}
			
			// TODO Definir a regra de mudança de status
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro na validação das regras para mudança de status.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Faz a validação da entidade para status LeadState.CONTACTED.
	 * 
	 * @param entity		entidade que será validada.
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	@Override
	public void validateContactedState(Lead entity) throws BusException, AppException {
		try {

			if( entity == null ) {
				throw new BusException( "Entidade inválida para o status " + LeadState.CONTACTED );
			}
			
			if( entity.getStatus() == null || !entity.getStatus().equals( LeadState.CONTACTED ) ) {
				throw new BusException( "O status do lead está inválido. Status: " + entity.getStatus() );
			}
			
			// TODO Definir a regra de mudança de status
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro na validação das regras para mudança de status.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	private void validateLeadFollowUpRelationship(Integer id) throws AppException, BusException {

		try {
			int fups = followUpDAO.findByLeadId(id).size();
			if(fups == 1) {
				throw new BusException( "Não é possível excluir o lead, pois existe um Follow Up relacionado" );
			} else if(fups > 1) {
				throw new BusException( String.format("Não é possível excluir o lead, pois existem %d Follow Ups relacionados", fups) );
			}
		} catch (BusException e) {
			throw e;
		} catch (AppException e) {
			log.error( "Erro ao carregar o relacionamento entre lead followup e lead.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}

	}
}
