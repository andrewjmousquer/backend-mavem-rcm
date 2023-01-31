package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IPriceListPartnerDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PartnerMapper;
import com.portal.mapper.PriceListMapper;
import com.portal.model.Partner;
import com.portal.model.PriceList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PriceListPartnerDAO extends BaseDAO implements IPriceListPartnerDAO {
	
	@Override
	public Optional<PriceList> getPriceList( Integer prlId, Integer ptnId ) throws AppException {
		try {
			
			if( prlId == null || ptnId == null || prlId.equals(0) || ptnId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de lista de preço e/ou parceiro estão inválido." );
			}
			
			StringBuilder query = new StringBuilder();
	
			query.append("SELECT prl.*, ");
			query.append("		 chn.name AS chn_name, ");
			query.append("		 chn.active AS chn_active ");
			query.append("FROM partner ptn ");
			query.append("INNER JOIN price_list_partner ppl ON ppl.ptn_id = ptn.ptn_id ");
			query.append("INNER JOIN price_list prl ON prl.prl_id = ppl.prl_id ");
			query.append("INNER JOIN channel chn ON chn.chn_id = prl.chn_id ");
			query.append("WHERE ppl.ptn_id = :ptnId ");
			query.append("AND ppl.prl_id = :prlId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ptnId", ptnId);
			params.addValue("prlId", prlId);
	
			log.trace( "[QUERY] priceListPartner.getPriceList: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new PriceListMapper() ) );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre lista de preço e parceiro.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre lista de preço e parceiro.", e );
		}
	}
	
	@Override
	public Optional<Partner> getPartner( Integer prlId, Integer ptnId ) throws AppException {
		try {
			
			if( prlId == null || ptnId == null || prlId.equals(0) || ptnId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de lista de preço e/ou parceiro estão inválido." );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  ptn.*, ");
			query.append( "		  sit.cla_id as sit_cla_id," );
			query.append( "		  sit.value as sit_cla_value, " );
			query.append( "		  sit.type as sit_cla_type, " );
			query.append( "	   	  sit.label as sit_cla_label, " );
			query.append("		  chn.name AS chn_name, ");
			query.append("		  chn.active AS chn_active, ");
			query.append("		  per.name AS per_name, ");
			query.append("		  per.job_title AS per_job_title, ");
			query.append("		  per.cpf AS per_cpf, ");
			query.append("		  per.cnpj AS per_cnpj, ");
			query.append("		  per.rg AS per_rg, ");
			query.append("	 	  per.rne AS per_rne, ");
			query.append("		  typ.cla_id as per_cla_id, " );
			query.append("		  typ.value as per_cla_value, " );
			query.append("		  typ.type as per_cla_type, " );
			query.append("		  typ.label as per_cla_label, " );
			query.append("		  per.add_id AS per_add_id, ");
			query.append("		  IFNULL(ptg.ptg_id, 0) AS ptg_id, " );
			query.append("		  ptg.name AS ptg_name " );
			query.append("FROM partner ptn ");
			query.append("INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " );
			query.append("INNER JOIN channel chn ON chn.chn_id = ptn.chn_id ");
			query.append("INNER JOIN person per ON per.per_id = ptn.entity_per_id ");
			query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN price_list_partner ppl ON ppl.ptn_id = ptn.ptn_id ");
			query.append("INNER JOIN price_list prl ON prl.prl_id = ppl.prl_id ");
			query.append("LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id ");
			query.append("WHERE ppl.ptn_id = :ptnId ");
			query.append("AND ppl.prl_id = :prlId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ptnId", ptnId);
			params.addValue("prlId", prlId);
	
			log.trace( "[QUERY] priceListPartner.getPartner: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new PartnerMapper() ) );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre lista de preço e parceiro.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre lista de preço e parceiro.", e );
		}
	}
	
	@Override
	public List<PriceList> findByPartner( Integer ptnId ) throws AppException {
		try {
			if( ptnId == null || ptnId.equals(0) ) {
				throw new AppException( "ID do parceiro está inválido." );
			}
			
			StringBuilder query = new StringBuilder();
	
			query.append("SELECT prl.*, ");
			query.append("		 chn.name AS chn_name, ");
			query.append("		 chn.active AS chn_active ");
			query.append("FROM partner ptn ");
			query.append("INNER JOIN price_list_partner ppl ON ppl.ptn_id = ptn.ptn_id ");
			query.append("INNER JOIN price_list prl ON prl.prl_id = ppl.prl_id ");
			query.append("INNER JOIN channel chn ON chn.chn_id = prl.chn_id ");
			query.append("WHERE ppl.ptn_id = :ptnId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ptnId", ptnId);
	
			log.trace( "[QUERY] priceListPartner.findByPartner: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceListMapper() );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre lista de preço e parceiro usando o parceiro.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre lista de preço e parceiro usando o parceiro.", e );
		}
	}

	@Override
	public List<Partner> findByPriceList( Integer prlId ) throws AppException {
		try {
			
			if( prlId == null || prlId.equals(0) ) {
				throw new AppException( "O ID da lista de preço está inválido." );
			}
			
			StringBuilder query = new StringBuilder();
	
			query.append("SELECT  ptn.*, ");
			query.append( "		  sit.cla_id as sit_cla_id," );
			query.append( "		  sit.value as sit_cla_value, " );
			query.append( "		  sit.type as sit_cla_type, " );
			query.append( "		  sit.label as sit_cla_label, " );
			query.append("		  chn.name AS chn_name, ");
			query.append("		  chn.active AS chn_active, ");
			query.append("		  per.name AS per_name, ");
			query.append("		  per.job_title AS per_job_title, ");
			query.append("		  per.cpf AS per_cpf, ");
			query.append("		  per.cnpj AS per_cnpj, ");
			query.append("		  per.rg AS per_rg, ");
			query.append("	 	  per.rne AS per_rne, ");
			query.append("		  typ.cla_id as per_cla_id, " );
			query.append("		  typ.value as per_cla_value, " );
			query.append("		  typ.type as per_cla_type, " );
			query.append("		  typ.label as per_cla_label, " );			
			query.append("		  per.add_id AS per_add_id, ");
			query.append("		  IFNULL(ptg.ptg_id, 0) AS ptg_id, " );
			query.append("		  ptg.name AS ptg_name " );
			query.append("FROM partner ptn ");
			query.append("INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " );
			query.append("INNER JOIN channel chn ON chn.chn_id = ptn.chn_id ");
			query.append("INNER JOIN person per ON per.per_id = ptn.entity_per_id ");
			query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN price_list_partner ppl ON ppl.ptn_id = ptn.ptn_id ");
			query.append("INNER JOIN price_list prl ON prl.prl_id = ppl.prl_id ");
			query.append("LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id ");
			query.append("WHERE ppl.prl_id = :prlId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("prlId", prlId);
	
			log.trace( "[QUERY] partnerPerson.findByBrand: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PartnerMapper() );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre lista de preço e parceiro usando o a lista de preço.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre lista de preço e parceiro usando o a lista de preço.", e );
		}
	}
	
	@Override
	public void save(Integer prlId, Integer ptnId) throws AppException {
		try {
			if( prlId == null || ptnId == null || prlId.equals(0) || ptnId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de lista de preço e parceiro estão inválido." );
			}
			
			String query = "INSERT INTO price_list_partner ( ptn_id, prl_id ) " +
					 	   "VALUES ( :ptnId, :prlId ) " +
						   "ON DUPLICATE KEY UPDATE prl_id=VALUES(prl_id), ptn_id=VALUES(ptn_id) ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "ptnId", ptnId );
			params.addValue( "prlId", prlId );
			
			log.trace( "[QUERY] priceListPartner.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento de lista de preço com parceiro.", e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de lista de preço com parceiro.", e);
		}		
	}

	@Override
	public void delete(Integer prlId, Integer ptnId) throws AppException {
		try {
			
			if( prlId == null || ptnId == null || prlId.equals(0) || ptnId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de lista de preço e parceiro estão inválido." );
			}
			
			String query = 	"DELETE FROM price_list_partner WHERE prl_id = :prlId AND ptn_id = :ptnId";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "prlId", prlId );
			params.addValue( "ptnId", ptnId );
	
			log.trace( "[QUERY] priceListPartner.delete: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query, params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre lista de preço e parceiro." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre lista de preço e parceiro.", e );
		}
	}

	@Override
	public void deleteByPriceList(Integer prlId) throws AppException {
		try {
			if( prlId == null || prlId.equals(0)  ) {
				throw new AppException( "O ID de relacionamento de lista de preço está inválido." );
			}
			
			String query = 	"DELETE FROM price_list_partner WHERE prl_id = :prlId";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "prlId", prlId );
	
			log.trace( "[QUERY] priceListPartner.deleteByPriceList: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query, params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre lista de preço e parceiro." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre lista de preço e parceiros.", e );
		}
	}
}
