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
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Qualification> save( Qualification model, UserProfileDTO userProfile ) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnSave.class);
			
			Optional<Qualification> saved = this.dao.save( model );

			if( saved.isPresent() ) {
				// Salva ele mesmo na árvore
				this.treeService.addNode(saved.get().getId(), saved.get().getId(), userProfile);
				
				/*
				 * Caso tenha um parent definido, então temos que adicionar na árvore
				 */
				if( model.getParentId() != null ) {
					// Adiciona na estrutura correta
					this.treeService.addNode( saved.get().getId(), model.getParentId(), userProfile );
				}
				
				this.audit( saved.get(), AuditOperationType.QUALIFICATION_INSERTED, userProfile);

			} else {
				throw new BusException( "Não é possível salvar a hierarquia sem qualificação." );			
			}
			
			return saved;
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de cadastro do qualificação: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Atualiza um qualificação
	 * 
	 * REGRA: QLF-U3
	 * 
	 * @param model objeto qualificação que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public Optional<Qualification> update(Qualification model, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			this.validateEntity(model, OnUpdate.class);
			
			// QLF-U3
			Optional<Qualification> modelDB = this.getById( model.getId() );
			if( !modelDB.isPresent() ) {
				throw new BusException( "A qualificação a ser atualizada não existe.");
			}
			
			Optional<Qualification> saved = this.dao.update( model );
			
			this.audit( ( saved.isPresent() ? saved.get() : null ), AuditOperationType.QUALIFICATION_UPDATED, userProfile);
			
			return saved;

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro no processo de atualização do qualificação: {}", model, e );
			throw new AppException( this.messageSource.getMessage("error.generic.update", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Efetua a exclusão de um qualificação
	 * 
	 * @param id ID do qualificação
	 * @param userProfile dados do usuário logado. 
	 */
	@Override
	public void delete(Integer id, UserProfileDTO userProfile) throws AppException, BusException {
		
		try {
			if( id == null ) {
				throw new BusException( "ID de exclusão inválido." );
			}
			
			Optional<Qualification> entityDB = this.getById(id);
			if( !entityDB.isPresent() ) {
				throw new BusException( "A qualificação a ser excluída não existe.");
			}

			//Rega: QLF-D1
			this.validatePersonRelationship(id);
			
			
			List<QualificationTreePathDTO> childrens = this.treeService.treeByParent( id );
			// Exclui todos as estrutura vinculadas e esse nó
			this.treeService.deleteNode( id, userProfile );
			this.deleteChildrens( childrens );
			
			this.dao.delete( id );
			
			this.audit( entityDB.get(), AuditOperationType.QUALIFICATION_DELETED, userProfile);
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão do qualificação.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}
	
	/**
	 * Exclui todos os nós abaixo da hierarquia.
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
			log.error( "Erro no processo de exclusão do qualificação.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.delete", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}	
	}

	/**
	 * Busca um qualificação pelo seu ID
	 * 
	 * @param id ID do qualificação
	 */
	@Override
	public Optional<Qualification> getById(Integer id) throws AppException, BusException {
		try {
			
			if( id == null ) {
				throw new BusException( "ID de busca inválido." );
			}
			
			return this.dao.getById( id );

		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao consultar um qualificação pelo ID: {}", id, e );
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
			log.error( "Erro ao mover uma qualificação pelo na estrutura: Parent: {}, Child: {}", parentId, nodeId, e );
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
			log.error( "Erro ao carregar a árvore de qualificação.", e );
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
	 * Busca qualificações que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Qualification, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id")
	 * 
	 * @param model objeto canal para ser buscado
	 */
	@Override
	public Optional<Qualification> find(Qualification model) throws AppException, BusException {
		List<Qualification> models = this.find( model, null );
		return Optional.ofNullable( (models != null ? models.get(0) : null) );
	}

	/**
	 * Lista todos os qualificações.
	 *
	 * Esse método é uma sobrecarga de {@link #listAll(Pageable)}
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");
	 */
	@Override
	public List<Qualification> list() throws AppException, BusException {
		return this.listAll( null );
	}

	/**
	 * Busca qualificações que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * Esse é método é uma sobrecarga de {@link #search(Qualification, Pageable)} será usada a paginação padrão: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id")
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
	 * Lista todos os qualificações.
	 * 
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");
	 */
	@Override
	public List<Qualification> listAll( Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.listAll( pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de listar os qualificações.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.listall", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca qualificações que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o '='
	 * 
	 * @param model objeto qualificações para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");
	 */
	@Override
	public List<Qualification> find( Qualification model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.find( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de buscar as qualificações.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.find", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}

	/**
	 * Busca qualificações que respeitem os dados do objeto.
	 * Aqui os campos String são buscados com o 'LIKE'
	 * 
	 * @param model objeto qualificações para ser buscado
	 * @param pageable configuração da paginação e ordenação, se nulo usamos os valores padrões: PageRequest.of( 0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");
	 */
	@Override
	public List<Qualification> search( Qualification model, Pageable pageable ) throws AppException, BusException {
		try {
			if( pageable == null ) {
				pageable = DEFAULT_PAGINATION;
			}
			
			return this.dao.search( model, pageable );
			
		} catch (Exception e) {
			log.error( "Erro no processo de procurar as qualificações.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.search", new Object [] { Qualification.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida a entidade como um todo, passando por regras de formatação e obrigatoriedade
	 * 
	 * Regra: QLF-I1, QLF-I2, QLF-U1, QLF-U2
	 * 
	 * @param model entidade a ser validada
	 * @param group grupo de validação que será usado
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
	 * @param qlfId	ID da qualificação que deve ser verificada
	 * @throws AppException	Em caso de erro sistêmico
	 * @throws BusException	Em caso de erro relacionado a regra de negócio
	 * @throws NoSuchMessageException 
	 */
	private void validatePersonRelationship(Integer qlfId) throws BusException, NoSuchMessageException, AppException {
		try {
			if( qlfId != null ) {
				boolean exists = this.dao.hasPersonRelationship( qlfId );
				if( exists ) {
					throw new BusException( "Não é possível excluir a qualificação pois existe um relacionamento com pessoa." );
				}
				
			} else {
				throw new BusException( "ID da qualificação inválido para checar o relacionamento com pessoa." );
			}
			
		} catch (BusException e) {
			throw e;
			
		} catch (Exception e) {
			log.error( "Erro ao carregar o relacionamento entre qualificação e pessoa.", e );
			throw new AppException( this.messageSource.getMessage("error.generic", new Object [] { Person.class.getSimpleName() }, LocaleContextHolder.getLocale() ) );
		}
	}
}
