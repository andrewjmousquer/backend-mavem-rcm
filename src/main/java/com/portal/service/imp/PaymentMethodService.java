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
import com.portal.dao.IPaymentMethodDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;
import com.portal.model.Person;
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
public class PaymentMethodService implements IPaymentMethodService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPaymentMethodDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private IPaymentRuleService pyrService;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pym_id"); 

	/**
	 * Lista todos os métdos de pagamento.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PaymentMethod> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<PaymentMethod> list = this.dao.listAll(pageable);
			return list;
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os métdos de pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<PaymentMethod> saveOrUpdate(PaymentMethod model, UserProfileDTO userProfile) throws AppException, BusException {
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
	 * @param profile dados do usuário logado.
	 */
	@Override
	public Optional<PaymentMethod> save(PaymentMethod model, UserProfileDTO profile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnSave.class);
			this.validateHasDuplicate(model);
			
			Optional<PaymentMethod> saved = this.dao.save(model);

			if( !saved.isPresent() ) {
				throw new BusException( "Não houve retorno ao salvar o método de pagamento, não é possível seguir." );
			}

			this.syncPaymentRules( saved.get(), profile );

			this.audit( saved.get(), AuditOperationType.PAYMENT_METHOD_INSERTED, profile);
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do método de pagamento: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um método de pagamento
	 * 
	 * @param model objeto método de pagamento que deve ser salvo.
	 * @param profile dados do usuário logado.
	 */
	@Override
	public Optional<PaymentMethod> update(PaymentMethod model, UserProfileDTO profile) throws AppException, BusException {
		try {
			this.validateEntity(model, OnUpdate.class);
			
			Optional<PaymentMethod> modelDB = this.getById( model.getId(), false );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O método de pagamento a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate(model);

			this.dao.update(model);

			this.syncPaymentRules( model, profile );

			this.audit( model, AuditOperationType.PAYMENT_METHOD_UPDATED, profile);
			
			return Optional.ofNullable(model);

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do método de pagamento: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca métdos de pagamento que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto métdos de pagamento para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PaymentMethod> find( PaymentMethod model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<PaymentMethod> list = this.dao.find( model, pageable );
			return list;

		} catch (Exception e) {
			log.error( "Erro no processo de buscar os métdos de pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca métdos de pagamento que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto métdos de pagamento para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PaymentMethod> search( PaymentMethod model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			List<PaymentMethod> list = this.dao.search( model, pageable );
			return list;
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar os métdos de pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca métdos de pagamento que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(PaymentMethod, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id")
	 * 
	 * @param model objeto método de pagamento para ser buscado
	 */
	@Override
	public Optional<PaymentMethod> find(PaymentMethod model) throws AppException, BusException {
		List<PaymentMethod> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Busca métdos de pagamento que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(PaymentMethod, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id")
	 * 
	 * @param model objeto método de pagamento para ser buscado
	 */
	@Override
	public List<PaymentMethod> search(PaymentMethod model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	@Override
	public Optional<PaymentMethod> getById(Integer id, boolean withRule) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			Optional<PaymentMethod> pym = this.dao.getById(id);

			return pym;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um método de pagamento pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca um método de pagamento pelo seu ID
	 * 
	 * @param id ID do método de pagamento
	 */
	@Override
	public Optional<PaymentMethod> getById(Integer id) throws AppException, BusException {
		return this.getById(id, true);
	}

	/**
	 * Lista todos os métdos de pagamento.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");
	 */
	@Override
	public List<PaymentMethod> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um método de pagamento
	 * 
	 * @param id ID do método de pagamento
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			// PYR-D2
			Optional<PaymentMethod> entityDB = this.getById(id, false);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O método de pagamento a ser excluído não existe.");
			}

			// PYM-D1
			this.deleteRulesRelationship(id, userProfile);
			
			// PYM-D3
			this.validateProposalRelationship( id );
			
			this.dao.delete( id );
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PAYMENT_METHOD_DELETED, userProfile);
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do método de pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	/**
	 * Exclui todos as regras relacionadas ao método de pagamento.
	 * 
	 * AVISO: foi feita em um loop para que possamos rastrear todos os elemento excluídos e para que 
	 * o delete passe pelas regras de exclusão da entidade.
	 */
	private void deleteRulesRelationship( Integer id, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			PaymentRule findByPym = PaymentRule.builder().paymentMethod( PaymentMethod.builder().id(id).build() ).build();
			List<PaymentRule> rulesDB = this.pyrService.find(findByPym, null);
			if( rulesDB != null ) {
				log.debug( "Existem {} regra(s) para serem deletadas.", rulesDB.size() );
				for (PaymentRule paymentRule : rulesDB) {
					this.pyrService.delete(paymentRule.getId(), userProfile);
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão das regras relacionadas ao método de pagamento.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PaymentMethod.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public void audit(PaymentMethod model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PYM-I1, PYM-U1 
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( PaymentMethod model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		PaymentMethod rnSearch = PaymentMethod.builder()
				.name( model.getName() )
				.build();

		List<PaymentMethod> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe um método de pagamento com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um método de pagamento com o mesmo nome.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PYM-I1, PYM-I3, PYM-U1, PYM-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PaymentMethod model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

	private void syncPaymentRules( PaymentMethod model, UserProfileDTO profile ) throws AppException, BusException {

		try {
			if( model != null && model.getId() != null && !model.getId().equals(0) ) {

				PaymentRule findByPym = PaymentRule.builder().paymentMethod( model ).build();

				List<PaymentRule> existsRules = this.pyrService.find( findByPym, DEFAULT_PAGINATION );
				List<PaymentRule> rules = this.pyrService.getListRules(model.getId());

				if( rules == null ) {
					rules = new ArrayList<>();
				}

				if( existsRules == null ) {
					existsRules = new ArrayList<>();
				}

				// Cria o diff da lista do BD x base para decobrir quais devem ser excluidas
				List<PaymentRule> toDelete = new ArrayList<>( existsRules );
				toDelete.removeAll( rules );

				// Cria o diff da lista do BD x base para decobrir quais devem ser inseridas
				List<PaymentRule> toInsert = new ArrayList<>( rules );
				toInsert.removeAll( existsRules );

				// Cria o diff da lista do BD x base para decobrir quais regras devem ser atualizadas
				List<PaymentRule> toUpdate = new ArrayList<>( existsRules );
				toUpdate.retainAll( rules );

				for( PaymentRule rule : toDelete ) {
					this.pyrService.delete( rule.getId(), profile  );
				}

				for( PaymentRule rule : toInsert ) {
					this.pyrService.save(rule, profile);
				}

				for( PaymentRule rule : toUpdate ) {
					this.pyrService.update(rule, profile);
				}
			}

		} catch (BusException e) {
			throw e;

		} catch (Exception e) {
			log.error( "Erro no processo de sincronizar as regras de pagamento.", e );
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Valida se existe algum relacionamento com a proposta.
	 *  
	 * REGRA: PYM-D3
	 *  
	 * @param pymId	ID da fonte que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validateProposalRelationship(Integer pymId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( pymId != null ) {
				boolean exists = this.dao.hasProposalRelationship( pymId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o método de pagamento pois existe um relacionamento com a proposta." );
				}
				
			} else {
				throw new BusException( "ID do método de pagamento está inválido para checar o relacionamento com a proposta." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre método de pagamento e proposta.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
