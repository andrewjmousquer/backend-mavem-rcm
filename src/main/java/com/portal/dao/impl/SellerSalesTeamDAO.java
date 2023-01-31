package com.portal.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.ISellerSalesTeamDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.SalesTeamMapper;
import com.portal.mapper.SellerMapper;
import com.portal.model.SalesTeam;
import com.portal.model.Seller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SellerSalesTeamDAO extends BaseDAO implements ISellerSalesTeamDAO {

	@Override
    public List<SalesTeam> findBySeller(Integer selId) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   slt.slt_id as slt_id, ");
            query.append("   slt.name as name ");
            query.append("FROM " + schemaName + "sales_team_seller AS sts   ");
            query.append("INNER JOIN " + schemaName + "sales_team AS slt ON slt.slt_id = sts.slt_id   ");
            query.append("INNER JOIN " + schemaName + "seller AS sel ON sel.sel_id = sts.sel_id   ");
            query.append("WHERE sel.sel_id = :selId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("selId", selId);
            
            log.trace("[QUERY] salesTeamSeller.findBySeller: {}", query);
            return this.getJdbcTemplatePortal().query(query.toString(), params, new SalesTeamMapper());

        } catch (Exception e) {
            log.error("Erro ao listar salesTeamSeller.findBySeller .", e);
            throw new AppException("Erro ao listar salesTeamSeller.findBySeller .", e);
        }
    }
	
	@Override
    public List<Seller> findBySalesTeam(Integer sltdId) throws AppException {
        try {
        	
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            query.append("   sel.sel_id AS sel_id, ");
            query.append("   per.per_id, ");
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
            query.append("   job.job_id, ");
            query.append("   per_type.cla_id as per_cla_id, ");
            query.append("   per_type.value as per_cla_value, ");
            query.append("   per_type.type as per_cla_type, ");
            query.append("   per_type.label as per_cla_label, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "sales_team_seller AS sts   ");
            query.append("INNER JOIN " + schemaName + "sales_team AS slt ON slt.slt_id = sts.slt_id   ");
            query.append("INNER JOIN " + schemaName + "seller AS sel ON sel.sel_id = sts.sel_id   ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id ");
            query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id ");
            query.append("INNER JOIN " + schemaName + "classifier AS per_type ON per_type.cla_id = per.classification_cla_id ");
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("WHERE slt.slt_id = :sltId ");
            			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("sltId", sltdId);
            
            log.trace("[QUERY] salesTeamSeller.findBySalesTeam: {}", query);
            return this.getJdbcTemplatePortal().query(query.toString(), params, new SellerMapper());

        } catch (Exception e) {
            log.error("Erro ao listar salesTeamSeller.findBySalesTeam.", e);
            throw new AppException("Erro ao listar  salesTeamSeller.findBySalesTeam .", e);
        }
    }
	
	@Override
	public void save( Integer selId, Integer sltId ) throws AppException {
		try {
			if( selId == null || sltId == null || selId.equals(0) || sltId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de seller e sales team estão inválidos." );
			}
			
			String query = "INSERT INTO sales_team_seller ( slt_id, sel_id ) " +
					 	   "VALUES ( :sltId, :selId) " +
						   "ON DUPLICATE KEY UPDATE slt_id=VALUES(slt_id), sel_id=VALUES(sel_id) ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "sltId", sltId );
	        params.addValue( "selId", selId );
			
			log.trace( "[QUERY] salesTeamSeller.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento célula de vendas e vendedor", e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de  célula de vendas e vendedor.", e);
		}		
	}

	@Override
	public void delete(Integer selId, Integer sltId) throws AppException {
		try {
			
			if( (selId == null || selId.equals(0)) && (sltId == null ||   sltId.equals(0))) {
				throw new AppException( "Os IDs de relacionamento de marca e parceiro estão inválido." );
			}
			
			StringBuilder query = new StringBuilder("DELETE FROM sales_team_seller WHERE ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if( selId != null && selId > 0) {
				query.append(" sel_id = :selId ");
				params.addValue( "selId", selId );
			}

			if( selId != null && selId > 0 && sltId != null && sltId > 0) {
				query.append(" AND ");
			}
			
			if( sltId != null && sltId > 0 ) {
				query.append(" slt_id = :sltId ");
				params.addValue( "sltId", sltId );
			}
	
			log.trace( "[QUERY] salesTeamSeller.delete: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query.toString(), params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre marca e parceiro." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre marca e parceiro.", e );
		}
	}
}
