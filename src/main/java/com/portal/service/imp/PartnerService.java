package com.portal.service.imp;

import java.util.ArrayList;
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
import com.portal.dao.IPartnerDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Channel;
import com.portal.model.Partner;
import com.portal.model.PartnerPerson;
import com.portal.model.Person;
import com.portal.model.Seller;
import com.portal.service.IAuditService;
import com.portal.service.IChannelService;
import com.portal.service.IPartnerBrandService;
import com.portal.service.IPartnerPersonService;
import com.portal.service.IPartnerService;
import com.portal.service.IPersonService;
import com.portal.service.ISellerPartnerService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PartnerService implements IPartnerService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPartnerDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private IPersonService personService;

	@Autowired
	private IChannelService channelService;

	@Autowired
	private IPartnerPersonService partnerPersonService;

	@Autowired
	private IPartnerBrandService partnerBrandService;

    @Autowired
    private ISellerPartnerService sellerPartnerService;
	
	@Autowired
	private ObjectMapper objectMapper;

	private static final Pageable DEFAULT_PAGINATION = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "ptn_id");

	/**
	 * Lista todos os parceiros.
	 *
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id");
	 */
	@Override
	public List<Partner> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {

			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			List<Partner> partners = this.dao.listAll(pageable);
			return partners;

		} catch (Exception e) {
			log.error("Erro no processo de listar os parceiros.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{Partner.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 *
	 * @param model       objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Partner> saveOrUpdate(Partner model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<Partner> save( Partner model, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			this.validateChannel(model.getChannel());

			this.personService.saveOrUpdate(model.getPerson(), userProfile);

			Optional<Partner> saved = this.dao.save(model);

			this.syncPartnerBrandRelationship( model, userProfile );
			this.syncPartnerEmployeeRelationship( model, userProfile );
			this.syncPartnerSellerRelationship( model, userProfile );
			
			this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.PARTNER_INSERTED, userProfile);

			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do parceiro: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Partner.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Atualiza um parceiro
	 * 
	 * @param model objeto parceiro que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Partner> update(Partner model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			Optional<Partner> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O parceiro a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate( model );
			this.validateChannel( model.getChannel() );

			this.personService.saveOrUpdate( model.getPerson(), userProfile);

			Optional<Partner> saved = this.dao.update(model);
			
			this.syncPartnerBrandRelationship( model, userProfile );
			this.syncPartnerEmployeeRelationship( model, userProfile );
			this.syncPartnerSellerRelationship( model, userProfile );

			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PARTNER_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do parceiro: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Partner.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca parceiros que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto parceiros para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id");
	 */
	@Override
	public List<Partner> find( Partner model, Pageable pageable ) throws AppException, BusException {
		try {
			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.find(model, pageable);

		} catch (Exception e) {
			log.error( "Erro no processo de buscar os parceiros.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Partner.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca parceiros que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto parceiros para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id");
	 */
	@Override
	public List<Partner> search( Partner model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.search(model, pageable);

		} catch (Exception e) {
			log.error("Erro no processo de procurar os parceiros.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Partner.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public List<Partner> searchForm(String searchText, Pageable pageable) throws AppException {

		try {
			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.searchForm(searchText, pageable);

		} catch (Exception e) {
			log.error("Erro no processo de procurar os parceiros.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Partner.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Busca parceiros que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * <p>
	 * Esse é método é uma sobrecarga de {@link #search(Partner, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id")
	 *
	 * @param model objeto parceiro para ser buscado
	 */
	@Override
	public Optional<Partner> find(Partner model) throws AppException, BusException {
		List<Partner> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca parceiros que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Partner, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id")
	 * 
	 * @param model objeto parceiro para ser buscado
	 */
	@Override
	public List<Partner> search(Partner model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca um parceiro pelo seu ID
	 * 
	 * @param id ID do parceiro
	 */
	@Override
	public Optional<Partner> getById(Integer id) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}

			Optional<Partner> partner =  this.dao.getById( id );
			if(partner.isPresent()) {
				partner.get().setPerson(this.personService.getById(partner.get().getPerson().getId()).get());
				partner.get().setEmployeeList(this.partnerPersonService.findPartnerPerson(new PartnerPerson(partner.get())));
				partner.get().setBrandList(this.partnerBrandService.findByPartner(partner.get().getId()));
				partner.get().setSellerList(this.sellerPartnerService.findByPartner( id ));
			}

			return partner;
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um parceiro pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Partner.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos os parceiros.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id");
	 */
	@Override
	public List<Partner> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um parceiro
	 * 
	 * @param id ID do parceiro
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Partner> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O parceiro a ser excluído não existe.");
			}

			// REGRA: PTN-D1
			this.partnerPersonService.delete(id, null, userProfile);
			
			// REGRA: PTN-D2
			this.partnerBrandService.delete(id, null, userProfile);
			
			// REGRA: PTN-D3
			this.validatePriceListRelationship(id);
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PARTNER_DELETED, userProfile);
			
			this.dao.delete( id );
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do parceiro.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Partner.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(Partner model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Função que sincroniza as pessas ( funcionários ) que são do parceiro, essa função executa 2 operações
	 * 
	 * 1 - Excluir relacionamento com pessoa, quando a lista do parceiro não contém mais as pessoas ( ID ) na lista de 'employees'
	 * 2 - Insere relacionamento com pessoa, quando a lista do parceiro contém novas pessoas que ainda não existem salvas no relacionamento
	 * 
	 * @param model			objeto do parceiro que vamos usar na sincronização
	 * @param userProfile 
	 * @throws AppException
	 * @throws BusException
	 */
	private void syncPartnerEmployeeRelationship( Partner model, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {
				List<PartnerPerson> existsPersons = this.partnerPersonService.findPartnerPerson(new PartnerPerson(model));
				List<PartnerPerson> persons = model.getEmployeeList();
				
				if( persons == null ) persons = new ArrayList<>();
				if( existsPersons == null ) existsPersons = new ArrayList<>();
				
				List<PartnerPerson> toDelete = new ArrayList<>( existsPersons );
				toDelete.removeAll( persons );
	
				List<PartnerPerson> toInsert = new ArrayList<>( persons );
				toInsert.removeAll( existsPersons );
				
				List<PartnerPerson> toUpdate = new ArrayList<>( existsPersons );
				toUpdate.removeAll( toInsert );
				toUpdate.removeAll( toDelete );
				
				for( PartnerPerson entity : toDelete ) {
					this.partnerPersonService.delete( model.getId(), entity.getPerson().getId() , userProfile);
				}
				
				for( PartnerPerson entity : toInsert ) {
					this.partnerPersonService.save( entity, userProfile );
				}
				
				for( PartnerPerson entity : toUpdate ) {
					int index = persons.indexOf(entity);
					if(index  >= 0) {
						entity = persons.get(index);
					}
					this.partnerPersonService.update( entity, userProfile );
				}
			}
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar o pessoas e parceiro.", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale())); 
		}
	}

	/**
	 * Função que sincroniza as marcas que são do parceiro, essa função executa 2 operações
	 * 
	 * 1 - Excluir relacionamento com marca, quando a lista do parceiro não contém mais as marcas ( ID ) na lista
	 * 2 - Insere relacionamento com marca, quando a lista do parceiro contém novas marcas que ainda não existem salvas no relacionamento
	 * 
	 * @param model			objeto do parceiro que vamos usar na sincronização
	 * @param userProfile 
	 * @throws AppException
	 * @throws BusException
	 */
	private void syncPartnerBrandRelationship(Partner model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
		try {
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {
				List<Brand> existsBrands = this.partnerBrandService.findByPartner( model.getId() );
				List<Brand> brands = model.getBrandList();
				
				if( brands == null ) {
					brands = new ArrayList<>();
				}
				
				if( existsBrands == null ) {
					existsBrands = new ArrayList<>();
				}
				
				List<Brand> toDelete = new ArrayList<>( existsBrands );
				toDelete.removeAll( brands );
	
				List<Brand> toInsert = new ArrayList<>( brands );
				toInsert.removeAll( existsBrands );
				
				for( Brand brand : toDelete ) {
					this.partnerBrandService.delete( model.getId(), brand.getId(), userProfile);
				}
				
				for( Brand brand : toInsert ) {
					this.partnerBrandService.save( model.getId(), brand.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar marca e parceiro.", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale())); 
		}
	}
	
	/**
	 * Função que sincroniza os executivos com parceiros, essa função executa 2 operações
	 * 
	 * 1 - Excluir relacionamento com executivos, quando a lista do parceiro não contém mais os executivos ( ID ) 
	 * 2 - Insere relacionamento com executivos, quando a lista do parceiro contém novos executivos que ainda não existem salvas no relacionamento
	 * 
	 * @param model			objeto do parceiro que vamos usar na sincronização
	 * @param userProfile 
	 * @throws AppException
	 * @throws BusException
	 */
	private void syncPartnerSellerRelationship( Partner model, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {
				List<Seller> existsSellers = this.sellerPartnerService.findByPartner( model.getId() );
				List<Seller> sellers = model.getSellerList();
				
				if( sellers == null ) sellers = new ArrayList<>();
				if( existsSellers == null ) existsSellers = new ArrayList<>();
				
				List<Seller> toDelete = new ArrayList<>( existsSellers );
				toDelete.removeAll( sellers );
	
				List<Seller> toInsert = new ArrayList<>( sellers );
				toInsert.removeAll( existsSellers );
				
				for( Seller entity : toDelete ) {
					this.sellerPartnerService.delete( entity.getId(), model.getId(), userProfile);
				}
				
				for( Seller entity : toInsert ) {
					this.sellerPartnerService.save( entity.getId(), model.getId(), userProfile );
				}
			}
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar o pessoas e parceiro.", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale())); 
		}
	}

	/**
	 * Validamos a pessoa associada ao parceiro, para evitar duplicidade.
	 * 
	 * REGRA: PTN-I4, PTN-U4
	 */
	private void validateHasDuplicate( Partner model ) throws BusException, AppException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		Person person = model.getPerson();
		Person rnSearch = null;
		
		if( person.getClassification().equals( PersonClassification.PF ) ) {
			rnSearch = Person.builder().cpf( person.getCpf() ).build();
			
		} else if( person.getClassification().equals( PersonClassification.PJ ) ) {
			rnSearch = Person.builder().cnpj( person.getCnpj() ).build();
			
		} else if( person.getClassification().equals( PersonClassification.ESTRANGEIRO ) ) {
			rnSearch = Person.builder().rne( person.getRne() ).build();
		}
		
		List<Person> listBD = this.personService.find( rnSearch, null );

		// NOVA PESSOA
		if( person.getId() == null || person.getId().equals(0) ) {
			
			if( listBD != null && !listBD.isEmpty() ) {
				throw new BusException( "Já existe um parceiro com esse número de documento." );
			}
			
		} else { // PESSOA EXISTENTE
			if( listBD != null && !listBD.isEmpty() ) {
				long count = listBD.stream()
									.filter( item -> !item.getId().equals( person.getId() ) )
									.count();
				
				if( count > 0 ) {
					throw new BusException( "Já existe um parceiro com esse número de documento." );
				}
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PTN-I1, PTN-I2, PTN-I3, PTN-U1, PTN-U2, PTN-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Partner model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
		
		Person person = model.getPerson();
		String document = null;
		
		if( person.getClassification() == null ) {
			throw new BusException( "A classificação da pessoa que representa o parceiro não pode ficar sem classificação." );
		}
		
		if( person.getClassification().getValue().equals( PersonClassification.PF.name() ) ) {
			document = person.getCpf();
			
		} else if( person.getClassification().getValue().equals( PersonClassification.PJ.name() ) ) {
			document = person.getCnpj();
			
		} else if( person.getClassification().getValue().equals( PersonClassification.ESTRANGEIRO.name() ) ) {
			document = person.getRne();
		}
		
		if( person.getName() == null || person.getName().equals("")  ) {
			throw new BusException( "O nome do parceiro é obrigatório." );
		}
		
		if( document == null || document.equals("") ) {
			throw new BusException( "O número de documento do parceiro é obrigatório." );
		}
	}
	
	/**
	 * Valida o canal relacionado ao parceiro
	 * 
	 * Regra: PTN-I2, PTN-U2
	 * @throws BusException 
	 * @throws AppException 
	 * @throws NoSuchMessageException 
	 */
	private void validateChannel( Channel model ) throws BusException, NoSuchMessageException, AppException {
		try {
		
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {
				Optional<Channel> channelDB = this.channelService.getById( model.getId()  );

				if( !channelDB.isPresent() ) {
					throw new BusException( "O canal associado ao parceiro não existe." );
				}
				
			} else {
				throw new BusException( "O canal associado ao parceiro é inválido." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar o canal do parceiro.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Partner.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com lista de preço.
	 *  
	 * REGRA: PRT-D1
	 *  
	 * @param ptgId	ID do grupo que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validatePriceListRelationship(Integer prtId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( prtId != null ) {
				boolean exists = this.dao.hasPriceListRelationship( prtId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o parceiro pois existe um relacionamento com lista de preço." );
				}
				
			} else {
				throw new BusException( "ID do parceiro inválido para checar o relacionamento com lista de preço." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre parceiro e lista de preço.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
