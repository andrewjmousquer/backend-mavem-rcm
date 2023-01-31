package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

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
import com.portal.dao.IModelItemCostDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Channel;
import com.portal.model.ModelItemCost;
import com.portal.service.IAuditService;
import com.portal.service.IModelItemCostService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ModelItemCostService implements IModelItemCostService {

	@Autowired
	private IModelItemCostDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mic_id"); 

	/**
	 * Lista todos os custos por modelo e item.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mic_id");
	 */
	@Override
	public List<ModelItemCost> listAll(Pageable pageable) throws AppException, BusException {
		try {
			
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar custo por modelo e item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Channel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca um custo de modelo e item pelo seu ID
	 * 
	 * @param id ID do custo por modelo e item
	 */
	@Override
	public Optional<ModelItemCost> getById(Integer id) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar custo por modelo e item pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { ModelItemCost.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Salva um novo custo de modelo e item
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ModelItemCost> save(ModelItemCost model, UserProfileDTO userProfile) throws AppException, BusException {
		this.validateHasDuplicate(model);
		Optional<ModelItemCost> savedItem = this.dao.save(model);
		if(savedItem.isPresent()) {
			this.audit( ( savedItem.isPresent() ? savedItem.get() : null ), AuditOperationType.MODEL_ITEM_COST_INSERTED, userProfile);
		}
		return savedItem;
	}
	
	/**
	 * Salva a edição de um custo de modelo e item
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<ModelItemCost> update(ModelItemCost model, UserProfileDTO userProfile) throws AppException, BusException {
		this.validateHasDuplicate(model);
		Optional<ModelItemCost> savedItem = this.dao.update(model);
		if(savedItem.isPresent()) {
			this.audit( ( savedItem.isPresent() ? savedItem.get() : null ), AuditOperationType.MODEL_ITEM_COST_UPDATED, userProfile);
		}
		return savedItem;
	}
	
	/**
	 * Efetua a exclusão de um Custo de Modelo e Item
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
			Optional<ModelItemCost> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "O Custo de Modelo e Item a ser excluído não existe.");
			}
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.MODEL_ITEM_COST_DELETED, userProfile);
			this.dao.delete( id );			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do Custo de Modelo e Item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { ModelItemCost.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
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
	public Optional<ModelItemCost> saveOrUpdate(ModelItemCost model, UserProfileDTO userProfile) throws AppException, BusException {
		this.validateHasDuplicate(model);
		if (model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	/**
	 * Lista todos as regras de um custo por modelo e item.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mic_id");
	 */
	@Override
	public List<ModelItemCost> list() throws AppException, BusException {
		return this.dao.list();
	}

	@Override
	public List<ModelItemCost> find(ModelItemCost model, Pageable pageable) throws AppException, BusException {
		try {

			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.find(model, pageable);

		} catch (Exception e) {
			log.error("Erro no processo de buscar custo por modelo e item.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.find", new Object[]{Channel.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<ModelItemCost> find(ModelItemCost model) throws AppException, BusException {
		List<ModelItemCost> models = this.find(model, null);
		return Optional.ofNullable((models != null ? models.get(0) : null));
	}

	@Override
	public List<ModelItemCost> search(ModelItemCost model, Pageable pageable) throws AppException, BusException {
		try {
			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.find(model, pageable);

		} catch (Exception e) {
			log.error( "Erro no processo de procurar custo por modelo e item.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Channel.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<ModelItemCost> search(ModelItemCost model) throws AppException, BusException {
		return this.search( model, null );
	}

	@Override
	public void audit(ModelItemCost model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	public void validateHasDuplicate(ModelItemCost model) throws AppException, BusException {
        if (model == null) {
            throw new BusException("Não é possível checar a duplicidade com o objeto da entidade nulo.");
        }

        if (this.dao.hasDuplicate(model)) {
            throw new BusException("Já existe um Custo para este Item e Modelo com período conflitante.");
        }
    }

}
