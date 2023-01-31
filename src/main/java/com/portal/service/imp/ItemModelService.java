package com.portal.service.imp;

import java.util.List;
import java.util.Objects;
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
import com.portal.dao.IItemModelDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.Model;
import com.portal.model.Person;
import com.portal.service.IAuditService;
import com.portal.service.IItemModelService;
import com.portal.service.IItemService;
import com.portal.service.IModelService;
import com.portal.validators.ValidationHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ItemModelService implements IItemModelService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IItemModelDAO dao;
	
	@Autowired
	private IModelService modelService;
	
	@Autowired
	private IItemService itemService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "imd_id"); 

	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ItemModel> saveOrUpdate(ItemModel model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<ItemModel> save(ItemModel model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			//this.validateEntity(model, OnSave.class);
			this.validateItemEntity( model );
			this.validateModelEntity(model);
			this.validateHasDuplicate(model);
			
			Optional<ItemModel> saved = this.dao.save(model);
			
			//this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.ITEM_MODEL_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			log.error( "Erro no processo de cadastro do relacionamento entre item e modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do relacionamento entre item e modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um item
	 * 
	 * @param model objeto produto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ItemModel> update(ItemModel model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			//this.validateEntity(model, OnUpdate.class);
			
			// IMD-U5
			Optional<ItemModel> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O modelo do item a ser atualizado não existe.");
			}
			
			this.validateItemEntity( model );
			this.validateModelEntity(model);
			this.validateHasDuplicate(model);

			Optional<ItemModel> saved = this.dao.update(model);
			
			//this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.ITEM_MODEL_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do relacionamento entre item e modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { ItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca itens que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto produtos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "imd_id");
	 */
	@Override
	public List<ItemModel> find( ItemModel model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar o relacionamento entre item e modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { ItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são bmodeluscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(ItemModel, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "imd_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public Optional<ItemModel> find(ItemModel model) throws AppException, BusException {
		List<ItemModel> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ItemModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	public List<ItemModel> search(ItemModel model) throws AppException, BusException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}
	
	/**
	 * Busca um produto pelo seu ID
	 * 
	 * @param id ID do produto
	 */
	@Override
	public Optional<ItemModel> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o relacionamento entre item e nodelo pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { ItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ItemModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ItemModel> list() throws AppException, BusException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

	/**
	 * Efetua a exclusão de um produto
	 * 
	 * @param id ID do produto
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<ItemModel> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O modelo do item a ser excluído não existe.");
			}

			// REGRA: IMD-D1
			this.validatePriceListRelationship( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.ITEM_MODEL_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do relacionamento entre item e modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	@Override
	public void audit(ItemModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: IMD-I6,IMD-U6 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( ItemModel model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		ItemModel rnSearch = ItemModel.builder()
											.item( model.getItem() )
											.model( model.getModel() )
											.modelYearStart( model.getModelYearStart() )
											.modelYearEnd( model.getModelYearEnd() )
											.build();

		List<ItemModel> listBD  = this.dao.findDuplicated( rnSearch );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() &&
				listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
			throw new BusException( "Já existe um relacionamento com esse item e modelo, dentro do mesmo range de ano/modelo.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) &&  listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um relacionamento com esse item e modelo, dentro do mesmo range de ano/modelo.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: IMD-I1,IMD-I2,IMD-I3,IMD-U1,IMD-U2,IMD-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( ItemModel model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
		
		if( model.getModelYearEnd().equals(0) || model.getModelYearStart().equals(0) ) {
			throw new BusException( "O ano inicio ou fim do modelo não pode ser zero." );
		}
	}

	/**
	 * Valida se existe algum relacionamento com lista de preço.
	 *  
	 * REGRA: IMD-D1
	 *  
	 * @param prmId	ID do relacionamento de item e modelo
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validatePriceListRelationship( Integer prmId ) throws BusException, NoSuchMessageException, AppException {
		try {
			if( prmId != null ) {
				boolean exists = this.dao.hasPriceListRelationship( prmId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o relacionamento de item com modelo pois existe um relacionamento com lista de preço." );
				}
				
			} else {
				throw new BusException( "ID do relacionamento entre item e modelo inválido para checar o relacionamento com lista de preço." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar o relacionamento entre produto e lista de preço.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Valida se a entidade item é válida
	 * 
	 * REGRAS: IMD-I5,IMD-U6
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateItemEntity(ItemModel model) throws BusException, AppException {
		try {
			if( model != null  ) {
				// REGRA: 
				if( model.getItem() == null || model.getItem().getId() == null || model.getItem().getId().equals(0) ) {
					throw new BusException( "Não é possível salvar o relacionamento pois o item relacionado é inválido ou não existe." );
				} else {
					Optional<Item> dbItem = this.itemService.getById( model.getItem().getId() );
					if( !dbItem.isPresent() ) {
						throw new BusException( "Não é possível salvar o relacionamento pois o item relacionado é inválido ou não existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade item relacionadas ao modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se a entidade modelo é válida
	 * 
	 * REGRAS: IMD-I6,IMD-U7
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateModelEntity(ItemModel model) throws BusException, AppException {
		try {
			if( model != null  ) {
				// REGRA: 
				if( model.getModel() == null || model.getModel().getId() == null || model.getModel().getId().equals(0) ) {
					throw new BusException( "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe." );
				} else {
					Optional<Model> dbModel = this.modelService.getById( model.getModel().getId() );
					if( !dbModel.isPresent() ) {
						throw new BusException( "Não é possível salvar o relacionamento pois o modelo relacionado é inválido ou não existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade modelo relacionadas ao item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	
}
