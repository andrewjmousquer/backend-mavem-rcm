package com.portal.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IQualificationTreeDAO;
import com.portal.dto.QualificationTreePathDTO;
import com.portal.exceptions.AppException;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class QualificationTreeDAO extends BaseDAO implements IQualificationTreeDAO {

	@Override
	public void addNode( int parentId, int childId ) throws AppException {
		try {
			String query = "INSERT INTO qualification_tree( parent_qlf_id, child_qlf_id, level ) " +
						   		"SELECT parent_qlf_id, :childId, level+1 " +
						   		"FROM qualification_tree " +
						   		"WHERE child_qlf_id = :parentId " +
						   		"UNION ALL " +
						   		"SELECT :childId, :childId, 0 " +
						   		"ON DUPLICATE KEY UPDATE parent_qlf_id=VALUES(parent_qlf_id), child_qlf_id=VALUES(child_qlf_id)";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "parentId", parentId  );
			params.addValue( "childId", childId );
	
			log.trace( "[QUERY] qualification_tree.save: {} [PARAMS]: {}", query, params.getValues() );
			this.getNamedParameterJdbcTemplate().update(query, params);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o qualification_tree: Parent: {}, Child: {}", parentId, childId , e );
			throw new AppException( "Erro ao tentar salvar o qualification_tree.", e);
		}
	}

	@Override
	public void deleteNode( Integer id ) throws AppException {
		try {
			String query = 	"DELETE FROM qualification_tree " +
							"WHERE child_qlf_id IN ( " +
								"SELECT child_qlf_id " +
								"FROM ( " +
									"SELECT child_qlf_id " +
									"FROM qualification_tree " +
									"WHERE parent_qlf_id = :id " +
								") AS c" +
							");";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] qualification_tree.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o qualification_tree" , e );
			throw new AppException( "Erro ao excluir o qualification_tree.", e );
		}

	}

	@Override
	public boolean isChildOf(int parentId, int childId) throws AppException {
		
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT parent_qlf_id " +
								"FROM qualification_tree " +
								"WHERE parent_qlf_id = :parentId AND child_qlf_id = :childId " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists`";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "parentId", parentId );
			params.addValue( "childId", childId );

			log.trace( "[QUERY] qualification_tree.isChildOf: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao checar se um nó é filho de outro." , e );
			throw new AppException( "Erro ao checar se um nó é filho de outro.", e );
		}
	}

	/**
	 * Desconecta toda um nó e toda sua estrutura de filhos da árvore.
	 * Só remove a relação com seus pais, mas não apaga sua estrutura de filhos.
	 * 
	 * @param nodeId	ID do nó que será desconectado 
	 */
	@Override
	public void disconnectNode( int nodeId ) throws AppException {
		
		try {
			String query = 	"DELETE q1 " +
							"FROM qualification_tree AS q1 " +
							"JOIN qualification_tree AS q2 ON q1.child_qlf_id = q2.child_qlf_id " +
							"LEFT JOIN qualification_tree AS q3 ON q3.parent_qlf_id = q2.parent_qlf_id AND q3.child_qlf_id = q1.parent_qlf_id " +
							"WHERE q2.parent_qlf_id = :node AND q3.parent_qlf_id IS NULL;";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "node", nodeId );

			log.trace( "[QUERY] qualification_tree.disconnectNode: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update( query, params );
			
		} catch (Exception e) {
			log.error( "Erro ao desconectar um nó da árevore." , e );
			throw new AppException( "Erro ao desconectar um nó da árevore.", e );
		}
		
	}

	/**
	 * Reconecta um nó e sua hierarquia em uma nova estrutura.
	 * 
	 * @param	nodeId			Nó que será reconectado
	 * @param	newParentId		Novo pai que será usado como estrutura.	
	 */
	@Override
	public void connectNode(int nodeId, int newParentId) throws AppException {
		
		try {
			String query = 	"INSERT INTO qualification_tree (parent_qlf_id, child_qlf_id, level) " +
								"SELECT  supertree.parent_qlf_id,  " +
										"subtree.child_qlf_id,  " +
										"supertree.level + subtree.level + 1 " +
								"FROM qualification_tree AS supertree  " +
								"JOIN qualification_tree AS subtree " +
								"WHERE subtree.parent_qlf_id = :childId " +
								"AND supertree.child_qlf_id = :parentId;";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "childId", nodeId );
			params.addValue( "parentId", newParentId );

			log.trace( "[QUERY] qualification_tree.connectNode: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update( query, params );
			
		} catch (Exception e) {
			log.error( "Erro ao reconectar um nó na árevore." , e );
			throw new AppException( "Erro ao reconectar um nó na árevore.", e );
		}
		
	}

	/**
	 * Verifica se existe um relacionamento duplicado.
	 * 
	 * @param	nodeA	Nó A que ser verificado 
	 * @param	nodeB	Nó B que ser verificado
	 * 
	 * @return true caso já existe um relacionamento, caso contrário false.
	 */
	@Override
	public boolean hasDuplicate( int nodeA, int nodeB ) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT parent_qlf_id " +
								"FROM qualification_tree " +
								"WHERE parent_qlf_id = :nodeA AND child_qlf_id = :nodeB " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists`";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "nodeA", nodeA );
			params.addValue( "nodeB", nodeB );

			log.trace( "[QUERY] qualification_tree.hasDuplicate: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao checar duplicidade de um relacionamento. Parent: {}, Child: {}", nodeA, nodeB, e );
			throw new AppException( "Erro ao checar duplicidade de um relacionamento.", e );
		}
	}

	@Override
	public List<QualificationTreePathDTO> tree() throws AppException {
		try {
			String query = 	"SELECT  q.qlf_id, " +
									"MAX( q2.level ) AS level, " +
									"q.seq, " +
									"q.name, " +
									"q.required, " +
									"q.active, " +
									"GROUP_CONCAT( " +
										"q3.name ORDER BY q2.level DESC separator ',' " +
									") AS breadcrumb_name, " +
									"GROUP_CONCAT( " +
										"q2.parent_qlf_id ORDER BY q2.level DESC separator ','  " +
									") AS breadcrumb_id, " +
									"GROUP_CONCAT( " +
									"		q3.seq ORDER BY q3.seq DESC separator ',' " + 
									") AS breadcrumb_seq " +
							"FROM qualification q " +
							"INNER JOIN qualification_tree q1 ON q1.child_qlf_id = q.qlf_id " +
							"INNER JOIN qualification_tree q2 ON q2.child_qlf_id = q1.child_qlf_id " +
							"INNER JOIN qualification q3 ON q3.qlf_id = q2.parent_qlf_id " +
							"WHERE q1.parent_qlf_id = q1.child_qlf_id " +
						    "GROUP BY q.qlf_id " +
							"ORDER BY breadcrumb_seq";
			
			log.trace( "[QUERY] qualification_tree.tree: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, (rs, rowNum) -> { 
																	return QualificationTreePathDTO.builder()
																							.id( rs.getInt( "qlf_id" ) )
																							.level( rs.getInt( "level" ) )
																							.seq( rs.getInt( "seq" ) )
																							.name( rs.getString( "name" ) )
																							.required( PortalNumberUtils.intToBoolean( rs.getInt( "required" ) ) )
																							.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
																							.breadcrumbNamePath( rs.getString( "breadcrumb_name" ) )
																							.breadcrumbIdPath( rs.getString( "breadcrumb_id" ) )
																							.build();
																} );
			
		} catch (Exception e) {
			log.error( "Erro ao carregar a estrutura de qualificação.", e );
			throw new AppException( "Erro ao carregar a estrutura de qualificação.", e );
		}
	}
	
	@Override
	public List<QualificationTreePathDTO> treeByName( String name ) throws AppException {
		try {
			String query = 	"SELECT  q.qlf_id, " +
									"MAX( q2.level ) AS level, " +
									"q.seq, " +
									"q.name, " +
									"q.required, " +
									"q.active, " +
									"GROUP_CONCAT( " +
										"q3.name ORDER BY q2.level DESC separator ',' " +
									") AS breadcrumb_name, " +
									"GROUP_CONCAT( " +
									"	q2.parent_qlf_id ORDER BY q2.level DESC separator ','  " +
									") AS breadcrumb_id, " +
									"GROUP_CONCAT( " +
									"	q3.seq ORDER BY q3.seq DESC separator ',' " + 
									") AS breadcrumb_seq " +
							"FROM qualification q " +
							"INNER JOIN qualification_tree q1 ON q1.child_qlf_id = q.qlf_id " +
							"INNER JOIN qualification_tree q2 ON q2.child_qlf_id = q1.child_qlf_id " +
							"INNER JOIN qualification q3 ON q3.qlf_id = q2.parent_qlf_id " +
							"WHERE q1.parent_qlf_id = ( SELECT qlf_id FROM qualification WHERE name = '" + name + "' ) " +
							"GROUP BY q.qlf_id " +
							"ORDER BY breadcrumb_seq";
			
			log.trace( "[QUERY] qualification_tree.treeByName: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, (rs, rowNum) -> { 
																	return QualificationTreePathDTO.builder()
																							.id( rs.getInt( "qlf_id" ) )
																							.level( rs.getInt( "level" ) )
																							.seq( rs.getInt( "seq" ) )
																							.name( rs.getString( "name" ) )
																							.required( PortalNumberUtils.intToBoolean( rs.getInt( "required" ) ) )
																							.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
																							.breadcrumbNamePath( rs.getString( "breadcrumb_name" ) )
																							.breadcrumbIdPath( rs.getString( "breadcrumb_id" ) )
																							.build();
																} );
			
		} catch (Exception e) {
			log.error( "Erro ao carregar a estrutura de qualificação por nome.", e );
			throw new AppException( "Erro ao carregar a estrutura de qualificação por nome.", e );
		}
	}
	
	@Override
	public List<QualificationTreePathDTO> treeByParent( int parentId ) throws AppException {
		try {
			String query = 	"SELECT  q.qlf_id, " +
									"MAX( q2.level ) AS level, " +
									"q.seq, " +
									"q.name, " +
									"q.required, " +
									"q.active, " +
									"GROUP_CONCAT( " +
										"q3.name ORDER BY q2.level DESC separator ',' " +
									") AS breadcrumb_name, " +
									"GROUP_CONCAT( " +
									"	q2.parent_qlf_id ORDER BY q2.level DESC separator ','  " +
									") AS breadcrumb_id, " +
									"GROUP_CONCAT( " +
									"	q3.seq ORDER BY q3.seq DESC separator ',' " + 
									") AS breadcrumb_seq " +
							"FROM qualification q " +
							"INNER JOIN qualification_tree q1 ON q1.child_qlf_id = q.qlf_id " +
							"INNER JOIN qualification_tree q2 ON q2.child_qlf_id = q1.child_qlf_id " +
							"INNER JOIN qualification q3 ON q3.qlf_id = q2.parent_qlf_id " +
							"WHERE q1.parent_qlf_id = :parentId " +
							"GROUP BY q.qlf_id " +
							"ORDER BY breadcrumb_seq";
			
			log.trace( "[QUERY] qualification_tree.treeByName: {} [PARAMS]: {}", query );

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "parentId", parentId );
			
			return this.getJdbcTemplatePortal().query( query, params, (rs, rowNum) -> { 
																	return QualificationTreePathDTO.builder()
																							.id( rs.getInt( "qlf_id" ) )
																							.level( rs.getInt( "level" ) )
																							.seq( rs.getInt( "seq" ) )
																							.name( rs.getString( "name" ) )
																							.required( PortalNumberUtils.intToBoolean( rs.getInt( "required" ) ) )
																							.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
																							.breadcrumbNamePath( rs.getString( "breadcrumb_name" ) )
																							.breadcrumbIdPath( rs.getString( "breadcrumb_id" ) )
																							.build();
																} );
			
		} catch (Exception e) {
			log.error( "Erro ao carregar a estrutura de qualificação por parentId.", e );
			throw new AppException( "Erro ao carregar a estrutura de qualificação por parentId.", e );
		}
	}
}
