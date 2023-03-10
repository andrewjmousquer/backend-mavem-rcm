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
import com.portal.dao.IPriceItemModelDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Item;
import com.portal.model.ItemModel;
import com.portal.model.Person;
import com.portal.model.PriceItemModel;
import com.portal.model.PriceList;
import com.portal.service.IAuditService;
import com.portal.service.IBrandService;
import com.portal.service.IItemModelService;
import com.portal.service.IItemService;
import com.portal.service.IPriceItemModelService;
import com.portal.service.IPriceListService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PriceItemModelService implements IPriceItemModelService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPriceItemModelDAO dao;
	
	@Autowired
	private IPriceListService priceListService;
	
	@Autowired
	private IBrandService brandService;
	
	@Autowired
	private IItemModelService itemModelService;
	
	@Autowired
	private IItemService itemService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pim_id"); 

	/**
	 * M??todo auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se n??o tiver ID ?? save, caso contr??rio ?? update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<PriceItemModel> saveOrUpdate(PriceItemModel model, UserProfileDTO userProfile) throws AppException, BusException {
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
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<PriceItemModel> save(PriceItemModel model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validatePriceListEntity(model);
			this.validateItemEntity(model);
			this.validateFlags(model);
			
			Optional<PriceItemModel> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRICE_ITEM_MODEL_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do pre??o do item por modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PriceItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um produto
	 * 
	 * @param model objeto produto que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<PriceItemModel> update(PriceItemModel model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			// PIM-U7
			Optional<PriceItemModel> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O pre??o do item por modelo a ser atualizado n??o existe.");
			}
			
			this.validatePriceListEntity(model);
			this.validateItemEntity(model);
			this.validateFlags(model);

			Optional<PriceItemModel> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRICE_ITEM_MODEL_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualiza????o do pre??o do item por modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PriceItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * @param model objeto produtos para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pim_id");
	 */
	@Override
	public List<PriceItemModel> find( PriceItemModel model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar o pre??o do item por modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { PriceItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String s??o bmodeluscados com o '='
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(PriceItemModel, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pim_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public Optional<PriceItemModel> find(PriceItemModel model) throws AppException, BusException {
		List<PriceItemModel> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * @deprecated Essa opera????o n??o existe nessa entidade. Use o {@link #find(PriceItemModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	public List<PriceItemModel> search(PriceItemModel model) throws AppException, BusException {
		throw new UnsupportedOperationException( "Essa opera????o n??o existe nessa entidade. Use o find" );
	}
	
	/**
	 * Busca um produto pelo seu ID
	 * 
	 * @param id ID do produto
	 */
	@Override
	public Optional<PriceItemModel> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inv??lido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o pre??o do item por modelo: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PriceItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * @deprecated Essa opera????o n??o existe nessa entidade. Use o {@link #find(PriceItemModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceItemModel> list() throws AppException, BusException {
		throw new UnsupportedOperationException( "Essa opera????o n??o existe nessa entidade. Use o find" );
	}

	/**
	 * Efetua a exclus??o de um produto
	 * 
	 * @param id ID do produto
	 * @param userProfile dados do usu??rio logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclus??o inv??lido." );
			}
			
			Optional<PriceItemModel> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O pre??o do item por modelo a ser exclu??do n??o existe.");
			}

			// REGRA: PIM-D1
			this.validateHasProposalRelationship( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PRICE_ITEM_MODEL_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o do relacionamento entre pre??o do item e modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PriceItemModel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	@Override
	public void audit(PriceItemModel model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formata????o e obrigatoriedade
	 * 
	 * Regra: PIM-I1,PIM-I2,PIM-I5,PIM-I7,PIM-U1,PIM-U2,PIM-U5,PIM-U7
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de valida????o que ser?? usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PriceItemModel model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

	private void validateFlags( PriceItemModel model ) throws AppException, BusException {
		
		try {
			if( model != null  ) {
				
				// REGRA: PIM-I6, PIM-U6
				if( model.getAllBrands().booleanValue() ) {
					if( model.getItemModel() != null || model.getBrand() != null ) {
						throw new BusException( "N??o ?? permitido salvar item por modelo ou marca quando a flag TODOS AS MARCAS est?? selecionada." );
					}
					
				} else { 

					this.validateBrandEntity( model );
					
					// REGRA: PIM-I3, PIM-U3, PIM-I4, PIM-U4
					if( model.getAllModels().booleanValue() ) { 			
					
						if( model.getItemModel() != null ) {
							throw new BusException( "N??o ?? permitido salvar o item por modelo quando a flag TODOS OS MODELOS est?? selecionada." );
						}
					}
				}

				// REGRA: PIM-I9,PIM-U9
				if( !model.getAllBrands().booleanValue() && !model.getAllModels().booleanValue() ) {
					this.validateItemModelEntity(model);
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade lista de pre??o relacionadas ao pre??o do item por modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
		
	}
	
	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * REGRAS: PIM-I8,PIM-U8
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sist??mico
	 * @throws BusException	Em caso de erro relacionado a regra de neg??cio
	 */
	private void validatePriceListEntity(PriceItemModel model) throws BusException, AppException {
		try {
			if( model != null  ) {
				// REGRA: PIM-I3,PIM-U3
				if( model.getPriceList() == null || model.getPriceList().getId() == null || model.getPriceList().getId().equals(0) ) {
					throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois a lista de pre??o relacionada ?? inv??lida ou n??o existe." );
				} else {
					Optional<PriceList> dbPriceList = this.priceListService.getById( model.getPriceList().getId() );
					if( !dbPriceList.isPresent() ) {
						throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois a lista de pre??o relacionada ?? inv??lida ou n??o existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade lista de pre??o relacionadas ao pre??o do item por modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sist??mico
	 * @throws BusException	Em caso de erro relacionado a regra de neg??cio
	 */
	private void validateBrandEntity(PriceItemModel model) throws BusException, AppException {
		try {
			if( model != null  ) {
				if( model.getBrand() == null || model.getBrand().getId() == null || model.getBrand().getId().equals(0) ) {
					throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois a marca relacionada ?? inv??lida ou n??o existe." );
				} else {
					Optional<Brand> dbPriceList = this.brandService.getById( model.getBrand().getId() );
					if( !dbPriceList.isPresent() ) {
						throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois a marca relacionada ?? inv??lida ou n??o existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade marca relacionadas ao pre??o do item por modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sist??mico
	 * @throws BusException	Em caso de erro relacionado a regra de neg??cio
	 */
	private void validateItemModelEntity(PriceItemModel model) throws BusException, AppException {
		try {
			if( model != null  ) {
				if( model.getItemModel() == null || model.getItemModel().getId() == null || model.getItemModel().getId().equals(0) ) {
					throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois o item por modelo relacionado ?? inv??lido ou n??o existe." );
				} else {
					Optional<ItemModel> dbPriceList = this.itemModelService.getById( model.getItemModel().getId() );
					if( !dbPriceList.isPresent() ) {
						throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois o item por modelo relacionado ?? inv??lido ou n??o existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade modelo do item relacionadas ao pre??o do item por modelo.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sist??mico
	 * @throws BusException	Em caso de erro relacionado a regra de neg??cio
	 */
	private void validateItemEntity(PriceItemModel model)  throws BusException, AppException {
		try {
			if( model != null  ) {
				if( model.getItem() == null || model.getItem().getId() == null || model.getItem().getId().equals(0) ) {
					throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois o item relacionado ?? inv??lido ou n??o existe." );
				} else {
					Optional<Item> dbItem = this.itemService.getById( model.getItem().getId() );
					if( !dbItem.isPresent() ) {
						throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois o item relacionado ?? inv??lido ou n??o existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade de item relacionadas a lista de pre??o.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com proposta.
	 *  
	 * REGRA: PIM-D1
	 *  
	 * @param prpId	ID do pre??o de produto que deve ser verificada
	 * @throws AppException	Em caso de erro sist??mico
	 * @throws BusException	Em caso de erro relacionado a regra de neg??cio
	 * @throws NoSuchMessageException 
	 */
	private void validateHasProposalRelationship(Integer prpId) throws BusException, AppException {
		try {
			if( prpId != null ) {
				boolean exists = this.dao.hasProposalDetailRelationship( prpId );
				if( exists ) {
					throw new BusException( "N??o ?? poss??vel excluir o pre??o do item por modelo pois existe um relacionamento com a proposta." );
				}
				
			} else {
				throw new BusException( "ID do pre??o do item por modelo inv??lido para checar o relacionamento com proposta." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pre??o de produto e proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
