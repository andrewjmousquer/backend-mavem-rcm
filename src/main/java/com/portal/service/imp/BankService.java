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
import com.portal.dao.IBankDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Bank;
import com.portal.model.Person;
import com.portal.service.IAuditService;
import com.portal.service.IBankService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BankService implements IBankService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IBankDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id"); 

	/**
	 * Lista todos os bancos.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id");
	 */
	@Override
	public List<Bank> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os bancos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Bank.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<Bank> saveOrUpdate(Bank model, UserProfileDTO userProfile) throws AppException, BusException {
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
	public Optional<Bank> save(Bank model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity( model, OnSave.class );
			
			this.validateHasDuplicate(model);
			
			Optional<Bank> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.BANK_INSERTED, userProfile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do banco: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Bank.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um banco
	 * 
	 * @param model objeto banco que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Bank> update(Bank model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity( model, OnUpdate.class );
			
			// BNK-U4
			Optional<Bank> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O banco a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate(model);
			
			Optional<Bank> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.BANK_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do banco: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Bank.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca bancos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto bancos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id");
	 */
	@Override
	public List<Bank> find( Bank model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar os bancos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Bank.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca bancos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto bancos para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id");
	 */
	@Override
	public List<Bank> search( Bank model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar os bancos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Bank.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca bancos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Bank, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id")
	 * 
	 * @param model objeto banco para ser buscado
	 */
	@Override
	public Optional<Bank> find(Bank model) throws AppException, BusException {
		List<Bank> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca bancos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Bank, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id")
	 * 
	 * @param model objeto banco para ser buscado
	 */
	@Override
	public List<Bank> search(Bank model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca um banco pelo seu ID
	 * 
	 * @param id ID do banco
	 */
	@Override
	public Optional<Bank> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um banco pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Bank.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos os bancos.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id");
	 */
	@Override
	public List<Bank> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um banco
	 * 
	 * @param id ID do banco
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Bank> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "O banco a ser excluído não existe.");
			}
			
			this.validateBankAccountRelationship( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.BANK_DELETED, userProfile);
			
			this.dao.delete( id );
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do banco.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Bank.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(Bank model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com conta bancária.
	 *  
	 * REGRA: BNK-D1
	 *  
	 * @param bankId		ID do banco que deve ser verificado
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateBankAccountRelationship(Integer bankId) throws AppException, BusException {
		try {
			if( bankId != null ) {
				boolean exists = this.dao.hasBankAccountRelationship( bankId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a pessoa pois existe um relacionamento com conta bancária." );
				}
				
			} else {
				throw new BusException( "ID da pessoa inválido para checar o relacionamento com conta bancária." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre pessoa e conta bancária.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: BNK-I2, BNK-U2, BNK-I4, BNK-U5
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( Bank model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		Bank rnSearchName = Bank.builder()
								.name( model.getName() )
								.build();

		Bank rnSearchCode = Bank.builder()
								.code( model.getCode() )
								.build();
		
		List<Bank> listNameBD = this.find( rnSearchName, null );
		List<Bank> listCodeBD = this.find( rnSearchCode, null );
		
		// Save Action
		if( model.getId() == null || model.getId().equals(0) ) {
			if( listNameBD != null && !listNameBD.isEmpty() ) {
				throw new BusException( "Já existe um banco com o mesmo nome.");
			}
			
			if( listCodeBD != null && !listCodeBD.isEmpty() ) {
				throw new BusException( "Já existe um banco com o mesmo código.");
			}
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) ) { 
			if( listNameBD != null && !listNameBD.isEmpty() ) {
				long count = listNameBD.stream()
								.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
								.count();
			
				if( count > 0 ) {
					throw new BusException( "Já existe um banco com o mesmo nome.");
				}
			}
			
			if( listCodeBD != null && !listCodeBD.isEmpty() ) {
				long count = listCodeBD.stream()
								.filter( item -> !item.getId().equals( model.getId() ) && item.getCode().equals( model.getCode() ) )
								.count();
			
				if( count > 0 ) {
					throw new BusException( "Já existe um banco com o mesmo código.");
				}
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: BNK-I1, BNK-I3, BNK-U1, BNK-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Bank model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
}
