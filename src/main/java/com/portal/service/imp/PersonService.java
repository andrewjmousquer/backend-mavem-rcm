package com.portal.service.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.portal.dao.IPersonDAO;
import com.portal.dao.IPersonQualificationDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.enums.PersonClassification;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.AddressModel;
import com.portal.model.BankAccount;
import com.portal.model.Contact;
import com.portal.model.Person;
import com.portal.model.PersonQualification;
import com.portal.model.PersonRelated;
import com.portal.model.Product;
import com.portal.model.UserModel;
import com.portal.service.IAddressService;
import com.portal.service.IAuditService;
import com.portal.service.IBankAccountService;
import com.portal.service.IContactService;
import com.portal.service.IPersonQualificationService;
import com.portal.service.IPersonRelatedService;
import com.portal.service.IPersonService;
import com.portal.service.IQualificationService;
import com.portal.service.IUserService;
import com.portal.utils.PortalStringUtils;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PersonService implements IPersonService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPersonDAO dao;

	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private IContactService contactService;
	
	@Autowired
	private IAddressService addressService;

	@Autowired
	private IBankAccountService bankAccountService;
	
	@Autowired
	private IPersonQualificationService personQualificationService;
	
	@Autowired
	private IPersonRelatedService personRelatedService;
	
	
	@Autowired
	private IUserService userService;
	
    @Autowired
    public MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;
    
    private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "per_id");
    
    /**
	 * Lista todos as pessoas.
	 * Nesse método carregamos por padrão a lista de tipos da pessoa, isso por que é um relacionamento obrigatório.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "per_id");
	 */
	@Override
	public List<Person> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {
			
			List<Person> listPerson = dao.listAll( pageable );
			
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}

			listPerson.forEach(person ->{
				try {
					person.setContacts(this.contactService.findByPerson(person.getId()));
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
			
			return listPerson;
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar as pessoas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
    
	/**
	 * Lista todos as pessoas.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "per_id");
	 */
	@Override
	public List<Person> list() throws AppException, BusException {
		
		List<Person> listPerson = this.listAll( null );
		
		listPerson.forEach(person ->{
			try {
				person.setContacts(this.contactService.findByPerson(person.getId()));
				person.setPersonRelated(this.personRelatedService.findByPerson(person.getId()));
			} catch (AppException | BusException e) {
				e.printStackTrace();
			}
		});


		
		return listPerson;
	}
    
	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Nesse método carregamos por padrão a lista de tipos da pessoa, isso por que é um relacionamento obrigatório.
	 * 
	 * @param model objeto produtos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "per_id");
	 */
	@Override
	public List<Person> find( Person model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );

		} catch (Exception e) {
			log.error( "Erro no processo de buscar as pessoas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
    
	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Person, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "per_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public Optional<Person> find(Person model) throws AppException, BusException {
		List<Person> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}
	
	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Nesse método carregamos por padrão a lista de tipos da pessoa, isso por que é um relacionamento obrigatório.
	 * 
	 * @param model objeto produtos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "per_id");
	 */
	@Override
	public List<Person> search( Person model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<Person> persons = this.dao.search( model, pageable );
			fillContact( persons );
			
			return persons;
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de procurar as pessoas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca produtos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Person, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "per_id")
	 * 
	 * @param model objeto produto para ser buscado
	 */
	@Override
	public List<Person> search(Person model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma pessoa pelo seu ID
	 * 
	 * Nesse método carregamos por padrão a lista de tipos da pessoa, isso por que é um relacionamento obrigatório.
	 * 
	 * LEGADO: Para manter a compatibilidade esse método mantém o carregamento de contato.
	 * 
	 * @param id ID do produto
	 */
	@Override
	public Optional<Person> getById(Integer id) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
		
			Optional<Person> person = dao.getById(id);
			if( person.isPresent() ) {
				this.fillContact(person.get());
				this.fillQualification(person.get());
				this.fillBankAccount(person.get());
				this.fillAddress(person.get());
				this.fillPersonRelated(person.get());
			}
			
			return person;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar uma pessoa pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<Person> saveOrUpdate(Person model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model.getId() != null && model.getId() > 0) {
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
	public Optional<Person> save(Person model, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			this.validateEntity( model, OnSave.class );
			
			if(model.getAddress() != null) {
				model.setAddress(this.addressService.save(model.getAddress(), userProfile).get());
				this.validateAddress( model );
			}

			// Para manter a compatibiliade setamos por padrão PF
			// REGRAS: PER-I2
			if( model.getClassification() == null ) {
				model.setClassification( PersonClassification.PF.getType() );
			}
			
			Optional<Person> saved = this.dao.save( model );
			
			if( !saved.isPresent() ) {
				throw new BusException( "Não houve retorno do ID da nova pessoa, impedindo de continuar." );
			}else {
				
				if(model.getContacts() != null) {
					this.syncContacts(model, userProfile);
				}
				
				if(model.getBankAccount() != null) {
					this.syncBankAccount(model, userProfile);
				}
				
				if(model.getPersonRelated() != null) {
					this.syncPersonRelated(model, userProfile);
				}
				
                if(model.getQualifications() != null) {   
                    this.syncQualification(model, userProfile);
                }
			}
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PERSON_INSERTED, userProfile);
			
			
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do pessoa: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza uma pessoa
	 * 
	 * @param model objeto produto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Person> update(Person model, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			
			this.validateEntity(model, OnUpdate.class);

			if(model.getAddress() != null) {
				this.addressService.saveOrUpdate(model.getAddress(), userProfile);
				this.validateAddress( model );
			}
			
			// Para manter a compatibiliade setamos por padrão PF
			// REGRAS: PER-U2
			if( model.getClassification() == null ) {
				model.setClassification( PersonClassification.PF.getType() );
			}
			
			// PER-U4
			Optional<Person> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "A pessoa a ser atualizada não existe.");
			}
			
			Optional<Person> saved = this.dao.update(model);
			
			if( !saved.isPresent() ) {
				throw new BusException( "Não houve retorno do ID da pessoa atualizada, impedindo de continuar." );
			}else {
				
				if(model.getContacts() != null) {
					this.syncContacts(model, userProfile);
				}
				
				if(model.getBankAccount() != null) {
					this.syncBankAccount(model, userProfile);
				}
				
				if(model.getPersonRelated() != null) {
					this.syncPersonRelated(model, userProfile);
				}
				
                if(model.getQualifications() != null) {   
                    this.syncQualification(model, userProfile);
                }
				
			
			}

			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PERSON_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do pessoa: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Efetua a exclusão de uma pessoa
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
			
			Optional<Person> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A pessoa a ser excluída não existe.");
			}

			this.validatePartnerRelationship(id);
			this.validatePartnerPersonRelationship(id);
			this.validateProposalRelationship(id);
			this.validateProposalDetailRelationship(id);
			this.validateCommissionRelationship(id);
			this.validateLeadRelationship(id);
			this.validateHoldingRelationship(id);
			this.validateUserRelationship(id);
			this.validateSellerRelationship(id);
			
			// REGRA: PER-D10
			List<Contact> contactsDB = this.contactService.findByPerson(id);
			if( contactsDB != null ) {
				for( Contact contact : contactsDB ) {
					this.contactService.delete(contact.getId(), userProfile);
				}
			}
			
			// REGRA: PER-D09
			List<BankAccount> banckAccountDB = this.bankAccountService.find( BankAccount.builder().person( Person.builder().id(id).build() ).build() , null);
			if( banckAccountDB != null ) {
				for( BankAccount bankAccount : banckAccountDB ) {
					this.bankAccountService.delete(bankAccount.getId(), userProfile);
				}
			}
			
			// REGRA: PER-D12
			this.personQualificationService.deleteByPerson(id);
			
			// REGRA: PER-D13
			List<PersonRelated> personRelatedDB = this.personRelatedService.find( PersonRelated.builder().person( Person.builder().id(id).build() ).build() , null);
			if( personRelatedDB != null ) {
				for( PersonRelated personRelated : personRelatedDB ) {
					this.personRelatedService.delete(personRelated.getId(), userProfile);
				}
			}
			
			this.dao.delete( id );
			
			// REGRA: PER-D13
			AddressModel address = entityDB.get().getAddress();
			if( address != null && address.getId() != null  ) {
				Optional<AddressModel> addressDB = this.addressService.getById( address.getId() );
				if( addressDB.isPresent() ) {
					this.addressService.delete(addressDB.get().getId(), userProfile);
				}
			}
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PERSON_DELETED, userProfile);

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do produto.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Product.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(Person model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
    
	public List<Person> fillContact(List<Person> persons) throws AppException, BusException {
		if( persons != null && !persons.isEmpty() ) {			
			persons.forEach(person -> {
			    try {
					this.fillContact( person );
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		
		return persons;
	}
	
	public Person fillContact(Person person) throws AppException, BusException {
		if( person != null ) {
			person.setContacts( this.contactService.findByPerson( person.getId() ) );
		}
		return person;
	}

	/**
	 * Carrega qualificações associadas as entidades.
	 * Vamos usar as mesmas entidades do parâmetro para preencher a lista.
	 */
	@Override
	public void fillQualification(List<Person> persons) throws AppException, BusException {
		try {
			
			if( persons != null && !persons.isEmpty() ) {
				for( Person person : persons ) {
					 this.fillQualification( person );
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e qualificação.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Carrega qualificações associados a entidade.
	 * Vamos usar a mesma entidade do parâmetro para preencher lista.
	 */
	@Override
	public void fillQualification(Person person) throws AppException, BusException {
		try {
			
			if( person != null && person.getId() != null ) {
				person.setQualifications( this.personQualificationService.findByPerson( person.getId() ) );
			}
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e qualificação.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Carrega as contas bancárias associadas a entidade.
	 * Vamos usar a mesma entidade do parâmetro para preencher lista.
	 */
	@Override
	public void fillBankAccount(Person person) throws AppException, BusException {
		try {
			
			if( person != null && person.getId() != null ) {
				person.setBankAccount( this.bankAccountService.find( BankAccount.builder().person(person).build(), DEFAULT_PAGINATION ) );   
			}
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e conta bancária.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Carrega contas bancárias associada as entidades.
	 * Vamos usar as mesmas entidades do parâmetro para preencher a lista.
	 */
	@Override
	public void fillBankAccount(List<Person> persons) throws AppException, BusException {
		try {
			
			if( persons != null && !persons.isEmpty() ) {
				for( Person person : persons ) {
					 this.fillBankAccount(person);
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e conta bancária.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Carrega as enderecos associadas a entidade.
	 * Vamos usar a mesma entidade do parâmetro para preencher lista.
	 */
	@Override
	public void fillAddress(Person person) throws AppException, BusException {
		try {
			
			if( person != null && person.getId() != null ) {
				if(person.getAddress() != null) {
					if(person.getAddress().getId() != 0) {
						
						person.setAddress( this.addressService.getById( person.getAddress().getId() ).get() );
					}
				}
			}
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e endereco.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Carrega enderecos associada as entidades.
	 * Vamos usar as mesmas entidades do parâmetro para preencher a lista.
	 */
	@Override
	public void fillAddress(List<Person> persons) throws AppException, BusException {
		try {
			
			if( persons != null && !persons.isEmpty() ) {
				for( Person person : persons ) {
					 this.fillAddress(person);
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e endereco.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Carrega as pessoas relacionadas associadas a entidade.
	 * Vamos usar a mesma entidade do parâmetro para preencher lista.
	 */
	@Override
	public void fillPersonRelated(Person person) throws AppException, BusException {
		try {
			
			if( person != null && person.getId() != null ) {
				person.setPersonRelated( this.personRelatedService.search( PersonRelated.builder().person(new Person(person.getId())).build() ));
			}
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e endereco.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Carrega pessoas relacionadas associada as entidades.
	 * Vamos usar as mesmas entidades do parâmetro para preencher a lista.
	 */
	@Override
	public void fillPersonRelated(List<Person> persons) throws AppException, BusException {
		try {
			
			if( persons != null && !persons.isEmpty() ) {
				for( Person person : persons ) {
					 this.fillPersonRelated(person);
				}
			}

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error("Erro ao carregar o relacionamento entre pessoa e endereco.", e);
			throw new AppException(this.messageSource.getMessage("error.generic", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}
	
	private void syncContacts(Person model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
		try {
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {
				List<Contact> existsContact = this.contactService.findByPerson( model.getId() );
				List<Contact> contacts = model.getContacts() != null && model.getContacts().size() > 0 ? model.getContacts() : null;
				
				if( contacts == null ) contacts = new ArrayList<>();
				if( existsContact == null ) existsContact = new ArrayList<>();
				
				List<Contact> toDelete = new ArrayList<>( existsContact );
				toDelete.removeAll( contacts );
	
				List<Contact> toInsert = new ArrayList<>( contacts );
				toInsert.removeAll( existsContact );
				
				List<Contact> toUpdate = new ArrayList<>( existsContact );
				toUpdate.removeAll( toInsert );
				toUpdate.removeAll( toDelete );
								
				for( Contact modelDelete : toDelete ) {
					modelDelete.setPerson(new Person(model.getId()));
					this.contactService.delete( modelDelete.getId(), userProfile);
				}
				
				for( Contact modelInsert : toInsert ) {
					modelInsert.setPerson(new Person(model.getId()));
					this.contactService.save( modelInsert, userProfile);
				}
								
				for( Contact entity : toUpdate ) {
					int index = contacts.indexOf(entity);
					if(index  >= 0) {
						entity = contacts.get(index);
					}
					
					entity.setPerson(new Person(model.getId()));
					
					this.contactService.update( entity, userProfile );
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar marca e parceiro.", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale())); 
		}
	}
	
	private void syncBankAccount(Person model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
		try {
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {
				List<BankAccount> existsBanks = this.bankAccountService.find( BankAccount.builder().person(new Person(model.getId())).build(), DEFAULT_PAGINATION );
				List<BankAccount> banks = model.getBankAccount() != null && model.getBankAccount().size() > 0 ? model.getBankAccount() : null;
				
				if( banks == null ) banks = new ArrayList<>();
				if( existsBanks == null ) existsBanks = new ArrayList<>();
				
				List<BankAccount> toDelete = new ArrayList<>( existsBanks );
				toDelete.removeAll( banks );
	
				List<BankAccount> toInsert = new ArrayList<>( banks );
				toInsert.removeAll( existsBanks );
				
				List<BankAccount> toUpdate = new ArrayList<>( existsBanks );
				toUpdate.removeAll( toInsert );
				toUpdate.removeAll( toDelete );
								
				for( BankAccount modelDelete : toDelete ) {
					modelDelete.setPerson(new Person(model.getId()));
					this.bankAccountService.delete( modelDelete.getId(), userProfile);
				}
				
				for( BankAccount modelInsert : toInsert ) {
					modelInsert.setPerson(new Person(model.getId()));
					this.bankAccountService.save( modelInsert, userProfile);
				}
								
				for( BankAccount entity : toUpdate ) {
					int index = banks.indexOf(entity);
					if(index  >= 0) {
						entity = banks.get(index);
					}
					entity.setPerson(new Person(model.getId()));
					this.bankAccountService.update( entity, userProfile );
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar marca e parceiro.", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale())); 
		}
	}
	
	private void syncQualification(Person model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
        try {
            if( model != null && model.getId() != null && !model.getId().equals(0) ) {
                List<PersonQualification> existsQualification = Optional.ofNullable(this.personQualificationService.find( PersonQualification.builder().person(new Person(model.getId())).build())).orElse(new ArrayList<PersonQualification>());
                List<PersonQualification> qualification = Optional.ofNullable(model.getQualifications()).orElse(new ArrayList<PersonQualification>()).stream() //
                        .map(personQualification -> { //
                            personQualification.setPerson(model);
                            return personQualification;
                        }) //
                        .collect(Collectors.toList());
                
                List<PersonQualification> toDelete = new ArrayList<>( existsQualification );
                toDelete.removeAll( qualification );
    
                List<PersonQualification> toInsert = new ArrayList<>( qualification );
                toInsert.removeAll( existsQualification );
                
                List<PersonQualification> toUpdate = new ArrayList<>( existsQualification );
                toUpdate.removeAll( toInsert );
                toUpdate.removeAll( toDelete );
                                
                for( PersonQualification modelDelete : toDelete ) {
                    this.personQualificationService.delete(modelDelete.getPerson().getId(), modelDelete.getQualification().getId());
//                    this.personQualificationService.deleteByPerson(modelDelete, userProfile);
                }
                
                for( PersonQualification modelInsert : toInsert ) {
                    this.personQualificationService.save(modelInsert);
                }
                                
                for( PersonQualification entity : toUpdate ) {
                    int index = qualification.indexOf(entity);
                    if(index  >= 0) {
                        entity = qualification.get(index);
                    }                    
                    this.personQualificationService.save(entity);
//                    this.personQualificationService.update( entity, userProfile );
                }
            }
            
        } catch (BusException e) {
            throw e;
        } catch (Exception e) {
            log.error( "Erro no processo de sincronizar marca e parceiro.", e );
            throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale())); 
        }
    }
	
	private void syncPersonRelated(Person model, UserProfileDTO userProfile) throws BusException, NoSuchMessageException, AppException {
		try {
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {
				List<PersonRelated> existsPerson = this.personRelatedService.findByPerson(model.getId());
				List<PersonRelated> persons = model.getBankAccount() != null && model.getPersonRelated().size() > 0 ? model.getPersonRelated() : null;
				
				if( persons == null ) persons = new ArrayList<>();
				if( existsPerson == null ) existsPerson = new ArrayList<>();
				
				List<PersonRelated> toDelete = new ArrayList<>( existsPerson );
				toDelete.removeAll( persons );
	
				List<PersonRelated> toInsert = new ArrayList<>( persons );
				toInsert.removeAll( existsPerson );
								
				for( PersonRelated modelDelete : toDelete ) {
					modelDelete.setPerson(new Person(model.getId()));
					this.personRelatedService.delete( modelDelete.getId(), userProfile);
				}
				
				for( PersonRelated modelInsert : toInsert ) {
					modelInsert.setPerson(new Person(model.getId()));
					this.personRelatedService.saveOrUpdate( modelInsert, userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar marca e parceiro.", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale())); 
		}
	}
	
	
	@Override
	public List<Person> searchForm(String searchText, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<Person> listPerson = dao.searchForm( searchText, pageable );
			
			listPerson.forEach(person ->{
				try {
					person.setContacts(this.contactService.findByPerson(person.getId()));
				} catch (AppException | BusException e) {
					e.printStackTrace();
				}
			});
			
			return listPerson;

		} catch (Exception e) {
			log.error("Erro no processo de procurar pessoas.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<Person> searchByDocument(String document) throws AppException {
		try {
			Optional<Person> person = this.dao.searchByDocument(document);
			if(person != null && person.isPresent()) {
				person = this.getById(person.get().getId());
			
				if(person.isPresent()) {
					Optional<UserModel> user = this.userService.find(new UserModel(new Person(person.get().getId())));
					if(user.isPresent()) {
						person.get().setUser(true);
					}
				}
			}

			return person;
		} catch (Exception e) {
			log.error("Erro no processo de procurar pessoas.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public List<Person> searchByContact(String contact) throws AppException {
		try {
			List<Person> listPerson = dao.findByContact(contact);
			if(listPerson != null && !listPerson.isEmpty()) {
				listPerson.forEach(person ->{
					try {
						person.setContacts(this.contactService.findByPerson(person.getId()));
					} catch (AppException | BusException e) {
						e.printStackTrace();
					}
				});
			}
			return listPerson;		
		} catch (Exception e) {
			log.error("Erro no processo de procurar pessoas.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Person.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Person fillEntity(Person entity) throws AppException, BusException {
		fillContact(entity);
		fillBankAccount(entity);
		fillPersonRelated(entity);
		fillQualification(entity);
		fillAddress(entity);
		return entity;
	}

	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 *
	 * Regra: PER-I1, PER-I2, PER-U1, PER-U2
	 *
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Person model, Class<?> group ) throws AppException, BusException {
		//VERIFICAR SE EXISTE UMA ABORDAGEM MAIS BONITA PARA ESSAS NORMALIZAÇÕES
		if(model.getCpf() != null && !model.getCpf().equals("")) {
			model.setCpf(PortalStringUtils.extractOnlyNumber(model.getCpf()));
		}
		
		if(model.getCnpj() != null && !model.getCnpj().equals("")) {
			model.setCnpj(PortalStringUtils.extractOnlyNumber(model.getCnpj()));
		}
		
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

	/**
	 * Valida se existe algum relacionamento com parceiro.
	 *  
	 * REGRA: PER-D1
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validatePartnerRelationship( Integer perId ) throws AppException, BusException {
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasPartnerRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com parceiro." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com parceiro." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e parceiro. [partner]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com pessoas do parceiro.
	 *  
	 * REGRA: PER-D2
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validatePartnerPersonRelationship( Integer perId ) throws AppException, BusException {
	
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasPartnerPersonRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com pessoas do parceiro." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com pessoas do parceiro." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e parceiro. [partner_person]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
		
	}
	
	/**
	 * Valida se existe algum relacionamento com a proposta.
	 *  
	 * REGRA: PER-D3
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateProposalRelationship( Integer perId ) throws AppException, BusException {
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasProposalRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com a proposta." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com a proposta." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa a proposta. [proposal_person_client]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com detalhes a proposta.
	 *  
	 * REGRA: PER-D4
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateProposalDetailRelationship( Integer perId ) throws AppException, BusException {
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasProposalRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com os detalhes da proposta." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com os detalhes da proposta." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e detahes da proposta. [proposal_detail]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com comissões.
	 *  
	 * REGRA: PER-D5
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateCommissionRelationship( Integer perId ) throws AppException, BusException {
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasCommissionRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com comissões." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com as comissões." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e detahes da proposta. [proposal_commission]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com lead.
	 *  
	 * REGRA: PER-D6
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateLeadRelationship( Integer perId ) throws AppException, BusException {
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasCommissionRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com o lead." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com o lead." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e lead. [lead]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com conglomerado.
	 *  
	 * REGRA: PER-D7
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateHoldingRelationship( Integer perId ) throws AppException, BusException {
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasHoldingRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com o conglomerado." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com o lead." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e conglomerado. [holding]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com usuário.
	 *  
	 * REGRA: PER-D8
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateUserRelationship( Integer perId ) throws AppException, BusException {
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasUserRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com usuário." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com usuário." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e usuário. [user]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se o endereço informado existe.
	 */
	private void validateAddress(Person model) throws AppException, BusException {
		try {
			if( model != null && model.getAddress() != null && model.getAddress().getId() != null ) {
				Optional<AddressModel> address = addressService.getById( model.getAddress().getId() );
				if( !address.isPresent() ) {
					throw new BusException( "O endereço informado não existe ou está inválido." );
				}
			}
			
		} catch (BusException e) {
			throw e;
		
		} catch (Exception e) {
			log.error( "Erro ao validar a classificação da pessoa.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com seller.
	 *  
	 * REGRA: PER-D8
	 *  
	 * @param perId	ID da pessoa que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateSellerRelationship( Integer perId ) throws AppException, BusException {
		try {
			if( perId != null ) {
				boolean exists = this.dao.hasUserRelationship( perId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com usuário." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com usuário." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e usuário. [user]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}