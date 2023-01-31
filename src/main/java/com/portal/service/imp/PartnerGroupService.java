package com.portal.service.imp;

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
import com.portal.dao.IPartnerGroupDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Classifier;
import com.portal.model.Partner;
import com.portal.model.PartnerGroup;
import com.portal.model.Person;
import com.portal.service.IAuditService;
import com.portal.service.IClassifierService;
import com.portal.service.IPartnerGroupService;
import com.portal.service.IPartnerService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PartnerGroupService implements IPartnerGroupService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IPartnerGroupDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private IPartnerService partnerService;
	
	@Autowired
	private IClassifierService classifierService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id"); 

	/**
	 * Lista todos os grupos de parceiros.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id");
	 */
	@Override
	public List<PartnerGroup> listAll( Pageable pageable ) throws AppException, BusException {
		
		try {
			
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os grupos de parceiros.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { PartnerGroup.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Método auxiliar que decide com base no ID se a entidade deve ser salva ou
	 * atualizada.
	 * Se não tiver ID é save, caso contrário é update.
	 * 
	 * @param model objeto grupo parceiro que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<PartnerGroup> saveOrUpdate(PartnerGroup model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}
	
	/**
	 * Salva um novo grupo parceiro.
	 * 
	 * @param model objeto marca que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<PartnerGroup> save(PartnerGroup model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity(model, OnSave.class);
			
			this.validateHasDuplicate(model);
			
			Optional<PartnerGroup> saved = this.dao.save(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PARTNER_GROUP_INSERTED, userProfile);
			
			return saved;
			
		} catch ( BusException e ) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do grupo parceiro: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { PartnerGroup.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Atualiza um grupo de parceiro
	 * 
	 * @param model objeto grupo que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<PartnerGroup> update(PartnerGroup model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			
			this.validateEntity(model, OnUpdate.class);
			
			// PTG-U3
			Optional<PartnerGroup> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "O grupo a ser atualizado não existe.");
			}
			
			this.validateHasDuplicate(model);
			
			// PTG-U4
			if( modelDB.get().getActive() && !model.getActive() ) {
				this.disablePartners( model, userProfile );
			}
			
			
			Optional<PartnerGroup> saved = this.dao.update(model);
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.PARTNER_GROUP_UPDATED, userProfile);
			
			return saved;
			
		} catch ( BusException e ) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do grupo parceiro: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { PartnerGroup.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca grupos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto grupo para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id");
	 */
	@Override
	public List<PartnerGroup> find( PartnerGroup model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar os grupos de parceiros.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { PartnerGroup.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca grupos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto grupo para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id");
	 */
	@Override
	public List<PartnerGroup> search( PartnerGroup model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar os grupos de parceiros.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { PartnerGroup.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Busca grupos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #find(PartnerGroup, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id")
	 * 
	 * @param model objeto grupo para ser buscado
	 */
	@Override
	public Optional<PartnerGroup> find(PartnerGroup model) throws AppException, BusException {
		List<PartnerGroup> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}
	
	/**
	 * Busca grupos que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(PartnerGroup, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id")
	 * 
	 * @param model objeto grupo para ser buscado
	 */
	@Override
	public List<PartnerGroup> search(PartnerGroup model) throws AppException, BusException {
		return this.search( model, null );
	}
	
	/**
	 * Busca um grupo pelo seu ID
	 * 
	 * @param id ID do grupo parceiro
	 */
	@Override
	public Optional<PartnerGroup> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch ( BusException e ) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um grupo de parceiro pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { PartnerGroup.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Lista todos os grupos de parceiros.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id");
	 */
	@Override
	public List<PartnerGroup> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Efetua a exclusão de um grupo parceiro.
	 * 
	 * @param id ID do grupo
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<PartnerGroup> entityDB = this.getById(id);
			if( entityDB == null || !entityDB.isPresent() ) {
				throw new BusException( "O grupo a ser excluído não existe.");
			}
			
			// PTG-D1
			this.validatePartnerRelationship(id);
			
			this.audit( ( entityDB.isPresent() ? entityDB.get() : null ), AuditOperationType.PARTNER_GROUP_DELETED, userProfile);
			
			this.dao.delete( id ); 
			
		} catch ( BusException e ) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do grupo parceiro.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { PartnerGroup.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	@Override
	public void audit(PartnerGroup model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: PTG-I2, PTG-U2
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( PartnerGroup model ) throws AppException, BusException {
		
		if( model == null ) {
			throw new BusException( "Não é possível chegar a duplicidade com o objeto da entidade nula." );
		}
		
		PartnerGroup rnSearch = PartnerGroup.builder()
				.name( model.getName() )
				.build();

		List<PartnerGroup> listBD = this.find( rnSearch, null );
		
		// Save Action
		if( ( model.getId() == null || model.getId().equals(0) ) && 
				listBD != null && !listBD.isEmpty() ) {
			throw new BusException( "Já existe um grupo com o mesmo nome.");
		}
		
		// Update Action
		if( model.getId() != null && !model.getId().equals(0) && 
				listBD != null && !listBD.isEmpty()) {
			
			long count = listBD.stream()
							.filter( item -> !item.getId().equals( model.getId() ) && item.getName().equals( model.getName() ) )
							.count();
			
			if( count > 0 ) {
				throw new BusException( "Já existe um grupo com o mesmo nome.");
			}
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: PTG-I1, PTG-I3, PTG-U1, PTG-U3
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( PartnerGroup model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	/**
	 * Valida se existe algum relacionamento com parceiro.
	 *  
	 * REGRA: PTG-D1
	 *  
	 * @param ptgId	ID do grupo que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validatePartnerRelationship(Integer ptgId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( ptgId != null ) {
				boolean exists = this.dao.hasPartnerRelationship( ptgId );
				if( exists ) {
					throw new BusException( "Não é possível excluir o grupo pois existe um relacionamento com parceiro." );
				}
				
			} else {
				throw new BusException( "ID do grupo inválido para checar o relacionamento com parceiro." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre grupo e parceiro.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Desabilitar todos os parceiros associados ao grupo
	 * 
	 * REGRA: PTG-U4
	 */
	private void disablePartners(PartnerGroup model, UserProfileDTO userProfile) throws BusException, AppException {
		try {
			if( model == null || model.getId() == null ) {
				throw new BusException( "Necessário que o ID do grupo seja válido para desativar seus parceiros relacionados." );
			}
			
			List<Partner> partners = partnerService.search( Partner.builder().partnerGroup(model).build() );
			if( partners != null ) {
				Classifier inactiveStatus = this.classifierService.find(new Classifier("INATIVO", "PARTNER_SITUATION")).get();
				for (Partner partner : partners) {
					Optional<Partner> existsPartner = this.partnerService.getById(partner.getId());
					existsPartner.get().setSituation(inactiveStatus);
					this.partnerService.update( existsPartner.get(), userProfile );
				}
			}
		} catch (Exception e) {
			log.error( "Erro ao desativar todos os parceiros relacionados.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
