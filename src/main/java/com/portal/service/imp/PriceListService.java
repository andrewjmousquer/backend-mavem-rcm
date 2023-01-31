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
	 * Lista todos as listas de preços.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");
	 */
	@Override
	public List<PriceList> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
		} catch (Exception e) {
			log.error( "Erro no processo de listar as listas de preços.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	 * @param userProfile dados do usuário logado.
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
			log.error( "Erro no processo de cadastro da lista de preço: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza uma lista de preço
	 * 
	 * @param model objeto canal que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<PriceList> update(PriceList model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			// PRL-U4
			Optional<PriceList> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "A lista de preço a ser atualizada não existe.");
			}
			
			this.validateHasDuplicate(model);
			
			// PRL-U6
			if( modelDB.get().getChannel() != null && modelDB.get().getChannel().getId() != null ) {
				if( !modelDB.get().getChannel().equals( model.getChannel() ) ) {
					throw new BusException( "Não é permitido trocar o canal em uma lista já salva.");
				}
			}
			
			// PRL-U7
			if( modelDB.get().getAllPartners() != null && !model.getAllPartners().equals( model.getAllPartners() )	) {
				throw new BusException( "Não é permitido alterar a marcação de todos os parceiros em uma lista já salva.");
			}
			
			Optional<PriceList> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PRICE_LIST_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização da lista de preço: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");
	 */
	@Override
	public List<PriceList> find( PriceList model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as listas de preços.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto canais para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");
	 */
	@Override
	public List<PriceList> search( PriceList model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar as listas de preços.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca canais que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(PriceList, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id")
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
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(PriceList, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id")
	 * 
	 * @param model objeto canal para ser buscado
	 */
	@Override
	public List<PriceList> search(PriceList model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma lista de preço pelo seu ID
	 * 
	 * @param id ID da lista de preço
	 */
	@Override
	public Optional<PriceList> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar uma lista de preço pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos as listas de preços.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");
	 */
	@Override
	public List<PriceList> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de uma lista de preço
	 * 
	 * @param id ID da lista de preço
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<PriceList> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A lista de preço a ser excluída não existe.");
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
			log.error( "Erro no processo de exclusão da lista de preço.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	public List<PriceList> listOverlay( PriceList model ) throws AppException, BusException {
		
		try {
			if( model == null ) {
				throw new BusException( "Não é possível listar as sobreposições sem a o objeto de lista de preço válido. " );
			}
			
			return this.dao.listOverlay(model);
		} catch (Exception e) {
			log.error( "Erro no processo de listar as sobreposições de listas de preços.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { PriceList.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
		
	}

	/**
	 * Exclui todos os preços relacionados ao produtos
	 * 
	 * REGRA: PRL-D3
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento excluídos e para que 
	 * o delete passe pelas regras de exclusão da entidade.
	 */
	private void deleteProductPriceRelationship(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			PriceProduct findBy = PriceProduct.builder()
													.priceList( PriceList.builder().id(id).build() )
													.build();

			List<PriceProduct> modelsDB = this.productPriceService.find(findBy, DEFAULT_PAGINATION);
			if( modelsDB != null ) {
				log.debug( "Existem {} preço(s) de produto(s) para serem deletadas.", modelsDB.size() );
				for (PriceProduct item : modelsDB) {
					this.productPriceService.delete(item.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão dos preços dos produtos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Exclui todos os preços relacionados aos itens
	 * 
	 * REGRA: PRL-D1
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento excluídos e para que 
	 * o delete passe pelas regras de exclusão da entidade.
	 */
	private void deleteItemPriceRelationship(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			PriceItem findBy = PriceItem.builder()
											.priceList( PriceList.builder().id(id).build() )
											.build();

			List<PriceItem> modelsDB = this.itemPriceService.find(findBy, DEFAULT_PAGINATION);
			if( modelsDB != null ) {
				log.debug( "Existem {} preço(s) de item(s) para serem deletadas.", modelsDB.size() );
				for (PriceItem item : modelsDB) {
					this.itemPriceService.delete(item.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão dos preços dos itens.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}		
	}
	
	/**
	 * Exclui todos os preços relacionados ao itens por modelos
	 * 
	 * REGRA: PRL-D4
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento excluídos e para que 
	 * o delete passe pelas regras de exclusão da entidade.
	 */
	private void deleteItemModelPriceRelationship(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			PriceItemModel findBy = PriceItemModel.builder()
											.priceList( PriceList.builder().id(id).build() )
											.build();

			List<PriceItemModel> modelsDB = this.itemModelPriceService.find(findBy, DEFAULT_PAGINATION);
			if( modelsDB != null ) {
				log.debug( "Existem {} preço(s) de item(s) por modelo(s) para serem deletadas.", modelsDB.size() );
				for (PriceItemModel item : modelsDB) {
					this.itemModelPriceService.delete(item.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão dos preços dos itens por modelos.", e );
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
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		// REGRAS: PRL-I2, PRL-U2,
		PriceList rnSearch = PriceList.builder()
										.name( model.getName() )
										.build();

		List<PriceList> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe uma lista de preço com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && listBD != null && !listBD.isEmpty()) {
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe uma lista de preço com o mesmo nome.");
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
				throw new BusException( "Já existe uma lista de preço com o mesmo canal, parceiros dentro de uma mesma vigência.");
			}
			
			// Update Action
			if( model.getId() != null && !model.getId().equals(0) && listBD != null && !listBD.isEmpty()) {
				
				long count = listBD.stream()
								.filter( item -> !item.getId().equals( model.getId() ) )
								.count();
				
				if( count > 0 ) {
					throw new BusException( "Já existe uma lista de preço com o mesmo canal, parceiros dentro de uma mesma vigência.");
				}
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PRL-I1, PRL-I3, PRL-U1, PRL-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PriceList model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
		
		Optional<Channel> channel = this.channelService.getById( model.getChannel().getId() );
		if( !channel.isPresent() ) {
			throw new BusException( "Não é possível salvar a lista e preço com o canal inválido ou inexistente." );
		} else if( !channel.get().getActive().booleanValue() ) {
			throw new BusException( "Não é possível salvar a lista e preço com o canal desativado." );
		}
	}
}
