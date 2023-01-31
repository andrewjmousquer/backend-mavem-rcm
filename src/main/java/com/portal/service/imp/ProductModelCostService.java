package com.portal.service.imp;

import java.util.ArrayList;
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
import com.portal.dao.IProductModelCostDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Product;
import com.portal.model.ProductModel;
import com.portal.model.ProductModelCost;
import com.portal.service.IAuditService;
import com.portal.service.IProductModelCostService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Ederson Sergio Monteiro Coelho
 *
 */

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProductModelCostService implements IProductModelCostService {

	// DAO's
	@Autowired
	private IProductModelCostDAO dao;

	// Service's
	@Autowired
	private IAuditService auditService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
    public MessageSource messageSource;

	@Autowired
    private Validator validator;

	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pmc_id"); 

	@Override
	public Optional<ProductModelCost> getById(Integer id) throws AppException, BusException {

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

	@Override
	public List<ProductModelCost> list() throws AppException, BusException {
		return this.listAll( null );
	}

	@Override
	public List<ProductModelCost> listAll(Pageable pageable) throws AppException, BusException {

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

	@Override
	public Optional<ProductModelCost> find(ProductModelCost model) throws AppException, BusException {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public List<ProductModelCost> find(ProductModelCost model, Pageable pageable) throws AppException, BusException {

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
	public List<ProductModelCost> search(ProductModelCost model) throws AppException, BusException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProductModelCost> search(ProductModelCost model, Pageable pageable) throws AppException, BusException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<ProductModelCost> findDuplicateMultipleValidate(List<ProductModelCost> models) throws AppException, BusException {
		
		try {

			List<ProductModelCost> modelsDuplicateMultipleValidate = this.validateHasDuplicateMultiple(models);
			
			return modelsDuplicateMultipleValidate;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			//log.error( "Erro no processo de cadastro do Custo por Modelo e Produto: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public Optional<ProductModelCost> saveOrUpdate(ProductModelCost model, UserProfileDTO userProfile)
			throws AppException, BusException {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<ProductModelCost> save(ProductModelCost model, UserProfileDTO userProfile) throws AppException, BusException {

		try {

			this.validateEntity(model, OnSave.class);

			this.validateHasDuplicate(model);

			Optional<ProductModelCost> saved = this.dao.save(model);

			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_MODEL_COST_INSERTED, userProfile);

			return saved;

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do Custo por Modelo e Produto: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<ProductModelCost> saveBulk(List<ProductModelCost> models, UserProfileDTO userProfile) throws AppException, BusException {

		try {
			
			for (ProductModelCost productModelCost : models) {
				
				this.validateEntity(productModelCost, OnSave.class);

				Optional<ProductModelCost> saved = this.dao.save(productModelCost);

				this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_MODEL_COST_INSERTED, userProfile);
			}

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do Custo por Modelo e Produto: {}", models, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
		
		return models;
	}

	@Override
	public Optional<ProductModelCost> update(ProductModelCost model, UserProfileDTO userProfile) throws AppException, BusException {

		try {
			
			this.validateEntity(model, OnUpdate.class);
			this.validateHasDuplicate(model);

			Optional<ProductModelCost> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O produto a ser atualizado não existe.");
			}

			Optional<ProductModelCost> saved = this.dao.update(model);

			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_MODEL_COST_UPDATED, userProfile);

			return saved;

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error( "Erro no processo de atualização do Custo por Modelo e Produto: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<ProductModelCost> updateBulk(List<ProductModelCost> models, UserProfileDTO userProfile) throws AppException, BusException {

		try {
			
			for (ProductModelCost productModelCost : models) {
				
				this.validateEntity(productModelCost, OnUpdate.class);
	
				Optional<ProductModelCost> modelDB = this.getById( productModelCost.getId() );
				if( !modelDB.isPresent() ) {
					throw new BusException( "O produto a ser atualizado não existe.");
				}
	
				Optional<ProductModelCost> saved = this.dao.update(productModelCost);
	
				this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_MODEL_COST_UPDATED, userProfile);
			}
			
			return models;

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error( "Erro no processo de atualização do Custo por Modelo e Produto: {}", models, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {

		try {

			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}

			Optional<ProductModelCost> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "O Custo por Modelo e Produto a ser excluído não existe.");
			}

			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PRODUCT_MODEL_COST_DELETED, userProfile);

			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do Custo por Modelo e Produto.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ProductModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	private void validateEntity( ProductModelCost model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );

		if( model.getStartDate().equals(0) || model.getEndDate().equals(0) ) {
			throw new BusException( "O ano inicio ou fim do Custo por Modelo e Produto não pode ser zero." );
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
	private void validateHasDuplicate( ProductModelCost model ) throws AppException, BusException {

		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}

		ProductModelCost rnSearch = ProductModelCost.builder()
				   									.productModel(model.getProductModel())
				   									.startDate(model.getStartDate())
				   									.endDate(model.getEndDate())
				   									.totalValue(model.getTotalValue())
				   									.build();

		List<ProductModelCost> listBD  = this.dao.findDuplicate( rnSearch );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() &&
				listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
			throw new BusException( "Já existe um relacionamento com esse produto e modelo, dentro do mesmo range de ano/modelo e período.");
		}

		// Update Action
		if( model.getId() != null && !model.getId().equals(0) &&  listBD != null && !listBD.isEmpty()) {

			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) )
							.count();

			if( count > 0 ) {
				throw new BusException( "Já existe um relacionamento com esse produto e modelo, dentro do mesmo range de ano/modelo e período.");
			}
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PRM-I6,PRM-U6 
	 * 
	 * @param models	entidade a ser valiadada
	 * @return 
	 * @throws AppException
	 * @throws BusException
	 */
	private List<ProductModelCost> validateHasDuplicateMultiple( List<ProductModelCost> models ) throws AppException, BusException {

		if( models == null || models.isEmpty()) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		List<ProductModelCost> modelsDuplicateMultipleValidate = new ArrayList<>();
		
		for (ProductModelCost productModelCost : models) {
		
			ProductModelCost rnSearch = ProductModelCost.builder()
														.productModel(productModelCost.getProductModel())
														.startDate(productModelCost.getStartDate())
														.endDate(productModelCost.getEndDate())
														.totalValue(productModelCost.getTotalValue())
														.build();

			List<ProductModelCost> listBD  = this.dao.findDuplicate( rnSearch );
			
			// Save Action
			if( ( productModelCost.getId() == null || productModelCost.getId().equals(0) ) && 
				  listBD != null && !listBD.isEmpty() &&
				  listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), productModelCost.getId()))) {
				
				//throw new BusException( "Já existe um relacionamento com esse produto e modelo, dentro do mesmo range de ano/modelo e período.");
				modelsDuplicateMultipleValidate.add(productModelCost);
			}
			
			// Update Action
			if( productModelCost.getId() != null && !productModelCost.getId().equals(0) &&  listBD != null && !listBD.isEmpty()) {
			
				long count = listBD.stream()
								   .filter( item -> !item.getId().equals( productModelCost.getId() ) )
								   .count();
				
				if( count > 0 ) {
					//throw new BusException( "Já existe um relacionamento com esse produto e modelo, dentro do mesmo range de ano/modelo e período.");
					modelsDuplicateMultipleValidate.add(productModelCost);
				}
			}
		}
		
		return modelsDuplicateMultipleValidate;
	}

	@Override
	public void audit(ProductModelCost model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
}