package com.portal.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.ISellerAgentDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.SellerMapper;
import com.portal.model.Seller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SellerAgentDAO extends BaseDAO implements ISellerAgentDAO {

	@Override
    public List<Seller> findBySeller(Integer selId) throws AppException {
        try {
           
            StringBuilder query = new StringBuilder();
            
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id AS per_id, ");
            query.append("   per.name AS per_name, ");
            query.append("   per.job_title AS job_title, ");
            query.append("   per.cpf, ");
            query.append("   per.rg, ");
            query.append("   per.cnpj, ");
            query.append("   per.rne, ");
            query.append("   per.add_id, ");
            query.append("   per.classification_cla_id, ");
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, " );
			query.append("	 usr.username, " );
			query.append("	 usr.enabled, " );
            query.append("   per_type.cla_id as per_cla_id, ");
            query.append("   per_type.value as per_cla_value, ");
            query.append("   per_type.type as per_cla_type, ");
            query.append("   per_type.label as per_cla_label, ");
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller_agent AS agt   ");
            query.append("INNER JOIN " + schemaName + "seller AS sel ON sel.sel_id = agt.agent_sel_id ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier AS per_type ON per_type.cla_id = per.classification_cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id ");
            query.append("WHERE agt.sel_id = :selId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("selId", selId);
            
            log.trace("[QUERY] sellerAgent.findBySeller: {}", query);
            return this.getJdbcTemplatePortal().query(query.toString(), params, new SellerMapper());

        } catch (Exception e) {
            log.error("Erro ao listar sellerAgent.findBySeller .", e);
            throw new AppException("Erro ao sellerAgent sellerPartner.findBySeller .", e);
        }
    }
		
	@Override
	public void save( Integer selId, Integer agentSelId ) throws AppException {
		try {
			if( selId == null || agentSelId == null || selId.equals(0) || agentSelId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de executivo e agente estão inválidos." );
			}
			
			String query = "INSERT INTO seller_agent ( sel_id, agent_sel_id) " +
					 	   "VALUES ( :selId, :agentSelId) " +
						   "ON DUPLICATE KEY UPDATE sel_id=VALUES(sel_id), agent_sel_id=VALUES(agent_sel_id)  ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "selId", selId );
	        params.addValue( "agentSelId", agentSelId );
			
			log.trace( "[QUERY] sellerAgent.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento executivo e agente", e );
			throw new AppException( "Erro ao tentar salvar executivo e agente.", e);
		}		
	}

	@Override
	public void delete(Integer selId, Integer selAgentId) throws AppException {
		try {
			
			if( (selId == null || selId.equals(0)) && (selAgentId == null ||  selAgentId.equals(0))) {
				throw new AppException( "Os IDs de relacionamento de executivo e agente estão inválido." );
			}
			
			StringBuilder query = new StringBuilder("DELETE FROM seller_agent WHERE ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if( selId != null && selId > 0) {
				query.append(" sel_id = :selId ");
				params.addValue( "selId", selId );
			}

			if( selId != null && selId > 0 && selAgentId != null && selAgentId > 0) {
				query.append(" AND ");
			}
			
			if( selAgentId != null && selAgentId > 0 ) {
				query.append(" agent_sel_id = :selAgentId ");
				params.addValue( "selAgentId", selAgentId );
			}
	
			log.trace( "[QUERY] sellerAgent.delete: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query.toString(), params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre executivo e agente." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre agente e parceiro.", e );
		}
	}

}
