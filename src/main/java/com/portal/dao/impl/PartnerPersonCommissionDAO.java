package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.portal.config.BaseDAO;
import com.portal.dao.IPartnerPersonCommissionDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PartnerPersonCommissionMapper;
import com.portal.model.PartnerPersonCommission;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PartnerPersonCommissionDAO extends BaseDAO implements IPartnerPersonCommissionDAO {

	@Override
	public Optional<PartnerPersonCommission> find(PartnerPersonCommission model) throws AppException {
		Optional<PartnerPersonCommission> objReturn = Optional.empty();

		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ppc.*, ");
			query.append("	cla.cla_id as com_cla_id, ");
			query.append("	cla.value as com_cla_value, ");
			query.append("	cla.type as com_cla_type, ");
			query.append("	cla.label as com_cla_label ");
			query.append("FROM partner_person_commission ppc ");
			query.append("INNER JOIN classifier as cla on cla.cla_id = commission_type_cla_id ");
			query.append("WHERE ptn_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if( model.getCommissionType() != null && model.getCommissionType().getId() != null && model.getCommissionType().getId() > 0 ) {
					query.append(" AND commission_type_cla_id = :commissionType " );
					params.addValue( "commissionType", model.getCommissionType().getId() );
				}

				if( model.getDefaultValue() != null ) {
					query.append(" AND commission_default_value = :commissionDefaultValue " );
					params.addValue( "commissionDefaultValue", model.getDefaultValue() );
				}

				if( model.getPartner() != null ) {
					query.append(" AND ptn_id = :ptnId " );
					params.addValue( "ptnId", ( model.getPartner() == null ? null : model.getPartner().getId() ) );
				}
				
				if( model.getPerson() != null ) {
					query.append(" AND per_id = :perId " );
					params.addValue( "perId", ( model.getPerson() == null ? null : model.getPerson().getId() ) );
				}
			}
			
			log.trace( "[QUERY] partner_person_commission.find: {} [PARAMS]: {}", query, params.getValues() );
			
			List<PartnerPersonCommission> list = this.getJdbcTemplatePortal().query( query.toString(), params, new PartnerPersonCommissionMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				objReturn = Optional.ofNullable(list.get(0));
			}
			
			return objReturn;
		} catch (Exception e) {
			log.error( "Erro ao buscar os as comissões dos parceiros.", e );
			throw new AppException( "Erro ao buscar os as comissões dos parceiros.", e );
		}
	}
	
	@Override
	public List<PartnerPersonCommission> list(PartnerPersonCommission model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ppc.*, ");
			query.append("	cla.cla_id as com_cla_id, ");
			query.append("	cla.value as com_cla_value, ");
			query.append("	cla.type as com_cla_type, ");
			query.append("	cla.label as com_cla_label ");
			query.append("FROM partner_person_commission ppc ");
			query.append("INNER JOIN classifier as cla on cla.cla_id = commission_type_cla_id ");
			query.append("WHERE ptn_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if( model.getCommissionType() != null && model.getCommissionType().getId() != null && model.getCommissionType().getId() > 0 ) {
					query.append(" AND commission_type_cla_id = :commissionType " );
					params.addValue( "commissionType", model.getCommissionType().getId() );
				}

				if( model.getDefaultValue() != null ) {
					query.append(" AND commission_default_value = :commissionDefaultValue " );
					params.addValue( "commissionDefaultValue", model.getDefaultValue() );
				}

				if( model.getPartner() != null ) {
					query.append(" AND ptn_id = :ptnId " );
					params.addValue( "ptnId", ( model.getPartner() == null ? null : model.getPartner().getId() ) );
				}
				
				if( model.getPerson() != null ) {
					query.append(" AND per_id = :perId " );
					params.addValue( "perId", ( model.getPerson() == null ? null : model.getPerson().getId() ) );
				}
			}
			
			log.trace( "[QUERY] partner_person_commission.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PartnerPersonCommissionMapper() );
		} catch (Exception e) {
			log.error( "Erro ao buscar os as comissões dos parceiros.", e );
			throw new AppException( "Erro ao buscar os as comissões dos parceiros.", e );
		}
	}
	
	@Override
	public Optional<PartnerPersonCommission> save(PartnerPersonCommission model) throws AppException {
		try {
			String query = "INSERT INTO partner_person_commission ( commission_type_cla_id, commission_default_value, ptn_id, per_id ) " +
					       "VALUES ( :commissionType, :commissionDefaultValue, :ptnId, :perId ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "commissionType", model.getCommissionType().getId() );
			params.addValue( "commissionDefaultValue", model.getDefaultValue() );
			params.addValue( "ptnId", ( model.getPartner() == null ? null : model.getPartner().getId() ) );
			params.addValue( "perId", ( model.getPerson() == null ? null : model.getPerson().getId() ) );
	
			log.trace( "[QUERY] partner_person_commission.save: {} [PARAMS]: {}", query, params.getValues() );
			
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
		        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a comissão do parceiro: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a comissão do parceiro.", e);
		}
	}

	@Override
	public Optional<PartnerPersonCommission> update(PartnerPersonCommission model) throws AppException {
		try {
			if( model == null || model.getPartner() == null || model.getPartner().getId() == null || model.getPartner().getId().equals(0) || 
					model.getPerson() == null || model.getPerson().getId() == null || model.getPerson().getId().equals(0) ||
					model.getCommissionType() == null || model.getCommissionType().getId() == null || model.getCommissionType().getId().equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de parceiro e colaborador estão inválidos." );
			}
						
			String query = "UPDATE partner_person_commission SET commission_default_value = :commissionDefaultValue " +
					       "WHERE ptn_id = :ptnId AND per_id= :perId and commission_type_cla_id = :claId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "commissionDefaultValue", model.getDefaultValue() );
			params.addValue( "ptnId", ( model.getPartner() == null ? null : model.getPartner().getId() ) );
			params.addValue( "perId", ( model.getPerson() == null ? null : model.getPerson().getId() ) );
			params.addValue( "claId", ( model.getCommissionType() == null ? null : model.getCommissionType().getId() ) );
	
			log.trace( "[QUERY] partner_person_commission.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a comissão do parceiro: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a comissão do parceiro.", e);
		}
	}
	
	@Override
	public void delete(PartnerPersonCommission model) throws AppException {

		try {
			StringBuilder query = new StringBuilder("DELETE FROM partner_person_commission WHERE ptn_id = :ptnId AND per_id= :perId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ptnId", model.getPartner().getId() );
			params.addValue( "perId", model.getPerson().getId() );
			
			if(model.getCommissionType() != null && model.getCommissionType().getId() != null && model.getCommissionType().getId() > 0) {
				query.append(" AND commission_type_cla_id = :claId ");
				params.addValue( "claId", model.getCommissionType().getId() );
			}
			
			log.trace( "[QUERY] partner_person_commission.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query.toString(), params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a comissão do parceiro.", e );
			throw new AppException( "Erro ao excluir a comissão do parceiro.", e );
		}
		
	}
	
}
