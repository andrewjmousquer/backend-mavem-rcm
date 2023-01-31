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
import com.portal.dao.IItemTypeDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.ItemType;
import com.portal.service.IAuditService;
import com.portal.service.IItemTypeService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ItemTypeService implements IItemTypeService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IItemTypeDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id"); 

	/**
	 * Lista todos os tipos de itens.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id");
	 */
	@Override
	public List<ItemType> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {
			
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os tipos de itens.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { ItemType.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<ItemType> saveOrUpdate(ItemType model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<ItemType> save(ItemType model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {

			this.validateEntity(model, OnSave.class);
			
			this.validateHasDuplicate(model);
			
			Optional<ItemType> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.ITEM_TYPE_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do tipo de item: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ItemType.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um tipo de item
	 * 
	 * @param model objeto tipo de item que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ItemType> update(ItemType model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity(model, OnUpdate.class);
			
			// ITT-U6
			Optional<ItemType> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O tipo de item a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate(model);

			Optional<ItemType> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.ITEM_TYPE_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do tipo de item: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { ItemType.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca tipos de itens que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto tipos de itens para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id");
	 */
	@Override
	public List<ItemType> find( ItemType model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar os tipos de itens.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { ItemType.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca tipos de itens que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto tipos de itens para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id");
	 */
	@Override
	public List<ItemType> search( ItemType model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar os tipos de itens.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { ItemType.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca tipos de itens que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(ItemType, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id")
	 * 
	 * @param model objeto tipo de item para ser buscado
	 */
	@Override
	public Optional<ItemType> find(ItemType model) throws AppException, BusException {
		List<ItemType> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca tipos de itens que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(ItemType, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id")
	 * 
	 * @param model objeto tipo de item para ser buscado
	 */
	@Override
	public List<ItemType> search(ItemType model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca um tipo de item pelo seu ID
	 * 
	 * @param id ID do tipo de item
	 */
	@Override
	public Optional<ItemType> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um tipo de item pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { ItemType.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos os tipos de itens.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id");
	 */
	@Override
	public List<ItemType> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um tipo de item
	 * 
	 * @param id ID do tipo de item
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<ItemType> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "O tipo de item a ser excluído não existe.");
			}
			
			this.validateItemRelationship(id);
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.ITEM_TYPE_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do tipo de item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ItemType.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(ItemType model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: ITT-I2, ITT-U2, 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( ItemType model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		ItemType rnSearch = ItemType.builder()
				.name( model.getName() )
				.build();

		List<ItemType> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe um tipo de item com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um tipo de item com o mesmo nome.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: ITT-I1,ITT-I3,ITT-I4,ITT-I4,ITT-U1,ITT-U3,ITT-U4,ITT-U5
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( ItemType model, Class<?> group ) throws AppException, BusException {
		try {
			ValidationHelper.generateException( validator.validate( model, group ) );
		} catch (BusException e) {
			throw new BusException("Cadastro não validado.\n" + e);
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com item.
	 *  
	 * REGRA: ITT-D1
	 *  
	 * @param itemTypeId	ID do modelo que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateItemRelationship( Integer itemTypeId ) throws AppException, BusException {
		try {
			if( itemTypeId != null ) {
				boolean exists = this.dao.hasItemRelationship( itemTypeId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o tipo de item pois existe um relacionamento com item." );
				}
				
			} else {
				throw new BusException( "ID do tipo de item inválido para checar o relacionamento com item." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre tipo de item e item. [lead]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
