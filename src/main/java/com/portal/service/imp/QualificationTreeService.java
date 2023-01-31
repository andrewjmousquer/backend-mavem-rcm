package com.portal.service.imp;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.portal.dao.IQualificationTreeDAO;
import com.portal.dto.QualificationTreePathDTO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.Qualification;
import com.portal.service.IQualificationTreeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class QualificationTreeService implements IQualificationTreeService {

	@Autowired
	private IQualificationTreeDAO dao;
	
	@Autowired
    public MessageSource messageSource;
	
	@Autowired
	private QualificationService qualificationService;
	
	/**
	 * Adiciona um novo nó na árvore de relacionamento.
	 * 
	 * @param model objeto que deve ser salvo.
	 * @param userProfile dados do usuário logado.
	 */
	@Override
	public void addNode( int nodeId, int parentId, UserProfileDTO userProfile ) throws AppException, BusException {
		try {
			
			this.validateExistedNode(nodeId, parentId);
			this.validateHasDuplicate( nodeId, parentId );
			this.dao.addNode( parentId, nodeId );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de adicionar um nó na árvore: Parent: {}, Child: {}", parentId, nodeId, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { "Qualification Tree" }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Remove um nó da estrutura.
	 * 
	 * IMPORTANTE: Ao remover um nó, estará removendo todos os seus relacionamentos e o relacionamento de seus filhos,
	 * 
	 * @param nodeId	Id do nó a ser removido.
	 */
	@Override
	public void deleteNode( int nodeId, UserProfileDTO userProfile ) throws AppException, BusException {
		try {

			Optional<Qualification> node = this.qualificationService.getById( nodeId );
			if( !node.isPresent() ) {
				throw new BusException( "O nó não existe ou é inválido. ID: " + nodeId );
			}
			
			this.dao.deleteNode( nodeId );
			
		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de exclusão de um nó da árvore: {}", nodeId, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { "Qualification Tree" }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Move uma estrurura de um nó para outro. Nesse caso, ao mover um nó toda sua estrurua de filhos irá junto com ele.
	 *
	 * @param	nodeId		Nó que devemos mover
	 * @param	newParentId	Id do nó pai ao qual vamos mover a estrutura
	 */
	@Override
	public void moveNode( int nodeId, int newParentId ) throws AppException, BusException {
		try {
			
			// QLFT-U1
			if( nodeId == newParentId ) {
				throw new BusException( "Não é possível mover o nó para ele mesmo." );
			}
			
			// QLFT-U2
			if( this.dao.isChildOf(newParentId, nodeId) ) {
				throw new BusException( "Não é possível mover o node para um de seus próprios filhos." );
			}
			
			this.validateExistedNode(nodeId, newParentId);
			this.dao.disconnectNode( nodeId );
			this.dao.connectNode( nodeId, newParentId );

		} catch (BusException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro no processo de mover um nó na estrutura: {}", nodeId, e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { "Qualification Tree" }, LocaleContextHolder.getLocale() ) );
		}
	}

	@Override
	public List<QualificationTreePathDTO> tree() throws AppException {
		try {
			return this.dao.tree();
		} catch (Exception e) {
			log.error( "Erro ao carregar a estrutura de qualificação.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { "Qualification Tree" }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<QualificationTreePathDTO> treeByName(String name) throws AppException {
		try {
			return this.dao.treeByName( name );
		} catch (Exception e) {
			log.error( "Erro ao carregar a estrutura de qualificação por nome.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { "Qualification Tree" }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	@Override
	public List<QualificationTreePathDTO> treeByParent(int parentId) throws AppException {
		try {
			return this.dao.treeByParent( parentId );
		} catch (Exception e) {
			log.error( "Erro ao carregar a estrutura de qualificação por parentId.", e );
			throw new AppException( this.messageSource.getMessage("error.generic.save", new Object [] { "Qualification Tree" }, LocaleContextHolder.getLocale() ) );
		}
	}
	
	/**
	 * Valida se existe entidade duplicada.
	 * 
	 * Regra: QLFT-I1
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateHasDuplicate( int nodeId, int parentId ) throws AppException, BusException {
		boolean hasDuplicate = this.dao.hasDuplicate( parentId, nodeId );
		if( hasDuplicate ) {
			throw new BusException( "Já existe um relacionamento entre esses nós.");	
		}
	}
	
	/**
	 * Valida se o pai e filho existem.
	 * 
	 * Regra: QLFT-I2, QLFT-I3, QLFT-I3, QLFT-I4
	 * 
	 * @param model	entidade a ser valiadada
	 * @throws AppException
	 * @throws BusException
	 */
	private void validateExistedNode( int nodeId, int parentId ) throws AppException, BusException {
		Optional<Qualification> parentDB =  this.qualificationService.getById( parentId );
		if( !parentDB.isPresent() ) {
			throw new BusException( "A qualificação apontada como pai não existe.");	
		}
		
		Optional<Qualification> childDB =  this.qualificationService.getById( nodeId );
		if( !childDB.isPresent() ) {
			throw new BusException( "A qualificação apontada como filho não existe.");	
		}
	}
	
	
}
