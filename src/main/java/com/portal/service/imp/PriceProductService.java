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
import com.portal.dao.IPriceProductDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Person;
import com.portal.model.PriceList;
import com.portal.model.PriceProduct;
import com.portal.model.ProductModel;
import com.portal.service.IAuditService;
import com.portal.service.IPriceListService;
import com.portal.service.IPriceProductService;
import com.portal.service.IProductModelService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PriceProductService implements IPriceProductService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPriceProductDAO dao;
	
	@Autowired
	private IPriceListService priceListService;
	
	@Autowired
	private IProductModelService productModelService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppr_id"); 

	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<PriceProduct> saveOrUpdate(PriceProduct model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<PriceProduct> save(PriceProduct model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validatePriceListEntity(model);
			this.validateProductModelEntity(model);
			this.validateHasDuplicate(model);
			
			Optional<PriceProduct> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_PRICE_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do preço do produto: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PriceProduct.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um produto
	 * 
	 * @param model objeto produto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<PriceProduct> update(PriceProduct model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			// PDP-U7
			Optional<PriceProduct> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O preço do produto a ser atualizado não existe.");
			}
			
			this.validatePriceListEntity(model);
			this.validateProductModelEntity(model);
			this.validateHasDuplicate(model);

			Optional<PriceProduct> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRODUCT_PRICE_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do preço do produto: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PriceProduct.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto produtos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppr_id");
	 */
	@Override
	public List<PriceProduct> find( PriceProduct model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar o preço do produto.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { PriceProduct.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são bmodeluscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(PriceProduct, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppr_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public Optional<PriceProduct> find(PriceProduct model) throws AppException, BusException {
		List<PriceProduct> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(PriceProduct, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	public List<PriceProduct> search(PriceProduct model) throws AppException, BusException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}
	
	/**
	 * Busca um produto pelo seu ID
	 * 
	 * @param id ID do produto
	 */
	@Override
	public Optional<PriceProduct> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o preço do produto: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PriceProduct.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(PriceProduct, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceProduct> list() throws AppException, BusException {
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
			
			Optional<PriceProduct> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O preço do produto a ser excluído não existe.");
			}

			// REGRA: PDP-D1
			this.validateHasProposalRelationship( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PRODUCT_PRICE_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do relacionamento entre produto e modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PriceProduct.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	@Override
	public void audit(PriceProduct model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PDP-I6,PDP-U6 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( PriceProduct model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		PriceProduct rnSearch = PriceProduct.builder()
											.priceList( model.getPriceList() )
											.productModel( model.getProductModel() )
											.build();

		List<PriceProduct> listBD  = this.dao.find( rnSearch, DEFAULT_PAGINATION );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() &&
				listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
			throw new BusException( "Já existe um preço definido para esse modelo na mesma lista.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) &&  listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um preço definido para esse modelo na mesma lista.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PDP-I1,PDP-I2,PDP-I4,PDP-U1,PDP-U2,PDP-U4
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PriceProduct model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * REGRAS: PDP-I3,PDP-U3
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validatePriceListEntity(PriceProduct model) throws BusException, AppException {
		try {
			if( model != null  ) {
				// REGRA: PDP-I3,PDP-U3
				if( model.getPriceList() == null || model.getPriceList().getId() == null || model.getPriceList().getId().equals(0) ) {
					throw new BusException( "Não é possível salvar o relacionamento pois a lista de preço relacionada é inválida ou não existe." );
				} else {
					Optional<PriceList> dbPriceList = this.priceListService.getById( model.getPriceList().getId() );
					if( !dbPriceList.isPresent() ) {
						throw new BusException( "Não é possível salvar o relacionamento pois a lista de preço relacionada é inválida ou não existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade lista de preço relacionadas ao preço do produto.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * REGRAS: PDP-I5,PDP-U5
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateProductModelEntity(PriceProduct model) throws BusException, AppException {
		try {
			if( model != null  ) {
				// REGRA: PDP-I5 e PDP-U5
				if( model.getProductModel() == null || model.getProductModel().getId() == null || model.getProductModel().getId().equals(0) ) {
					throw new BusException( "Não é possível salvar o relacionamento pois o modelo de produto relacionado é inválido ou não existe." );
				} else {
					Optional<ProductModel> dbModel = this.productModelService.getById( model.getProductModel().getId() );
					if( !dbModel.isPresent() ) {
						throw new BusException( "Não é possível salvar o relacionamento pois o modelo de produto relacionado é inválido ou não existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade modelo de produto relacionada ao preço do produto.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Valida se existe algum relacionamento com proposta.
	 *  
	 * REGRA: PDP-D1
	 *  
	 * @param prpId	ID do preço de produto que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validateHasProposalRelationship(Integer prpId) throws BusException, AppException {
		try {
			if( prpId != null ) {
				boolean exists = this.dao.hasProposalDetailRelationship( prpId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o preço do produto pois existe um relacionamento com a proposta." );
				}
				
			} else {
				throw new BusException( "ID do preço do produto inválido para checar o relacionamento com proposta." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre preço de produto e proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
