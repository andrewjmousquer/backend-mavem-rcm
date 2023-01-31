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
import com.portal.dao.ISourceDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Person;
import com.portal.model.Source;
import com.portal.service.IAuditService;
import com.portal.service.ISourceService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SourceService implements ISourceService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private ISourceDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id"); 

	/**
	 * Lista todos as fontes.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id");
	 */
	@Override
	public List<Source> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar as fontes.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Source.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<Source> saveOrUpdate(Source model, UserProfileDTO userProfile) throws AppException, BusException {
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
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Source> save(Source model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			
			Optional<Source> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.SOURCE_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro da fonte: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Source.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza uma fonte
	 * 
	 * @param model objeto canal que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Source> update(Source model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			
			Optional<Source> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "A fonte a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate(model);

			Optional<Source> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.SOURCE_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de atualização da fonte: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Source.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca as fontes que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id");
	 */
	@Override
	public List<Source> find( Source model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as fontes.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Source.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca as fontes que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id");
	 */
	@Override
	public List<Source> search( Source model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar as fontes.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Source.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Source, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id")
	 * 
	 * @param model objeto canal para ser buscado
	 */
	@Override
	public Optional<Source> find(Source model) throws AppException, BusException {
		List<Source> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Source, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id")
	 * 
	 * @param model objeto canal para ser buscado
	 */
	@Override
	public List<Source> search(Source model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma fonte pelo seu ID
	 * 
	 * @param id ID da fonte
	 */
	@Override
	public Optional<Source> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar uma fonte pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Source.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos as fontes.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id");
	 */
	@Override
	public List<Source> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de uma fonte
	 * 
	 * @param id ID da fonte
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Source> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A fonte a ser excluída não existe.");
			}
			
			// Regra: SRC-D1
			this.validateLeadRelationship(id);

			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.SOURCE_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão da fonte.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Source.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(Source model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: SRC-I3, SRC-U3, 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( Source model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}

		Source rnSearch = Source.builder()
									.name( model.getName() )
									.build();

		List<Source> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe uma fonte com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe uma fonte com o mesmo nome.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: SRC-I1, SRC-I2, SRC-U1, SRC-U2
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Source model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	/**
	 * Valida se existe algum relacionamento com parceiro.
	 *  
	 * REGRA: SRC-D1
	 *  
	 * @param srcId	ID da fonte que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validateLeadRelationship(Integer srcId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( srcId != null ) {
				boolean exists = this.dao.hasLeadRelationship( srcId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a fonte pois existe um relacionamento com lead." );
				}
				
			} else {
				throw new BusException( "ID da fonte inválido para checar o relacionamento com lead." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre fonte e lead.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
