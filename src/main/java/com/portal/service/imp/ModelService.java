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
import com.portal.dao.IBrandDAO;
import com.portal.dao.IModelDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.model.Model;
import com.portal.service.IAuditService;
import com.portal.service.IModelService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ModelService implements IModelService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IModelDAO dao;
	
	@Autowired
	private IBrandDAO brandDAO;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mdl_id"); 

	/**
	 * Lista todos os modelos.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mdl_id");
	 */
	@Override
	public List<Model> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {
			
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.listAll(pageable);

		} catch (Exception e) {
			log.error("Erro no processo de listar os modelos.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{Model.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Lista todos os modelos de uma determinada marca.
	 *
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mdl_id");
	 */
	@Override
	public List<Model> listAllByBrand(int id, Pageable pageable) throws AppException {
		try {

			if (pageable == null) {
				pageable = DEFAULT_PAGINATION;
			}

			return this.dao.listAllByBrand(id, pageable);

		} catch (Exception e) {
			log.error("Erro no processo de listar os modelos.", e);
			throw new AppException(this.messageSource.getMessage("error.generic.listall", new Object[]{Model.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		}
	}

	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 *
	 * @param model       objeto modelo que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Model> saveOrUpdate(Model model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	/**
	 * Salva um novo modelo.
	 * 
	 * @param model objeto modelo que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Model> save(Model model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnSave.class);
			this.validateBrandRelationship(model);
			this.validateHasDuplicate(model);
			
			Optional<Model> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.MODEL_INSERTED, userProfile);
			
			return saved;
			
		} catch ( BusException e ) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Model.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um modelo
	 * 
	 * @param model objeto modelo que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Model> update(Model model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity(model, OnUpdate.class);
			
			this.validateBrandRelationship(model);			
			
			// MDL-U5
			Optional<Model> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O modelo a ser atualizado não existe.");
			}

			this.validateHasDuplicate(model);
			
			Optional<Model> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.MODEL_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do modelo: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Model.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<Model> find( Model model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar os modelos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Model.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<Model> search( Model model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de procurar os modelos.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Model.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}



	@Override
	public Optional<Model> find(Model model) throws AppException, BusException {
		List<Model> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	@Override
	public List<Model> search(Model model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca um modelo pelo seu ID
	 * 
	 * @param id ID do modelo
	 */
	@Override
	public Optional<Model> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um modelo pelo ID: {}", id, e );
			throw new AppException( "Erro ao consultar um modelo pelo ID.");
		}
	}

	@Override
	public List<Model> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um modelo.
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Model> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "O modelo a ser excluído não existe.");
			}
			
			this.validateLeadRelationship(id);
			this.validateItemRelationship(id);
			this.validateVehicleRelationship(id);
			this.validateProductRelationship(id);
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.MODEL_DELETED, userProfile);
			
			this.dao.delete( id );
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do modelo.", e );
			throw new AppException( "Erro no processo de exclusão do modelo.");
		}	
	}

	@Override
	public void audit(Model model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: MDL-I2, MDL-U2
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( Model model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		Model rnSearch = Model.builder()
				.name( model.getName() )
				.brand( model.getBrand() )
				.build();

		List<Model> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe um modelo com o mesmo nome e fabricante.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um modelo com o mesmo nome e fabricante.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: MDL-I1, MDL-I3, MDL-I4, MDL-U1, MDL-U3, MDL-U4
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Model model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}

	/**
	 * Valida se o objeto relacionado e obrigatório é válido.
	 * 
	 * Regra: MDL-I4, MDL-U4
	 * 
	 * @param model entidade a ser validada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateBrandRelationship( Model model ) throws AppException, BusException {
		Optional<Brand> brandBD = Optional.empty();
		if( model.getBrand() != null && model.getBrand().getId() != null ) {
			brandBD = brandDAO.getById( model.getBrand().getId() );
		}
			
		if( !brandBD.isPresent() ) {
			throw new BusException( "O fabricante associado é inválido ou não existe." );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com lead.
	 *  
	 * REGRA: MDL-D1
	 *  
	 * @param modelId	ID do modelo que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateLeadRelationship( Integer modelId ) throws AppException, BusException {
		try {
			if( modelId != null ) {
				boolean exists = this.dao.hasLeadRelationship( modelId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o modelo pois existe um relacionamento com lead." );
				}
				
			} else {
				throw new BusException( "ID do modelo inválido para checar o relacionamento com lead." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre modelo e lead. [lead]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com item.
	 *  
	 * REGRA: MDL-D2
	 *  
	 * @param modelId	ID do modelo que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateItemRelationship( Integer modelId ) throws AppException, BusException {
		try {
			if( modelId != null ) {
				boolean exists = this.dao.hasItemRelationship( modelId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o modelo pois existe um relacionamento com item." );
				}
				
			} else {
				throw new BusException( "ID do modelo inválido para checar o relacionamento com item." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre modelo e item. [lead]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com veículo.
	 *  
	 * REGRA: MDL-D3
	 *  
	 * @param modelId	ID do modelo que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateVehicleRelationship( Integer modelId ) throws AppException, BusException {
		try {
			if( modelId != null ) {
				boolean exists = this.dao.hasVehicleRelationship( modelId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o modelo pois existe um relacionamento com veículo." );
				}
				
			} else {
				throw new BusException( "ID do modelo inválido para checar o relacionamento com veículo." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre modelo e veículo. [lead]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com produto.
	 *  
	 * REGRA: MDL-D4
	 *  
	 * @param modelId	ID do modelo que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateProductRelationship( Integer modelId ) throws AppException, BusException {
		try {
			if( modelId != null ) {
				boolean exists = this.dao.hasProductRelationship( modelId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o modelo pois existe um relacionamento com produto." );
				}
				
			} else {
				throw new BusException( "ID do modelo inválido para checar o relacionamento com produto." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre modelo e produto. [lead]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
