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
import com.portal.dao.IPaymentRuleDAO;
import com.portal.dto.PaymentRuleDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;
import com.portal.service.IAuditService;
import com.portal.service.IPaymentMethodService;
import com.portal.service.IPaymentRuleService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PaymentRuleService implements IPaymentRuleService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPaymentMethodService pymService;
	
	@Autowired
	private IPaymentRuleDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pyr_id"); 

	/**
	 * Lista todas as regras de um método de pagamento.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pyr_id");
	 */
	@Override
	public List<PaymentRule> listAll(Pageable pageable) throws AppException, BusException {
		try {
			
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar as regras de pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Channel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Lista todos as regras de um método de pagamento.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pyr_id");
	 */
	@Override
	public List<PaymentRule> list() throws AppException, BusException {
		return this.listAll( null );
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
	public Optional<PaymentRule> saveOrUpdate(PaymentRule model, UserProfileDTO userProfile) throws AppException, BusException {
		if (model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}

	@Override
	public Optional<PaymentRule> save(PaymentRule model, UserProfileDTO userProfile) throws AppException, BusException {
		return Optional.empty();
	}

	@Override
	public Optional<PaymentRule> update(PaymentRule model, UserProfileDTO userProfile) throws AppException, BusException {
		return Optional.empty();
	}

	/**
	 * Salva um novo objeto.
	 *
	 * @param dto         objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<PaymentRule> saveRule(PaymentRuleDTO dto, UserProfileDTO userProfile) throws AppException, BusException {
		PaymentRule model = new PaymentRule();
		try {
			Optional<PaymentMethod> paymentMethod = this.pymService.getById(dto.getPaymentMethod().getId());
			if (!paymentMethod.isPresent()) {

			}
			model = PaymentRule.toEntity(dto, new PaymentMethod());
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			this.validatePaymentMethod(model.getPaymentMethod());

			Optional<PaymentRule> saved = this.dao.save(model);

			this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.PAYMENT_RULE_INSERTED, userProfile);

			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error("Erro no processo de cadastro da regra de método de pagamento: {}", model, e);
			throw new AppException(this.messageSource.getMessage("error.generic.save", new Object[]{PaymentRule.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Atualiza uma regra
	 *
	 * @param dto         objeto método de pagamento que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<PaymentRule> updateRule(PaymentRuleDTO dto, UserProfileDTO userProfile) throws AppException, BusException {
		PaymentRule model = new PaymentRule();
		try {
			Optional<PaymentMethod> paymentMethod = this.pymService.getById(dto.getPaymentMethod().getId());
			if (!paymentMethod.isPresent()) {

			}
			model = PaymentRule.toEntity(dto, new PaymentMethod());
			this.validateEntity(model, OnUpdate.class);
			this.validateHasDuplicate(model);
			this.validatePaymentMethod(model.getPaymentMethod());

			Optional<PaymentRule> modelDB = this.getById(model.getId());
			if (!modelDB.isPresent()) {
				throw new BusException("A regra a ser atualizada não existe.");
			}

			Optional<PaymentRule> saved = this.dao.update(model);

			this.audit((saved.isPresent() ? saved.get() : null), AuditOperationType.PAYMENT_RULE_UPDATED, userProfile);

			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do método de pagamento: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PaymentRule.class.getSimpleName() }, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public List<PaymentRule> getListRules(Integer id) throws AppException {
		return this.dao.listToPaymentMethod( id);
	}


	@Override
	public List<PaymentRule> findToDto(PaymentRuleDTO dto, Pageable pageable) throws AppException, BusException {
		try {
			PaymentRule model = PaymentRule.toEntity(dto, new PaymentMethod());

			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.find(model, pageable);

		} catch (Exception e) {
			log.error("Erro no processo de buscar as regras de pagamento.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{Channel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public List<PaymentRule> find(PaymentRule model, Pageable pageable) throws AppException, BusException {
		try {

			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.find(model, pageable);

		} catch (Exception e) {
			log.error("Erro no processo de buscar as regras de pagamento.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{Channel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<PaymentRule> find(PaymentRule model) throws AppException, BusException {
		List<PaymentRule> models = this.find(model, null);
		return Optional.ofNullable((models != null ? models.get(0) : null));
	}


	@Override
	public List<PaymentRule> searchToDto(PaymentRuleDTO dto, Pageable pageable) throws AppException, BusException {
		try {
			PaymentRule model = PaymentRule.toEntity(dto, new PaymentMethod());
			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.search(model, pageable);

		} catch (Exception e) {
			log.error("Erro no processo de procurar as regras de pagamento.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.search", new Object[]{Channel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public List<PaymentRule> search(PaymentRule model, Pageable pageable) throws AppException, BusException {
		try {
			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.search(model, pageable);

		} catch (Exception e) {
			log.error( "Erro no processo de procurar as regras de pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Channel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<PaymentRule> search(PaymentRule model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma regra pelo seu ID
	 * 
	 * @param id ID do método de pagamento
	 */
	@Override
	public Optional<PaymentRule> getById(Integer id) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um método de pagamento pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PaymentRule.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Efetua a exclusão de uma regra de método de pagamento
	 * 
	 * @param id ID do método de pagamento@Override
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<PaymentRule> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A regra a ser excluída não existe.");
			}
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PAYMENT_RULE_DELETED, userProfile);
			
			this.dao.delete( id );
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão da regra do método de pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentRule.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(PaymentRule model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PYR-I1,PYR-I2,PYR-I3,PYR-I4,PYR-I5,PYR-I6,PYR-U1,PYR-U2,PYR-U3,PYR-U4,PYR-U5,PYR-U6
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PaymentRule model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	/**
	 * Valida a entidade método de pagamento.
	 * 
	 * Regra: PRY-I3, PRY-U3
	 * 
	 * @param paymentMethod	objeto do método de pagamento que será válidado.
	 */
	private void validatePaymentMethod( PaymentMethod paymentMethod ) throws AppException, BusException {
		
		if( paymentMethod == null || paymentMethod.getId() == null ) {
			throw new BusException( "O método de pagamento é o obrigatório para salvar uma regra." );
		}
		
		Optional<PaymentMethod> pymDB = this.pymService.getById( paymentMethod.getId() );
		if( !pymDB.isPresent() ) {
			throw new BusException( "O método de pagamento é inválido ou não existe." );
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PYR-I7, PYR-U7
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate(PaymentRule model) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		PaymentRule rnSearch = PaymentRule.builder()
									.name( model.getName() )
									.paymentMethod( model.getPaymentMethod() )
									.build();

		List<PaymentRule> listBD = this.find( rnSearch, DEFAULT_PAGINATION );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe a regra de pagamento com o mesmo nome para o mesmo método de pagamento.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) &&  listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && ( item.getName().equals( model.getName() ) && item.getPaymentMethod().equals( model.getPaymentMethod() ) ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe a regra de pagamento com o mesmo nome para o mesmo método de pagamento.");
			}
		}		
	}
}
