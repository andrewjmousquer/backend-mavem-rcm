package com.portal.service.imp;

import java.util.List;
import java.util.Objects;
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
import com.portal.dao.IProductDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.service.IAuditService;
import com.portal.service.IProductModelService;
import com.portal.service.IProductService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProductService implements IProductService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IProductDAO dao;
	
	@Autowired
	private IProductModelService productModelService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id"); 

	/**
	 * Lista todos os produtos.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id");
	 */
	@Override
	public List<Product> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {
			
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os produtos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<Product> saveOrUpdate(Product model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<Product> save(Product model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			
			Optional<Product> saved = this.dao.save(model);

			if(model.getModels() != null){
				Product  product = new Product(saved.get());
				List<ProductModel> productModelList =  model.getModels();
				for (ProductModel productModel:productModelList) {
					productModel.setProduct(product);
					this.productModelService.save(productModel, userProfile);
				}
			}
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do produto: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um produto
	 * 
	 * @param model objeto produto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Product> update(Product model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity(model, OnUpdate.class);
			
			// PRD-U4
			Optional<Product> modelDB = this.getById( model.getId() );

			if(model.getModels() != null){
				List<ProductModel> productModelListBD = this.productModelService.getByProduct(modelDB.get().getId());
				Product  product = new Product(modelDB.get());
				List<ProductModel> productModelList =  model.getModels();
				productModelListBD.removeAll(productModelList);
				if(productModelListBD.size() > 0){
					for (ProductModel productModel: productModelListBD) {
						this.productModelService.delete(productModel.getId(), userProfile);
					}
				}
				for (ProductModel productModel:productModelList) {
					productModel.setProduct(product);
					if(productModel.getId()==null){
						this.productModelService.save(productModel, userProfile);
					}else{
						this.productModelService.update(productModel, userProfile);
					}
				}

			}
			if( !modelDB.isPresent() ) {
				throw new BusException( "O produto a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate(model);

			Optional<Product> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do produto: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public List<Product> find( Product model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar os produtos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto produtos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id");
	 */
	@Override
	public List<Product> search( Product model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar os produtos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Product, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public Optional<Product> find(Product model) throws AppException, BusException {
		List<Product> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Product, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public List<Product> search(Product model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca um produto pelo seu ID
	 * 
	 * @param id ID do produto
	 */
	@Override
	public Optional<Product> getById(Integer id) throws AppException, BusException {
		try {

			if (id == null) {
				throw new BusException("ID de busca inválido.");
			}
			Product product = new Product();
			product = this.dao.getById(id).get();

			List<ProductModel> productModelList = this.productModelService.getByProduct(id);
			if(productModelList.size() > 0) {
				product.initializerProductModelList();
				for (ProductModel productModel : productModelList) {
					product.setProductModelList(productModel);;
				}
			}
			return Optional.ofNullable(product);

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um produto pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos os produtos.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id");
	 */
	@Override
	public List<Product> list() throws AppException, BusException {
		return this.listAll( null );
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
			
			Optional<Product> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "O produto a ser excluído não existe.");
			}

			// REGRA: PRD-D1
			this.deleteProductModelRelationship(id, userProfile);
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PRODUCT_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do produto.", e );
			throw new AppException( this.messageSource.getMessage("error.product.delete", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	/**
	 * Exclui todos os relacionamentos com modelo
	 * 
	 * REGRA: PRD-D1
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento excluídos e para que 
	 * o delete passe pelas regras de exclusão da entidade.
	 */
	private void deleteProductModelRelationship( Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}

			ProductModel findByProduct = ProductModel.builder()
					.product(Product.builder()
							.id(id)
							.build()
					).build();
			
			List<ProductModel> modelsDB = this.productModelService.find(findByProduct, DEFAULT_PAGINATION);
			if( modelsDB != null && !modelsDB.isEmpty()) {
				log.debug("Existem {} modelos para serem deletados.", modelsDB.size());
				for (ProductModel productModel : modelsDB) {
					Integer prmId = (productModel.getProduct() != null ? productModel.getId() : null);
					this.productModelService.delete(prmId, userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão dos produtos relacionados.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public void audit(Product model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PRD-I2, PRD-U2, 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( Product model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		Product rnSearch = Product.builder()
				.name( model.getName() )
				.build();

		List<Product> listBD = this.find( rnSearch, null );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() &&
				listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
			throw new BusException( "Já existe um produto com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um produto com o mesmo nome.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PRD-I1, PRD-I3, PRD-U1, PRD-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Product model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

	@Override
	public Optional<Product> getProductByProductModel(Integer prmId) throws AppException, BusException {
		try {

			if (prmId == null) {
				throw new BusException("ID de busca inválido.");
			}
			Product product = new Product();
			product = this.dao.getProductByProductModel(prmId).get();

			return Optional.ofNullable(product);

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um produto pelo ProductModel ID: {}", prmId, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
