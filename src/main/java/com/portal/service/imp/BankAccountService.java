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
import com.portal.dao.IBankAccountDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Bank;
import com.portal.model.BankAccount;
import com.portal.model.Person;
import com.portal.service.IAuditService;
import com.portal.service.IBankAccountService;
import com.portal.service.IBankService;
import com.portal.service.IPersonService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BankAccountService implements IBankAccountService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IBankAccountDAO dao;
	
	@Autowired
	private IPersonService personService;
	
	@Autowired
	private IBankService bankService;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id"); 

	/**
	 * Lista todos as contas bancárias.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id");
	 */
	@Override
	public List<BankAccount> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar as contas bancárias.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { BankAccount.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<BankAccount> saveOrUpdate(BankAccount model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<BankAccount> save(BankAccount model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity( model, OnSave.class );
			this.validatePersonEntity(model);
			this.validateBankEntity(model);
			
			Optional<BankAccount> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.BANK_ACCOUNT_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro da conta bancária: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { BankAccount.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um banco
	 * 
	 * @param model objeto banco que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<BankAccount> update(BankAccount model, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.validateEntity( model, OnUpdate.class );
			
			Optional<BankAccount> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "A conta bancária a ser atualizado não existe.");
			}
			
			this.validatePersonEntity(model);
			this.validateBankEntity(model);
			
			Optional<BankAccount> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.BANK_ACCOUNT_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização da conta bancária: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { BankAccount.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca bancos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto bancos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id");
	 */
	@Override
	public List<BankAccount> find( BankAccount model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as contas bancárias.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { BankAccount.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca bancos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto bancos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id");
	 */
	@Override
	public List<BankAccount> search( BankAccount model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar as contas bancárias.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { BankAccount.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca bancos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(BankAccount, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id")
	 * 
	 * @param model objeto banco para ser buscado
	 */
	@Override
	public Optional<BankAccount> find(BankAccount model) throws AppException, BusException {
		List<BankAccount> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca bancos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(BankAccount, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id")
	 * 
	 * @param model objeto banco para ser buscado
	 */
	@Override
	public List<BankAccount> search(BankAccount model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca um banco pelo seu ID
	 * 
	 * @param id ID da conta bancária
	 */
	@Override
	public Optional<BankAccount> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar uma conta bancária pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { BankAccount.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos as contas bancárias.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id");
	 */
	@Override
	public List<BankAccount> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um banco
	 * 
	 * @param id ID da conta bancária
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<BankAccount> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A conta bancária a ser excluída não existe.");
			}
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.BANK_ACCOUNT_DELETED, userProfile);
			
			this.dao.delete( id );
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão da conta bancária.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { BankAccount.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(BankAccount model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: ACT-I1, ACT-I2, ACT-I3, ACT-U1, ACT-U2, ACT-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( BankAccount model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validatePersonEntity(BankAccount model) throws BusException, AppException {
		try {
			if( model != null  ) {
				if( model.getPerson() == null || model.getPerson().getId() == null || model.getPerson().getId().equals(0) ) {
					throw new BusException( "Não é possível salvar o relacionamento pois a pessoa relacionada é inválida ou não existe." );
				} else {
					Optional<Person> dbPriceList = this.personService.getById( model.getPerson().getId() );
					if( !dbPriceList.isPresent() ) {
						throw new BusException( "Não é possível salvar o relacionamento pois a pessoa relacionada é inválida ou não existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade pessoa relacionadas a conta bancária.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se as entidades relacionadas existem
	 * 
	 * @param model entidade para ser validada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateBankEntity(BankAccount model) throws BusException, AppException {
		try {
			if( model != null  ) {
				if( model.getBank() == null || model.getBank().getId() == null || model.getBank().getId().equals(0) ) {
					throw new BusException( "Não é possível salvar o relacionamento pois o banco relacionada é inválida ou não existe." );
				} else {
					Optional<Bank> dbPriceList = this.bankService.getById( model.getBank().getId() );
					if( !dbPriceList.isPresent() ) {
						throw new BusException( "Não é possível salvar o relacionamento pois o banco relacionada é inválida ou não existe." );	
					}
				}
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao validar a entidade banco relacionadas a conta bancária.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
