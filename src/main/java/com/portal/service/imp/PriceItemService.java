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
import com.portal.dao.IPriceItemDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Item;
import com.portal.model.Person;
import com.portal.model.PriceItem;
import com.portal.model.PriceList;
import com.portal.service.IAuditService;
import com.portal.service.IItemService;
import com.portal.service.IPriceItemService;
import com.portal.service.IPriceListService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PriceItemService implements IPriceItemService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPriceItemDAO dao;
	
	@Autowired
	private IPriceListService priceListService;
	
	@Autowired
	private IItemService itemService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pci_id"); 

	/**
	 * M??todo auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se n??o tiver ID ?? save, caso contr??rio ?? update.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<PriceItem> saveOrUpdate(PriceItem model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<PriceItem> save(PriceItem model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validatePriceListEntity(model);
			this.validateItemEntity(model);
			this.validateHasDuplicate(model);
			
			Optional<PriceItem> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRICE_ITEM_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do pre??o do produto: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PriceItem.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um item
	 * 
	 * @param model objeto produto que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<PriceItem> update(PriceItem model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			// PCI-U7
			Optional<PriceItem> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O pre??o do item a ser atualizado n??o existe.");
			}
			
			this.validatePriceListEntity(model);
			this.validateItemEntity(model);
			this.validateHasDuplicate(model);

			Optional<PriceItem> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRICE_ITEM_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualiza????o do pre??o do item: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PriceItem.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca itens que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * @param model objeto produtos para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pci_id");
	 */
	@Override
	public List<PriceItem> find( PriceItem model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar o pre??o do item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { PriceItem.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca itens que respeitem os dados do objeto.
	 * Aqui os campos String s??o bmodeluscados com o '='
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(PriceItem, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pci_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public Optional<PriceItem> find(PriceItem model) throws AppException, BusException {
		List<PriceItem> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * @deprecated Essa opera????o n??o existe nessa entidade. Use o {@link #find(PriceItem, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	public List<PriceItem> search(PriceItem model) throws AppException, BusException {
		throw new UnsupportedOperationException( "Essa opera????o n??o existe nessa entidade. Use o find" );
	}
	
	/**
	 * Busca um item pelo seu ID
	 * 
	 * @param id ID do produto
	 */
	@Override
	public Optional<PriceItem> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inv??lido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o pre??o do item: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PriceItem.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * @deprecated Essa opera????o n??o existe nessa entidade. Use o {@link #find(PriceItem, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceItem> list() throws AppException, BusException {
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
			
			Optional<PriceItem> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O pre??o do item a ser exclu??do n??o existe.");
			}

			// REGRA: PCI-D1
			this.validateHasProposalRelationship( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PRODUCT_MODEL_DELETED, userProfile);
			
			this.dao.delete( id );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o do pre??o do item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PriceItem.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	@Override
	public void audit(PriceItem model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PCI-I6,PCI-U6 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( PriceItem model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "N??o ?? poss??vel chegar a duplicidade com o objeto da entidade nula." );
		}
		
		PriceItem rnSearch = PriceItem.builder()
											.priceList( model.getPriceList() )
											.item( model.getItem() )
											.build();

		List<PriceItem> listBD  = this.dao.find( rnSearch, DEFAULT_PAGINATION );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() &&
				listBD.stream().anyMatch(p -> !Objects.equals(p.getId(), model.getId()))) {
			throw new BusException( "J?? existe um pre??o definido para esse item na mesma lista.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) &&  listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "J?? existe um pre??o definido para esse item na mesma lista.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formata????o e obrigatoriedade
	 * 
	 * Regra: PCI-I1,PCI-I2,PCI-I4,PCI-U1,PCI-U2,PCI-U4
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de valida????o que ser?? usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PriceItem model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

	/**
	 * Valida se a entidade lista de pre??o relacionada existe
	 * 
	 * REGRAS: PCI-I3,PCI-U3
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sist??mico
	 * @throws BusException	Em caso de erro relacionado a regra de neg??cio
	 */
	private void validatePriceListEntity(PriceItem model) throws BusException, AppException {
		try {
			if( model != null  ) {
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
			log.error( "Erro ao validar a entidade lista de pre??o relacionadas ao pre??o do item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * REGRAS: PCI-I5,PCI-U5
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sist??mico
	 * @throws BusException	Em caso de erro relacionado a regra de neg??cio
	 */
	private void validateItemEntity(PriceItem model) throws BusException, AppException {
		try {
			if( model != null  ) {
				if( model.getItem() == null || model.getItem().getId() == null || model.getItem().getId().equals(0) ) {
					throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois o item relacionado ?? inv??lido ou n??o existe." );
				} else {
					Optional<Item> dbModel = this.itemService.getById( model.getItem().getId() );
					if( !dbModel.isPresent() ) {
						throw new BusException( "N??o ?? poss??vel salvar o relacionamento pois o item relacionado ?? inv??lido ou n??o existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade item relacionada a lista de pre??o.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Valida se existe algum relacionamento com proposta.
	 *  
	 * REGRA: PDP-D1
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
					throw new BusException( "N??o ?? poss??vel excluir o pre??o do item pois existe um relacionamento com a proposta." );
				}
				
			} else {
				throw new BusException( "ID do pre??o do item inv??lido para checar o relacionamento com proposta." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pre??o de item e proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
