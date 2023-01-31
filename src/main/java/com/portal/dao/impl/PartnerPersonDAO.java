package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IPartnerPersonDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PartnerPersonMapper;
import com.portal.model.PartnerPerson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PartnerPersonDAO extends BaseDAO implements IPartnerPersonDAO {
	
	public Optional<PartnerPerson> getPartnerPerson( PartnerPerson model) throws AppException {
		List<PartnerPerson> partnerPersonList = this.findPartnerPerson(model);
		if(partnerPersonList != null && !partnerPersonList.isEmpty()) {
			return Optional.ofNullable(partnerPersonList.get(0));
		}
		
		return null;
	}
	
	public List<PartnerPerson> findPartnerPerson( PartnerPerson model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT "); 	
			query.append( "		ptp.person_type_cla_id as person_type_cla_id, ");
			query.append( "		ptp_person.per_id as pptp_per_id, ");
			query.append( "		ptp_person.name as pptp_name, ");
			query.append( "		ptp_person.job_title AS pptp_job_title,  ");
			query.append( "		ptp_person.cpf AS pptp_cpf,  ");
			query.append( "		ptp_person.cnpj AS pptp_cnpj,  ");
			query.append( "		ptp_person.rg AS pptp_rg,  ");
			query.append( "		ptp_person.rne AS pptp_rne,  ");
			query.append( "		ptp_person.add_id AS pptp_add_id,  ");
			query.append( "		ptp_person.classification_cla_id as pptpt_cla_id, ");
			query.append( "		ptn.ptn_id as ptn_id, ");
			query.append( "		sit.cla_id as sit_cla_id," );
			query.append( "		sit.value as sit_cla_value, " );
			query.append( "		sit.type as sit_cla_type, " );
			query.append( "	   	sit.label as sit_cla_label, " );
			query.append( "		ptn_person.per_id as pptn_per_id, ");
			query.append( "		ptn_person.name as pptn_name, ");
			query.append( "		ptn_person.job_title AS pptn_job_title,  ");
			query.append( "		ptn_person.cpf AS pptn_cpf,  ");
			query.append( "		ptn_person.cnpj AS pptn_cnpj,  ");
			query.append( "		ptn_person.rg AS pptn_rg,  ");
			query.append( "		ptn_person.rne AS pptn_rne,  ");
			query.append( "		ptn_person.add_id AS pptn_add_id,  ");
			query.append( "		ptn_person.classification_cla_id as ptnt_cla_id, ");
			query.append( "		cla.cla_id as pper_cla_id, ");
			query.append( "		cla.value as pper_cla_value, ");
			query.append( "		cla.type as pper_cla_type, ");
			query.append( "		cla.label as pper_cla_label, ");
			query.append( "		chn.chn_id AS chn_id,  ");
			query.append( "		chn.name AS chn_name,  ");
			query.append( "		chn.active AS chn_active, ");
			query.append( "		IFNULL(ptg.ptg_id, 0) AS ptg_id,  ");
			query.append( "		ptg.name AS ptg_name  ");
			query.append( "FROM partner_person ptp  ");
			query.append( "INNER JOIN classifier as cla on ptp.person_type_cla_id = cla.cla_id ");
			query.append( "INNER JOIN person ptp_person ON ptp.per_id = ptp_person.per_id  ");
			query.append( "INNER JOIN partner ptn on ptp.ptn_id = ptn.ptn_id  ");
			query.append( "INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " );
			query.append( "INNER JOIN person ptn_person ON ptn_person.per_id = ptn.entity_per_id  ");
			query.append( "INNER JOIN channel chn ON chn.chn_id = ptn.chn_id   ");
			query.append( "LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id  ");
			query.append( "WHERE ptp.ptn_id > 0  ");
	        
	        MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if(model.getPerson() != null && model.getPerson().getId() != null && model.getPerson().getId() > 0) {
					query.append( " AND ptp.per_id = :perId "); 
					params.addValue("perId", model.getPerson().getId());
				}
				
				if(model.getPartner() != null && model.getPartner().getId() != null && model.getPartner().getId() > 0) {
					query.append( " AND ptp.ptn_id = :ptnId "); 
					params.addValue("ptnId", model.getPartner().getId());
				}
			}
            
            log.trace("[QUERY] partnerPerson.find: {} [PARAMS]: {}", query, params.getValues());
            return this.getJdbcTemplatePortal().query(query.toString(), params, new PartnerPersonMapper());

        } catch (Exception e) {
            log.error("Erro ao listar partnerPerson.findPartnerPerson .", e);
            throw new AppException("Erro ao listar partnerPerson.findPartnerPerson .", e);
        }
    }
		
	public void save( PartnerPerson model ) throws AppException {
		try {
			if( model.getPartner() == null || model.getPerson() == null || 
				model.getPartner().getId() == null || model.getPerson().getId() == null ||
				model.getPartner().getId().equals(0) || model.getPerson().getId().equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de parceiro e colaborador estão inválidos." );
			}
			
			String query = "INSERT INTO partner_person ( ptn_id, per_id, person_type_cla_id ) " +
					 	   "VALUES ( :ptnId, :perId, :claId) " +
						   "ON DUPLICATE KEY UPDATE ptn_id=VALUES(ptn_id), per_id=VALUES(per_id), person_type_cla_id=VALUES(person_type_cla_id)";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "ptnId", model.getPartner().getId() );
	        params.addValue( "perId", model.getPerson().getId());
	        params.addValue( "claId", model.getPersonType().getId() );
			
			log.trace( "[QUERY] partnerPerson.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento parceiro e colaborador", e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de parceiro e colaborador", e);
		}		
	}
	
	public void update( PartnerPerson model ) throws AppException {
		try {
			if( model.getPartner() == null || model.getPerson() == null || 
				model.getPartner().getId() == null || model.getPerson().getId() == null ||
				model.getPartner().getId().equals(0) || model.getPerson().getId().equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de parceiro e colaborador estão inválidos." );
			}
			
			String query = "UPDATE partner_person set person_type_cla_id = :claId " +
					 	   "WHERE ptn_id = :ptnId and per_id = :perId ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "ptnId", model.getPartner().getId() );
	        params.addValue( "perId", model.getPerson().getId());
	        params.addValue( "claId", model.getPersonType().getId() );
			
			log.trace( "[QUERY] partnerPerson.update: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar atualizar o relacionamento parceiro e colaborador", e );
			throw new AppException( "Erro ao tentar atualizar o relacionamento de parceiro e colaborador", e);
		}		
	}
	
	public void delete( Integer ptnId, Integer perId  ) throws AppException {
		try {
			if( (ptnId == null || ptnId.equals(0)) && (perId == null ||   perId.equals(0))) {
				throw new AppException( "Os IDs de relacionamento de parceiro e colaborador estão inválido." );
			}
			
			StringBuilder query = new StringBuilder("DELETE FROM partner_person WHERE ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if( ptnId != null && ptnId > 0) {
				query.append(" ptn_id = :ptnId ");
				params.addValue( "ptnId", ptnId );
			}

			if( ptnId != null && ptnId > 0 && perId != null && perId > 0) {
				query.append(" AND ");
			}
			
			if( perId != null && perId > 0 ) {
				query.append(" per_id = :perId ");
				params.addValue( "perId", perId );
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
