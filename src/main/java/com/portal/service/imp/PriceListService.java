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
import com.portal.dao.IPriceListDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;
import com.portal.model.PaymentMethod;
import com.portal.model.PriceItem;
import com.portal.model.PriceItemModel;
import com.portal.model.PriceList;
import com.portal.model.PriceProduct;
import com.portal.service.IAuditService;
import com.portal.service.IChannelService;
import com.portal.service.IPriceItemModelService;
import com.portal.service.IPriceItemService;
import com.portal.service.IPriceListPartnerService;
import com.portal.service.IPriceListService;
import com.portal.service.IPriceProductService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PriceListService implements IPriceListService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPriceListDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private IPriceListPartnerService partnerPriceListService;
	
	@Autowired
	private IPriceProductService productPriceService;
	
	@Autowired
	private IPriceItemService itemPriceService;
	
	@Autowired
	private IPriceItemModelService itemModelPriceService;
	
	@Autowired
	private IChannelService channelService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id"); 

	/**
	 * Lista todos as listas de pre??os.
	 * 
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");
	 */
	@Override
	public List<PriceList> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
		} catch (Exception e) {
			log.error( "Erro no processo de listar as listas de pre??os.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<PriceList> saveOrUpdate(PriceList model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<PriceList> save(PriceList model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			
			Optional<PriceList> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRICE_LIST_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro da lista de pre??o: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza uma lista de pre??o
	 * 
	 * @param model objeto canal que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<PriceList> update(PriceList model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			// PRL-U4
			Optional<PriceList> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "A lista de pre??o a ser atualizada n??o existe.");
			}
			
			this.validateHasDuplicate(model);
			
			// PRL-U6
			if( modelDB.get().getChannel() != null && modelDB.get().getChannel().getId() != null ) {
				if( !modelDB.get().getChannel().equals( model.getChannel() ) ) {
					throw new BusException( "N??o ?? permitido trocar o canal em uma lista j?? salva.");
				}
			}
			
			// PRL-U7
			if( modelDB.get().getAllPartners() != null && !model.getAllPartners().equals( model.getAllPartners() )	) {
				throw new BusException( "N??o ?? permitido alterar a marca????o de todos os parceiros em uma lista j?? salva.");
			}
			
			Optional<PriceList> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRICE_LIST_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualiza????o da lista de pre??o: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");
	 */
	@Override
	public List<PriceList> find( PriceList model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as listas de pre??os.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o 'LIKE'
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");
	 */
	@Override
	public List<PriceList> search( PriceList model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar as listas de pre??os.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(PriceList, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id")
	 * 
	 * @param model objeto canal para ser buscado
	 */
	@Override
	public Optional<PriceList> find(PriceList model) throws AppException, BusException {
		List<PriceList> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o 'LIKE'
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(PriceList, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id")
	 * 
	 * @param model objeto canal para ser buscado
	 */
	@Override
	public List<PriceList> search(PriceList model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma lista de pre??o pelo seu ID
	 * 
	 * @param id ID da lista de pre??o
	 */
	@Override
	public Optional<PriceList> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inv??lido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar uma lista de pre??o pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos as listas de pre??os.
	 *
	 * Esse m??todo ?? uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");
	 */
	@Override
	public List<PriceList> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclus??o de uma lista de pre??o
	 * 
	 * @param id ID da lista de pre??o
	 * @param userProfile dados do usu??rio logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclus??o inv??lido." );
			}
			
			Optional<PriceList> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A lista de pre??o a ser exclu??da n??o existe.");
			}

			// REGRA: PRL-D2
			this.partnerPriceListService.deleteByPriceList(id);

			// REGRA: PRL-D3
			this.deleteProductPriceRelationship(id, userProfile);

			// REGRA: PRL-D1
			this.deleteItemPriceRelationship(id, userProfile);
			
			// REGRA: PRL-D4
			this.deleteItemModelPriceRelationship(id, userProfile);
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PRICE_LIST_DELETED, userProfile);
			
			this.dao.delete( id );
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o da lista de pre??o.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	public List<PriceList> listOverlay( PriceList model ) throws AppException, BusException {
		
		try {
			if( model == null ) {
				throw new BusException( "N??o ?? poss??vel listar as sobreposi????es sem a o objeto de lista de pre??o v??lido. " );
			}
			
			return this.dao.listOverlay(model);
		} catch (Exception e) {
			log.error( "Erro no processo de listar as sobreposi????es de listas de pre??os.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
		
	}

	/**
	 * Exclui todos os pre??os relacionados ao produtos
	 * 
	 * REGRA: PRL-D3
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento exclu??dos e para que 
	 * o delete passe pelas regras de exclus??o da entidade.
	 */
	private void deleteProductPriceRelationship(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclus??o inv??lido." );
			}
			
			PriceProduct findBy = PriceProduct.builder()
													.priceList( PriceList.builder().id(id).build() )
													.build();

			List<PriceProduct> modelsDB = this.productPriceService.find(findBy, DEFAULT_PAGINATION);
			if( modelsDB != null ) {
				log.debug( "Existem {} pre??o(s) de produto(s) para serem deletadas.", modelsDB.size() );
				for (PriceProduct item : modelsDB) {
					this.productPriceService.delete(item.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o dos pre??os dos produtos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Exclui todos os pre??os relacionados aos itens
	 * 
	 * REGRA: PRL-D1
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento exclu??dos e para que 
	 * o delete passe pelas regras de exclus??o da entidade.
	 */
	private void deleteItemPriceRelationship(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclus??o inv??lido." );
			}
			
			PriceItem findBy = PriceItem.builder()
											.priceList( PriceList.builder().id(id).build() )
											.build();

			List<PriceItem> modelsDB = this.itemPriceService.find(findBy, DEFAULT_PAGINATION);
			if( modelsDB != null ) {
				log.debug( "Existem {} pre??o(s) de item(s) para serem deletadas.", modelsDB.size() );
				for (PriceItem item : modelsDB) {
					this.itemPriceService.delete(item.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o dos pre??os dos itens.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}		
	}
	
	/**
	 * Exclui todos os pre??os relacionados ao itens por modelos
	 * 
	 * REGRA: PRL-D4
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento exclu??dos e para que 
	 * o delete passe pelas regras de exclus??o da entidade.
	 */
	private void deleteItemModelPriceRelationship(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclus??o inv??lido." );
			}
			
			PriceItemModel findBy = PriceItemModel.builder()
											.priceList( PriceList.builder().id(id).build() )
											.build();

			List<PriceItemModel> modelsDB = this.itemModelPriceService.find(findBy, DEFAULT_PAGINATION);
			if( modelsDB != null ) {
				log.debug( "Existem {} pre??o(s) de item(s) por modelo(s) para serem deletadas.", modelsDB.size() );
				for (PriceItemModel item : modelsDB) {
					this.itemModelPriceService.delete(item.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o dos pre??os dos itens por modelos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}				
	}

	@Override
	public void audit(PriceList model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PRL-I2, PRL-U2, PRL-I6, PRL-U8
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( PriceList model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "N??o ?? poss??vel chegar a duplicidade com o objeto da entidade nula." );
		}
		
		// REGRAS: PRL-I2, PRL-U2,
		PriceList rnSearch = PriceList.builder()
										.name( model.getName() )
										.build();

		List<PriceList> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "J?? existe uma lista de pre??o com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && listBD != null && !listBD.isEmpty()) {
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "J?? existe uma lista de pre??o com o mesmo nome.");
			}
		}
		
		// REGRAS: PRL-I6, PRL-U8
		if( model.getAllPartners().booleanValue() || !model.getChannel().getHasPartner()  ) {
			rnSearch = PriceList.builder()
									.channel( model.getChannel() )
									.allPartners( model.getAllPartners() )
									.start( model.getStart() )
									.end( model.getEnd() )
									.build();
			
			listBD = this.listOverlay( rnSearch );
		
			// Save Action
			if( ( model.getId() == null || model.getId().equals(0) ) && listBD != null && !listBD.isEmpty() ) {
				throw new BusException( "J?? existe uma lista de pre??o com o mesmo canal, parceiros dentro de uma mesma vig??ncia.");
			}
			
			// Update Action
			if( model.getId() != null && !model.getId().equals(0) && listBD != null && !listBD.isEmpty()) {
				
				long count = listBD.stream()
								.filter( item -> !item.getId().equals( model.getId() ) )
								.count();
				
				if( count > 0 ) {
					throw new BusException( "J?? existe uma lista de pre??o com o mesmo canal, parceiros dentro de uma mesma vig??ncia.");
				}
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formata????o e obrigatoriedade
	 * 
	 * Regra: PRL-I1, PRL-I3, PRL-U1, PRL-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de valida????o que ser?? usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PriceList model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
		
		Optional<Channel> channel = this.channelService.getById( model.getChannel().getId() );
		if( !channel.isPresent() ) {
			throw new BusException( "N??o ?? poss??vel salvar a lista e pre??o com o canal inv??lido ou inexistente." );
		} else if( !channel.get().getActive().booleanValue() ) {
			throw new BusException( "N??o ?? poss??vel salvar a lista e pre??o com o canal desativado." );
		}
	}
}
