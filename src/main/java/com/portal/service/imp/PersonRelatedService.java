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
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
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
	 * M??todo auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se n??o tiver ID ?? save, caso contr??rio ?? update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
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
	 * @param profile dados do usu??rio logado.
	 */
	@Override
	public Optional<PersonRelated> save(PersonRelated model, UserProfileDTO profile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			this.validatePerson(model);
			
			Optional<PersonRelated> saved = this.dao.save(model);

			if( !saved.isPresent() ) {
				throw new BusException( "N??o houve retorno ao salvar o relacionamento entre pessoas, n??o ?? poss??vel seguir." );
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
	 * @param profile dados do usu??rio logado.
	 */
	@Override
	public Optional<PersonRelated> update(PersonRelated model, UserProfileDTO profile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			this.validatePerson(model);
			
			Optional<PersonRelated> modelDB = this.getById( model.getId(), false );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O relacionamento de pessoa a ser atualizado n??o existe.");
			}
			
			this.validateHasDuplicate(model);

			this.dao.update(model);

			this.audit( model, AuditOperationType.PERSON_RELATED_UPDATED, profile);
			
			return Optional.ofNullable(model);

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualiza????o do relacionamento entre pessoas: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PersonRelated.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca relacionamento de pessoa que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * @param model objeto relacionamento de pessoa para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
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
	 * Aqui os campos String s??o buscados com o 'LIKE'
	 * 
	 * @param model objeto relacionamento de pessoa para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
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
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(PersonRelated, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id")
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
	 * Aqui os campos String s??o buscados com o 'LIKE'
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(PersonRelated, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id")
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
				throw new BusException( "ID de busca inv??lido." );
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
	 * Esse m??todo ?? uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PersonRelated> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclus??o do registro
	 * 
	 * @param id ID do relacionamento entre pessoas
	 * @param userProfile dados do usu??rio logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de exclus??o inv??lido." );
			}
			
			Optional<PersonRelated> entityDB = this.getById(id, false);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O relacionamento a ser exclu??do n??o existe.");
			}

			this.dao.delete( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PERSON_RELATED_DELETED, userProfile);
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o do relacionamento entre pessoas.", e );
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
			throw new BusException( "N??o ?? poss??vel chegar a duplicidade com o objeto da entidade nula." );
		}
		
		PersonRelated rnSearch = PersonRelated.builder()
												.name( model.getName() )
												.relatedType( model.getRelatedType() )
												.person( model.getPerson() )
												.build();

		List<PersonRelated> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "J?? existe esse relacionamento entre pessoas.");
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
				throw new BusException( "J?? existe esse relacionamento entre pessoas.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formata????o e obrigatoriedade
	 * 
	 * Regra: PSR-I1,PSR-I2,PSR-I3
	 * 		  PSR-U1,PSR-U2,PSR-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de valida????o que ser?? usado
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
				throw new AppException( "N??o ?? poss??vel executar a valida????o da pessoa pois a entidade est?? nula ou inv??lida." );
			}
			
			if( model.getPerson() != null && model.getPerson().getId() != null ) {
				Optional<Person> personDB = this.personService.getById( model.getPerson().getId() );

				if( !personDB.isPresent() ) {
					throw new BusException( "N??o ?? poss??vel salvar o relacionamento com a pessoa relacionada n??o existente." );
				}
			}

			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao executar a valida????o da pessoal relacionada.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
