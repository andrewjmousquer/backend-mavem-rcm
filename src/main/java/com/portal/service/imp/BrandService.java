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
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Brand;
import com.portal.service.IAuditService;
import com.portal.service.IBrandService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BrandService implements IBrandService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IBrandDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Lista todos as marcas.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "brd_id");
	 */
	@Override
	public List<Brand> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {
			
			if( pageable == null ) {
				pageable = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "brd_id");
			}
			
			return this.dao.listAll( pageable );
			
		} catch ( Exception e ) {
			log.error( "Erro no processo de listar as marcas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 * 
	 * @param brand objeto marca que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Brand> saveOrUpdate(Brand model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	/**
	 * Salva uma marca.
	 * 
	 * @param brand objeto marca que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Brand> save(Brand model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {

			this.validateEntity(model, OnSave.class);
			
			this.validateHasDuplicate(model);
			
			Optional<Brand> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.BRAND_INSERTED, userProfile);
			
			return saved;
			
		} catch ( BusException e ) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro da marca: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza uma marca
	 * 
	 * @param model objeto marca que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Brand> update(Brand model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity(model, OnUpdate.class);
			
			// BRD-U4
			Optional<Brand> brandDB = this.getById( model.getId() );
			if( !brandDB.isPresent() ) {
				throw new BusException( "A marca a ser atualizado não existe.");
			}

			this.validateHasDuplicate(model);

			Optional<Brand> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.BRAND_UPDATED, userProfile);
			
			return saved;

		} catch ( BusException e ) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização da marca: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<Brand> find( Brand brand, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "brd_id");
			}
			
			return this.dao.find( brand, pageable );
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as marcas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<Brand> search( Brand brand, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "brd_id");
			}
			
			return this.dao.search( brand, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar as marcas.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public Optional<Brand> find(Brand model) throws AppException, BusException {
		List<Brand> brands = this.find( model, null );
		return Optional.ofNullable( (brands != null ? brands.get(0) : null) );
	}

	@Override
	public List<Brand> search(Brand model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca uma marca pelo seu ID
	 * 
	 * @param id ID da marca
	 */
	@Override
	public Optional<Brand> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch ( BusException e ) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar uma marca pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<Brand> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de uma marca.
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Brand> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "A marca a ser excluído não existe.");
			}

			this.validateLeadRelationship(id);
			this.validateModelRelationship(id);
			this.validatePartnerRelationship(id);
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.BRAND_DELETED, userProfile);
			
			this.dao.delete( id );
			
		} catch ( BusException e ) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão da marca.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(Brand model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: BRD-I2, BRD-U2
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( Brand model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		Brand rnSearch = Brand.builder().name( model.getName() ).build();
		List<Brand> listBD = this.find( rnSearch, null );

		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe uma marca com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe uma marca com o mesmo nome.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: BRD-I1, BRD-I3, BRD-U1 e BRD-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Brand model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	/**
	 * Valida se existe algum relacionamento com modelo.
	 *  
	 * REGRA: BRD-D1
	 *  
	 * @param brandId	ID da marca que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateModelRelationship( Integer brandId ) throws AppException, BusException {
		try {
			if( brandId != null ) {
				boolean exists = this.dao.hasModelRelationship( brandId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a marca pois existe um relacionamento com modelo." );
				}
				
			} else {
				throw new BusException( "ID da marca inválido para checar o relacionamento com modelo." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre marca e modelo. [model]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com lead.
	 *  
	 * REGRA: BRD-D2
	 *  
	 * @param brandId	ID da marca que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validateLeadRelationship( Integer brandId ) throws AppException, BusException {
		try {
			if( brandId != null ) {
				boolean exists = this.dao.hasLeadRelationship( brandId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a marca pois existe um relacionamento com lead." );
				}
				
			} else {
				throw new BusException( "ID da marca inválido para checar o relacionamento com lead." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre marca e lead. [lead]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe algum relacionamento com parceiro.
	 *  
	 * REGRA: BRD-D3
	 *  
	 * @param brandId	ID da marca que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 */
	private void validatePartnerRelationship( Integer brandId ) throws AppException, BusException {
		try {
			if( brandId != null ) {
				boolean exists = this.dao.hasPartnerRelationship( brandId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a marca pois existe um relacionamento com parceiro." );
				}
				
			} else {
				throw new BusException( "ID da marca inválido para checar o relacionamento com parceiro." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre marca e parceiro. [partner_brand]", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Brand.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
