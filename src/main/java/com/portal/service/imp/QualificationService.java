package com.portal.service.imp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.portal.dao.IQualificationDAO;
import com.portal.dto.QualificationTreePathDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Person;
import com.portal.model.Qualification;
import com.portal.service.IAuditService;
import com.portal.service.IQualificationService;
import com.portal.service.IQualificationTreeService;
import com.portal.validators.ValidationHelper;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class QualificationService implements IQualificationService {

	@Autowired
    private Validator validator;
	
	@Autowired
	private IQualificationDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private IAuditService auditService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private IQualificationTreeService treeService;
	
	private static final Pageable DEFAULT_PAGINATION = PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id"); 

	/**
	 * Salva um novo objeto.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<Qualification> save( Qualification model, UserProfileDTO userProfile ) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnSave.class);
			
			Optional<Qualification> saved = this.dao.save( model );

			if( saved.isPresent() ) {
				// Salva ele mesmo na ??rvore
				this.treeService.addNode(saved.get().getId(), saved.get().getId(), userProfile);
				
				/*
				 * Caso tenha um parent definido, ent??o temos que adicionar na ??rvore
				 */
				if( model.getParentId() != null ) {
					// Adiciona na estrutura correta
					this.treeService.addNode( saved.get().getId(), model.getParentId(), userProfile );
				}
				
				this.audit( saved.get(), AuditOperationType.QUALIFICATION_INSERTED, userProfile);

			} else {
				throw new BusException( "N??o ?? poss??vel salvar a hierarquia sem qualifica????o." );			
			}
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do qualifica????o: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Atualiza um qualifica????o
	 * 
	 * REGRA: QLF-U3
	 * 
	 * @param model objeto qualifica????o que deve ser salvo.
	 * @param userProfile dados do usu??rio logado.
	 */
	@Override
	public Optional<Qualification> update(Qualification model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			// QLF-U3
			Optional<Qualification> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "A qualifica????o a ser atualizada n??o existe.");
			}
			
			Optional<Qualification> saved = this.dao.update( model );
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.QUALIFICATION_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualiza????o do qualifica????o: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Efetua a exclus??o de um qualifica????o
	 * 
	 * @param id ID do qualifica????o
	 * @param userProfile dados do usu??rio logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			if( id == null ) {
				throw new BusException( "ID de exclus??o inv??lido." );
			}
			
			Optional<Qualification> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A qualifica????o a ser exclu??da n??o existe.");
			}

			//Rega: QLF-D1
			this.validatePersonRelationship(id);
			
			
			List<QualificationTreePathDTO> childrens = this.treeService.treeByParent( id );
			// Exclui todos as estrutura vinculadas e esse n??
			this.treeService.deleteNode( id, userProfile );
			this.deleteChildrens( childrens );
			
			this.dao.delete( id );
			
			this.audit( entityDB.get(), AuditOperationType.QUALIFICATION_DELETED, userProfile);
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o do qualifica????o.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	/**
	 * Exclui todos os n??s abaixo da hierarquia.
	 */
	private void deleteChildrens(List<QualificationTreePathDTO> childrens) throws BusException, AppException {
		try {
			if( childrens != null && !childrens.isEmpty() ) {
				for (QualificationTreePathDTO children : childrens) {
					//Rega: QLF-D1
					this.validatePersonRelationship( children.getId() );
					this.dao.delete( children.getId() );
				}
			}
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclus??o do qualifica????o.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	/**
	 * Busca um qualifica????o pelo seu ID
	 * 
	 * @param id ID do qualifica????o
	 */
	@Override
	public Optional<Qualification> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inv??lido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um qualifica????o pelo ID: {}", id, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public void move( int nodeId, int parentId, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.treeService.moveNode(nodeId, parentId);
			Optional<Qualification> audit = this.getById( nodeId );
			this.audit( ( audit.isPresent() ? audit.get() : null ), AuditOperationType.QUALIFICATION_MOVED, userProfile);
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao mover uma qualifica????o pelo na estrutura: Parent: {}, Child: {}", parentId, nodeId, e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public Set<QualificationTreePathDTO> loadTree() throws AppException {
		try {
			Set<QualificationTreePathDTO> roots = new LinkedHashSet<>();
			List<QualificationTreePathDTO> tree = this.treeService.tree();

			Map<Integer, QualificationTreePathDTO> mapQual = new LinkedHashMap<Integer, QualificationTreePathDTO>();
			
			if( tree != null ) {
				// Faz um cache das entidades
				for (QualificationTreePathDTO dto : tree) {
					dto.setChildrens( new ArrayList<>() );
					mapQual.put( dto.getId(), dto );
				}

				for (QualificationTreePathDTO dto : tree) {
					String[] treePath = dto.getBreadcrumbIdPath().split( "," );
					QualificationTreePathDTO newRoot = treePath( treePath, 0, dto.getLevel(), mapQual );
					
					if( newRoot != null ) {
						roots.add(newRoot);
					}
				}
			}

			mapQual.clear();
			mapQual = null;
			
			return roots;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar a ??rvore de qualifica????o.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.getbyid", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	private QualificationTreePathDTO treePath( String[] treePath, Integer control, Integer level, Map<Integer, QualificationTreePathDTO> mapQual ) {

		int parentId = Integer.parseInt( treePath[ control ] );
				
		if( level.equals( control ) ) {
			return mapQual.get( parentId );
		}

		QualificationTreePathDTO root = mapQual.get( parentId );
		QualificationTreePathDTO child = treePath(treePath, ++control, level, mapQual);
		child.setParentId( parentId );

		if( root != null ) {
			if( !root.getChildrens().contains( child ) ) {
				root.getChildrens().add( child );
			}
			return root;

		} else {
			return child;
		}
	}
	
	@Override
	public void audit(Qualification model, AuditOperationType operationType, UserProfileDTO userProfile) throws AppException, BusException {
		try {
			this.auditService.save( objectMapper.writeValueAsString( model ), operationType, userProfile);
		} catch (JsonProcessingException e) {
			throw new AppException(this.messageSource.getMessage("error.audit", null, LocaleContextHolder.getLocale()));
		}
	}
	

	/**
	 * Busca qualifica????es que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(Qualification, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id")
	 * 
	 * @param model objeto canal para ser buscado
	 */
	@Override
	public Optional<Qualification> find(Qualification model) throws AppException, BusException {
		List<Qualification> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Lista todos os qualifica????es.
	 *
	 * Esse m??todo ?? uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");
	 */
	@Override
	public List<Qualification> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Busca qualifica????es que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o 'LIKE'
	 * 
	 * Esse ?? m??todo ?? uma sobrecarga de {@link #search(Qualification, Pageable)} ser?? usada a pagina????o padr??o: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id")
	 * 
	 * @param model objeto canal para ser buscado
	 */
	@Override
	public List<Qualification> search(Qualification model) throws AppException, BusException {
		return this.search( model, null );
	}

	@Override
	public Optional<Qualification> saveOrUpdate(Qualification model, UserProfileDTO userProfile) throws AppException, BusException {
		if(model != null && model.getId() != null && model.getId() != 0) {
			return this.update(model, userProfile);
		} else {
			return this.save(model, userProfile);
		}
	}

	/**
	 * Lista todos os qualifica????es.
	 * 
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");
	 */
	@Override
	public List<Qualification> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os qualifica????es.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca qualifica????es que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o '='
	 * 
	 * @param model objeto qualifica????es para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");
	 */
	@Override
	public List<Qualification> find( Qualification model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as qualifica????es.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca qualifica????es que respeitem os dados do objeto.
	 * Aqui os campos String s??o buscados com o 'LIKE'
	 * 
	 * @param model objeto qualifica????es para ser buscado
	 * @param pageable configura????o da pagina????o e ordena????o, se nulo usamos os valores padr??es: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");
	 */
	@Override
	public List<Qualification> search( Qualification model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar as qualifica????es.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formata????o e obrigatoriedade
	 * 
	 * Regra: QLF-I1, QLF-I2, QLF-U1, QLF-U2
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de valida????o que ser?? usado
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateEntity( Qualification model, Class<?> group ) throws AppException, BusException {
		ValidationHelper.generateException( validator.validate( model, group ) );
	}
	
	/**
	 * FIXME Tirar essa REGRA - COTO
	 * 
	 * Valida se existe algum relacionamento com pessoa.
	 *  
	 * REGRA: QLF-D1
	 *  
	 * @param qlfId	ID da qualifica????o que deve ser verificada
	 * @throws AppException	Em caso de erro sist??mico
	 * @throws BusException	Em caso de erro relacionado a regra de neg??cio
	 * @throws NoSuchMessageException 
	 */
	private void validatePersonRelationship(Integer qlfId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( qlfId != null ) {
				boolean exists = this.dao.hasPersonRelationship( qlfId );
				if( exists ) {
					throw new BusException( "N??o ?? poss??vel excluir a qualifica????o pois existe um relacionamento com pessoa." );
				}
				
			} else {
				throw new BusException( "ID da qualifica????o inv??lido para checar o relacionamento com pessoa." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre qualifica????o e pessoa.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
