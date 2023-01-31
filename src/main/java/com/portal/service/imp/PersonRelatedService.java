package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

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
import com.portal.dao.IPersonRelatedDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Person;
import com.portal.model.PersonRelated;
import com.portal.service.IAuditService;
import com.portal.service.IPersonRelatedService;
import com.portal.service.IPersonService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PersonRelatedService implements IPersonRelatedService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPersonRelatedDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private IPersonService personService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "psr_id"); 

	/**
	 * Lista todos o relacionamento entre pessoas.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PersonRelated> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll(pageable);
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar o relacionamento entre pessoas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { PersonRelated.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<PersonRelated> saveOrUpdate(PersonRelated model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	/**
	 * Salva um novo objeto.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param profile dados do usuário logado.
	 */
	@Override
	public Optional<PersonRelated> save(PersonRelated model, UserProfileDTO profile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			this.validatePerson(model);
			
			Optional<PersonRelated> saved = this.dao.save(model);

			if( !saved.isPresent() ) {
				throw new BusException( "Não houve retorno ao salvar o relacionamento entre pessoas, não é possível seguir." );
			}
			
			this.audit( saved.get(), AuditOperationType.PERSON_RELATED_INSERTED, profile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do relacionamento entre pessoas: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PersonRelated.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um relacionamento de pessoa
	 * 
	 * @param model objeto relacionamento de pessoa que deve ser salvo.
	 * @param profile dados do usuário logado.
	 */
	@Override
	public Optional<PersonRelated> update(PersonRelated model, UserProfileDTO profile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			this.validatePerson(model);
			
			Optional<PersonRelated> modelDB = this.getById( model.getId(), false );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O relacionamento de pessoa a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate(model);

			this.dao.update(model);

			this.audit( model, AuditOperationType.PERSON_RELATED_UPDATED, profile);
			
			return Optional.ofNullable(model);

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do relacionamento entre pessoas: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PersonRelated.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca relacionamento de pessoa que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto relacionamento de pessoa para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PersonRelated> find( PersonRelated model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );

		} catch (Exception e) {
			log.error( "Erro no processo de buscar o relacionamento entre pessoas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { PersonRelated.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca relacionamento de pessoa que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto relacionamento de pessoa para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PersonRelated> search( PersonRelated model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar o relacionamento entre pessoas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { PersonRelated.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca relacionamento de pessoa que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(PersonRelated, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id")
	 * 
	 * @param model objeto relacionamento de pessoa para ser buscado
	 */
	@Override
	public Optional<PersonRelated> find(PersonRelated model) throws AppException, BusException {
		List<PersonRelated> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca relacionamento de pessoa que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(PersonRelated, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id")
	 * 
	 * @param model objeto relacionamento de pessoa para ser buscado
	 */
	@Override
	public List<PersonRelated> search(PersonRelated model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	@Override
	public Optional<PersonRelated> getById(Integer id, boolean withRule) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			
			return this.dao.getById(id);

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o relacionamento entre pessoas pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PersonRelated.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<PersonRelated> findByPerson(Integer id) throws AppException {
		return this.dao.findByPerson(id);
	}

	/**
	 * Busca um relacionamento de pessoa pelo seu ID
	 * 
	 * @param id ID do relacionamento entre pessoas
	 */
	@Override
	public Optional<PersonRelated> getById(Integer id) throws AppException, BusException {
		return this.getById(id, true);
	}

	/**
	 * Lista todos o relacionamento entre pessoas.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PersonRelated> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão do registro
	 * 
	 * @param id ID do relacionamento entre pessoas
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<PersonRelated> entityDB = this.getById(id, false);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O relacionamento a ser excluído não existe.");
			}

			this.dao.delete( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PERSON_RELATED_DELETED, userProfile);
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do relacionamento entre pessoas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PersonRelated.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	@Override
	public void audit(PersonRelated model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PSR-I5, PSR-U5 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( PersonRelated model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		PersonRelated rnSearch = PersonRelated.builder()
												.name( model.getName() )
												.relatedType( model.getRelatedType() )
												.person( model.getPerson() )
												.build();

		List<PersonRelated> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe esse relacionamento entre pessoas.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && 
													( item.getName().equals( model.getName() ) &&
													  item.getPerson().equals( model.getPerson() ) &&
													  item.getRelatedType().equals( model.getRelatedType() )
													) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe esse relacionamento entre pessoas.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PSR-I1,PSR-I2,PSR-I3
	 * 		  PSR-U1,PSR-U2,PSR-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PersonRelated model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	/**
	 * Valida se a pessoa relacionada existe,
	 * 
	 * Regra: PSR-I4
	 * 
	 * @param model
	 * @throws BusException 
	 * @throws AppException 
	 * @throws NoSuchMessageException 
	 */
	private void validatePerson(PersonRelated model) throws BusException, AppException {
		try {
			if( model == null ) {
				throw new AppException( "Não é possível executar a validação da pessoa pois a entidade está nula ou inválida." );
			}
			
			if( model.getPerson() != null && model.getPerson().getId() != null ) {
				Optional<Person> personDB = this.personService.getById( model.getPerson().getId() );

				if( !personDB.isPresent() ) {
					throw new BusException( "Não é possível salvar o relacionamento com a pessoa relacionada não existente." );
				}
			}

			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao executar a validação da pessoal relacionada.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
