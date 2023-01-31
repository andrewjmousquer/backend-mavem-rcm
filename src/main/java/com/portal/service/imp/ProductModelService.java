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
import com.portal.dao.IProductModelDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Model;
import com.portal.model.Person;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.service.IAuditService;
import com.portal.service.IModelService;
import com.portal.service.IProductModelService;
import com.portal.service.IProductService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProductModelService implements IProductModelService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IProductModelDAO dao;
	
	@Autowired
	private IModelService modelService;
	
	@Autowired
	private IProductService productService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id"); 

	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ProductModel> saveOrUpdate(ProductModel model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<ProductModel> save(ProductModel model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateModelEntity(model, userProfile);
			this.validateProductEntity(model, userProfile);
			this.validateHasDuplicate(model);

			Optional<ProductModel> saved = this.dao.save(model);

			this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.PRODUCT_MODEL_INSERTED, userProfile);

			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro  do relacionamento entre produto e modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um produto
	 * 
	 * @param model objeto produto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ProductModel> update(ProductModel model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			// PRM-U7
			Optional<ProductModel> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O produto a ser atualizado não existe.");
			}
			
			this.validateModelEntity(model, userProfile);
			this.validateProductEntity(model, userProfile);
			this.validateHasDuplicate(model);

			Optional<ProductModel> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_MODEL_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do relacionamento entre produto e modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<ProductModel> listAll(Pageable pageable) throws AppException, BusException {
		try {

			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}
			return this.dao.listAll(pageable);
		} catch (Exception e) {
			log.error("Erro no processo de listar os produtos.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{Product.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto produtos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id");
	 */
	@Override
	public List<ProductModel> find( ProductModel model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar o relacionamento entre produto e modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<ProductModel> search(ProductModel model, Pageable pageable) throws AppException, BusException {
		return null;
	}


	/**
	 * Busca produtos por id de Produto
	 */
	@Override
	public List<ProductModel> getByProduct(Integer id) throws AppException {
		try {

			return this.dao.getByProduct( id );

		} catch (Exception e) {
			log.error( "Erro no processo de buscar o relacionamento entre produto e modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	}

	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são bmodeluscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(ProductModel, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public Optional<ProductModel> find(ProductModel model) throws AppException, BusException {
		List<ProductModel> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	public List<ProductModel> search(ProductModel model) throws AppException, BusException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}
	
	/**
	 * Busca um produto pelo seu ID
	 * 
	 * @param id ID do produto
	 */
	@Override
	public Optional<ProductModel> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o relacionamento entre produto e nodelo pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ProductModel> list() throws AppException, BusException {
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
			
			Optional<ProductModel> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "O produto a ser excluído não existe.");
			}

			// REGRA: PRM-D1
			this.validatePriceListRelationship( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PRODUCT_MODEL_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do relacionamento entre produto e modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	@Override
	public void audit(ProductModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PRM-I6,PRM-U6 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( ProductModel model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		ProductModel rnSearch = ProductModel.builder()
											.product( model.getProduct() )
											.model( model.getModel() )
											.modelYearStart( model.getModelYearStart() )
											.modelYearEnd( model.getModelYearEnd() )
											.build();

		List<ProductModel> listBD  = this.dao.findDuplicated( rnSearch );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() &&
				listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
			throw new BusException( "Já existe um relacionamento com esse produto e modelo, dentro do mesmo range de ano/modelo.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) &&  listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um relacionamento com esse produto e modelo, dentro do mesmo range de ano/modelo.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PRM-I1,PRM-I2,PRM-I3,PRM-I4,PRM-I5
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( ProductModel model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
		
		if( model.getModelYearEnd().equals(0) || model.getModelYearStart().equals(0) ) {
			throw new BusException( "O ano inicio ou fim do modelo não pode ser zero." );
		}
	}

	/**
	 * Valida se existe algum relacionamento com lista de preço.
	 *  
	 * REGRA: PRM-D1
	 *  
	 * @param prmId	ID do relacionamento de produto e modelo
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validatePriceListRelationship( Integer prmId ) throws BusException, NoSuchMessageException, AppException {
		try {
			if( prmId != null ) {
				boolean exists = this.dao.hasPriceListRelationship( prmId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o relacionamento de produto com modelo pois existe um relacionamento com lista de preço." );
				}
				
			} else {
				throw new BusException( "ID do relacionamento entre produto e modelo inválido para checar o relacionamento com lista de preço." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar o relacionamento entre produto e lista de preço.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Valida se a entidade produto é válida
	 * <p>
	 * REGRAS: PRM-I7 e PRM-U8
	 *
	 * @param model entidade para ser validada
	 * @throws AppException Em caso de erro sistêmico
	 * @throws BusException Em caso de erro relacionado a regra de negócio
	 */
	private void validateProductEntity(ProductModel model, UserProfileDTO userProfile) throws BusException, AppException {
		try {
			if (model == null) {
				throw new BusException("Não é possível salvar o relacionamento pois o produto model  inválido ");
			}

			if (model.getProduct() == null) {
				throw new BusException("Não é possível salvar o relacionamento pois o produto relacionado é inválido ");
			}

			Optional<Product> dbProduct;
			if (model.getProduct().getId() != null) {

				dbProduct = this.productService.getById(model.getProduct().getId());
			} else {
				model.setProduct(this.productService.save(model.getProduct(), userProfile).get());
			}

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade produto relacionadas ao modelo de produto.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Valida se a entidade modelo é válida
	 * <p>
	 * REGRAS: PRM-I8 e PRM-U9
	 *
	 * @param model entidade para ser validada
	 * @throws AppException Em caso de erro sistêmico
	 * @throws BusException Em caso de erro relacionado a regra de negócio
	 */
	private void validateModelEntity(ProductModel model, UserProfileDTO userProfile) throws BusException, AppException {
		try {
			if (model == null) {
				throw new BusException("Não é possível salvar o relacionamento pois o produto model  inválido ");
			}

			if (model.getModel() == null) {
				throw new BusException("Não é possível salvar o relacionamento pois o modelo relacionado é inválido ");
			}

			Optional<Model> dbModel;
			if (model.getModel().getId() != null) {
				dbModel = this.modelService.getById(model.getModel().getId());
			} else {
				model.setModel(this.modelService.save(model.getModel(), userProfile).get());
			}

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade modelo relacionadas ao modelo de produto.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	
}
