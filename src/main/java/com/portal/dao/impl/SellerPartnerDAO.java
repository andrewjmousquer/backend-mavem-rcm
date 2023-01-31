package com.portal.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.ISellerPartnerDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PartnerMapper;
import com.portal.mapper.SellerMapper;
import com.portal.model.Partner;
import com.portal.model.Seller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SellerPartnerDAO extends BaseDAO implements ISellerPartnerDAO {

	@Override
    public List<Partner> findBySeller(Integer selId) throws AppException {
        try {
           
            StringBuilder query = new StringBuilder();
			query.append( "SELECT  	ptn.*, " );
			query.append( "			sit.cla_id as sit_cla_id," );
			query.append( "			sit.value as sit_cla_value, " );
			query.append( "			sit.type as sit_cla_type, " );
			query.append( "			sit.label as sit_cla_label, " );
			query.append( "			chn.name AS chn_name, " );
			query.append( "			chn.active AS chn_active, " );
			query.append( "			per.name AS per_name, " );
			query.append( "			per.job_title AS per_job_title, " );
			query.append( "			per.cpf AS per_cpf, " );
			query.append( "			per.cnpj AS per_cnpj, " );
			query.append( "			per.rg AS per_rg, " );
			query.append( "			per.rne AS per_rne, " );
			query.append( "			per.add_id AS per_add_id, " );
			query.append("	 		typ.cla_id as per_cla_id, " );
			query.append("	 		typ.value as per_cla_value, " );
			query.append("	 		typ.type as per_cla_type, " );
			query.append("	 		typ.label as per_cla_label, " );
			query.append( "			IFNULL(ptg.ptg_id, 0) AS ptg_id, " );
			query.append( "			ptg.name AS ptg_name " );
            query.append( "FROM " + schemaName + "seller_partner AS spt   ");
            query.append( "INNER JOIN " + schemaName + "seller AS sel ON sel.sel_id = spt.sel_id   ");
            
            
            query.append( "INNER JOIN " + schemaName + "partner AS ptn ON ptn.ptn_id = spt.ptn_id   ");
			query.append( "INNER JOIN " + schemaName + "classifier sit ON ptn.situation_cla = sit.cla_id " );
			query.append( "INNER JOIN " + schemaName + "person per ON per.per_id = ptn.entity_per_id " );
            query.append( "INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id " );
			query.append( "INNER JOIN channel chn ON chn.chn_id = ptn.chn_id  " );
			query.append( "LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id " );
            query.append("WHERE sel.sel_id = :selId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("selId", selId);
            
            log.trace("[QUERY] sellerPartner.findBySeller: {}", query);
            return this.getJdbcTemplatePortal().query(query.toString(), params, new PartnerMapper());

        } catch (Exception e) {
            log.error("Erro ao listar sellerPartner.findBySeller .", e);
            throw new AppException("Erro ao listar sellerPartner.findBySeller .", e);
        }
    }
	
	@Override
    public List<Seller> findByPartner(Integer ptnId) throws AppException {
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
            query.append("   per.birthdate, ");
            query.append("	 usr.usr_id, " );
			query.append("	 usr.username, " );
			query.append("	 usr.enabled, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );
            query.append("   job.job_id, ");
            query.append("   job.name as job_name ");
            query.append("FROM " + schemaName + "seller_partner AS spt ");
            query.append("INNER JOIN " + schemaName + "partner AS ptn ON ptn.ptn_id = spt.ptn_id ");
            query.append("INNER JOIN " + schemaName + "seller AS sel ON sel.sel_id = spt.sel_id ");
            query.append("INNER JOIN " + schemaName + "person AS per ON sel.per_id = per.per_id ");
			query.append("INNER JOIN " + schemaName + "user AS usr ON per.per_id = usr.per_id   ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON per.classification_cla_id = typ.cla_id " );
            query.append("INNER JOIN " + schemaName + "job AS job ON sel.job_id = job.job_id   ");
            query.append("WHERE ptn.ptn_id = :ptnId ");
            			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ptnId", ptnId);
            
            log.trace("[QUERY] sellerPartner.findByPartner: {}", query);
            return this.getJdbcTemplatePortal().query(query.toString(), params, new SellerMapper());

        } catch (Exception e) {
            log.error("Erro ao listar sellerPartner.findByPartner.", e);
            throw new AppException("Erro ao listar  sellerPartner.findByPartner .", e);
        }
    }
	
	@Override
	public void save( Integer selId, Integer ptnId ) throws AppException {
		try {
			if( selId == null || ptnId == null || selId.equals(0) || ptnId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de seller e partner estão inválidos." );
			}
			
			String query = "INSERT INTO seller_partner ( sel_id, ptn_id) " +
					 	   "VALUES ( :selId, :ptnId) " +
						   "ON DUPLICATE KEY UPDATE sel_id=VALUES(sel_id), ptn_id=VALUES(ptn_id)  ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "selId", selId );
	        params.addValue( "ptnId", ptnId );
			
			log.trace( "[QUERY] sellerPartner.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento executivo e parceiro", e );
			throw new AppException( "Erro ao tentar salvar executivo e parceiro.", e);
		}		
	}

	@Override
	public void delete(Integer selId, Integer ptnId) throws AppException {
		try {
			
			if( (selId == null || selId.equals(0)) && (ptnId == null ||   ptnId.equals(0))) {
				throw new AppException( "Os IDs de relacionamento de executivo e parceiro estão inválido." );
			}
			
			StringBuilder query = new StringBuilder("DELETE FROM seller_partner WHERE ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if( selId != null && selId > 0) {
				query.append(" sel_id = :selId ");
				params.addValue( "selId", selId );
			}

			if( selId != null && selId > 0 && ptnId != null && ptnId > 0) {
				query.append(" AND ");
			}
			
			if( ptnId != null && ptnId > 0 ) {
				query.append(" ptn_id = :ptnId ");
				params.addValue( "ptnId", ptnId );
			}
	
			log.trace( "[QUERY] sellerPartner.delete: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query.toString(), params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre executivo e parceiro." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre executivo e parceiro.", e );
		}
	}
}
