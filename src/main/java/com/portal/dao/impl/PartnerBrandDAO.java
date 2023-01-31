package com.portal.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IPartnerBrandDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.BrandMapper;
import com.portal.mapper.PartnerMapper;
import com.portal.model.Brand;
import com.portal.model.Partner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PartnerBrandDAO extends BaseDAO implements IPartnerBrandDAO {
	
	public List<Brand> findByPartner( Integer ptnId ) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT brd.* ");
			query.append("FROM partner_brand ptb ");
			query.append("INNER JOIN brand brd ON ptb.brd_id = brd.brd_id ");
            query.append("WHERE ptb.ptn_id = :ptnId ");
	        
	        MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ptnId", ptnId);
            
            log.trace("[QUERY] partnerBrand.findByPartner: {}", query);
            return this.getJdbcTemplatePortal().query(query.toString(), params, new BrandMapper());

        } catch (Exception e) {
            log.error("Erro ao listar partnerBrand.findByPartner .", e);
            throw new AppException("Erro ao listar partnerPerson.findByPartner .", e);
        }
    }
	
	public List<Partner> findByBrand( Integer brdId ) throws AppException {
		 try {
            StringBuilder query = new StringBuilder();
            query.append( "SELECT  	ptn.*, " );
			query.append( "			chn.name AS chn_name, " );
			query.append( "			chn.active AS chn_active, " );
			query.append( "			per.name AS per_name, " );
			query.append( "			per.job_title AS per_job_title, " );
			query.append( "			per.cpf AS per_cpf, " );
			query.append( "			per.cnpj AS per_cnpj, " );
			query.append( "			per.rg AS per_rg, " );
			query.append( "			per.rne AS per_rne, " );
			query.append( "			per.add_id AS per_add_id, " );
			query.append( "			typ.cla_id as per_cla_id, " );
			query.append( "			typ.value as per_cla_value, " );
			query.append( "			typ.type as per_cla_type, " );
			query.append( "			typ.label as per_cla_label, " );
			query.append( "			IFNULL(ptg.ptg_id, 0) AS ptg_id, " );
			query.append( "			ptg.name AS ptg_name " );
			query.append( "FROM partner_brand ptb ");
			query.append( "INNER JOIN partner ptn on ptb.ptn_id = ptn.ptn_id " );
			query.append( "INNER JOIN person per ON per.per_id = ptn.entity_per_id " );
			query.append( "INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " );
			query.append( "INNER JOIN channel chn ON chn.chn_id = ptn.chn_id  " );
			query.append( "LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id " );
            query.append("WHERE ptb.brd_id = :brdId ");
            			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("brdId", brdId);
            
            log.trace("[QUERY] partnerBrand.findByBrand: {}", query);
            return this.getJdbcTemplatePortal().query(query.toString(), params, new PartnerMapper());

        } catch (Exception e) {
            log.error("Erro ao listar partnerBrand.findByBrand.", e);
            throw new AppException("Erro ao listar  partnerBrand.findByBrand .", e);
        }
    }
	
	public void save( Integer ptnId, Integer brdId ) throws AppException {
		try {
			if( ptnId == null || brdId == null || ptnId.equals(0) || brdId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de parceiro e colaborador estão inválidos." );
			}
			
			String query = "INSERT INTO partner_brand ( ptn_id, brd_id ) " +
					 	   "VALUES ( :ptnId, :brdId) " +
						   "ON DUPLICATE KEY UPDATE ptn_id=VALUES(ptn_id), brd_id=VALUES(brd_id) ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "ptnId", ptnId );
	        params.addValue( "brdId", brdId );
			
			log.trace( "[QUERY] partnerBrand.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento parceiro e marca", e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de parceiro e marca", e);
		}		
	}
	
	public void delete( Integer ptnId, Integer brdId  ) throws AppException {
		try {
			if( (ptnId == null || ptnId.equals(0)) && (brdId == null ||   brdId.equals(0))) {
				throw new AppException( "Os IDs de relacionamento de parceiro e marca estão inválido." );
			}
			
			StringBuilder query = new StringBuilder("DELETE FROM partner_brand WHERE ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if( ptnId != null && ptnId > 0) {
				query.append(" ptn_id = :ptnId ");
				params.addValue( "ptnId", ptnId );
			}

			if( ptnId != null && ptnId > 0 && brdId != null && brdId > 0) {
				query.append(" AND ");
			}
			
			if( brdId != null && brdId > 0 ) {
				query.append(" brd_id = :brdId ");
				params.addValue( "brdId", brdId );
			}
	
			log.trace( "[QUERY] partnerBrand.delete: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query.toString(), params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre parceiro e marca." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre parceiro e marca.", e );
		}
	}

}
