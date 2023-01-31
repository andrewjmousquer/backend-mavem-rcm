package com.portal.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IPersonQualificationDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PersonQualificationMapper;
import com.portal.model.PersonQualification;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PersonQualificationDAO extends BaseDAO implements IPersonQualificationDAO {

	@Override
	public List<PersonQualification> find( PersonQualification model ) throws AppException {
		try {
			boolean hasFilter = false;
			
			StringBuilder query = new StringBuilder();

			query.append("SELECT pqf.*, ");
			query.append("		 per.name AS per_name, " );
			query.append("		 per.job_title AS per_job_title, " );
			query.append("		 per.cpf AS per_cpf, " );
			query.append("		 per.cnpj AS per_cnpj, " );
			query.append("		 per.rg AS per_rg, " );
			query.append("		 per.rne AS per_rne, " );
			query.append("		 per.classification_cla_id AS per_cla_id, " );
			query.append("		 per.add_id AS per_add_id, " );
			query.append("		 qlf.name AS qlf_name, ");
			query.append("		 qlf.seq AS qlf_seq ");
			query.append("FROM person_qualification pqf ");
			query.append("INNER JOIN person per ON per.per_id = pqf.per_id ");
			query.append("INNER JOIN qualification qlf ON qlf.qlf_id = pqf.qlf_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getPerson() != null && model.getPerson().getId() != null) {
					query.append(" AND pqf.per_id = :perId ");
					params.addValue("perId", model.getPerson().getId());
					hasFilter = true;
				}
				
				if( model.getQualification() != null && model.getQualification().getId() != null) {
					query.append(" AND pqf.qlf_id = :qlfId ");
					params.addValue("qlfId", model.getQualification().getId());
					hasFilter = true;
				}
			}

			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] personQualification.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PersonQualificationMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre pessoa e qualificação.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre pessoa e qualificação.", e );
		}
	}

	@Override
	public void save( PersonQualification qualification ) throws AppException {
		try {
			if( qualification == null || qualification.getPerson() == null || qualification.getPerson().getId() == null || 
					qualification.getQualification() == null || qualification.getQualification().getId() == null ) {
				throw new AppException( "Objeto de relacionamento de pessoa e qualificação inválido." );
			}
			
			String query = "INSERT INTO person_qualification ( per_id, qlf_id, comments ) " +
					 	   "VALUES ( :perId, :qlfId, :comments ) " +
						   "ON DUPLICATE KEY UPDATE per_id=VALUES(per_id), qlf_id=VALUES(qlf_id), comments=VALUES(comments) ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "perId", qualification.getPerson().getId() );
			params.addValue( "qlfId", qualification.getQualification().getId() );
			params.addValue( "comments", qualification.getComments() );
			
			log.trace( "[QUERY] personQualification.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
			
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento de pessoa com qualification. {}", qualification, e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de pessoa com qualification.", e);
		}
	}

	@Override
	public void delete( Integer perId, Integer qlfId ) throws AppException {
		try {
			String query = 	"DELETE FROM person_qualification WHERE per_id = :perId AND qlf_id = :qlfId";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "perId", perId );
			params.addValue( "qlfId", qlfId );
	
			log.trace( "[QUERY] personQualification.delete: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre pessoa e qualificação. PER_ID: {}, QLF_ID: {}", perId, qlfId , e );
			throw new AppException( "Erro ao excluir os relacionamento entre pessoa e qualificação.", e );
		}
	}

	@Override
	public void deleteByPerson(Integer perId) throws AppException {
		try {
			String query = 	"DELETE FROM person_qualification WHERE per_id = :perId";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "perId", perId );
	
			log.trace( "[QUERY] personQualification.deleteByPerson: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre pessoa e qualificação. PER_ID: {}", perId, e );
			throw new AppException( "Erro ao excluir os relacionamento entre pessoa e qualificação.", e );
		}		
	}

}
