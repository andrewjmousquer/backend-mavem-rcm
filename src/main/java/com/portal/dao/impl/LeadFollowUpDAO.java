package com.portal.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.ILeadFollowUpDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.LeadFollowUpMapper;
import com.portal.model.LeadFollowUp;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class LeadFollowUpDAO extends BaseDAO implements ILeadFollowUpDAO {

	@Override
	public Optional<LeadFollowUp> find(LeadFollowUp model) throws AppException {
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Optional<LeadFollowUp> getById(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT fup.* ");			
			query.append("FROM " + schemaName + "lead_fup fup ");			
			query.append("WHERE fup.lfp_id = :id ");
			query.append("LIMIT 1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] leadFollowUp.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new LeadFollowUpMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a lead followup.", e );
			throw new AppException( "Erro ao consultar a followup.", e );
		}	
	}

	@Override
	public List<LeadFollowUp> list() throws AppException {
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public List<LeadFollowUp> search(LeadFollowUp model) throws AppException {
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Optional<LeadFollowUp> save(LeadFollowUp model) throws AppException {
		try {
			String query = "INSERT INTO " + schemaName + "`lead_fup` (`lfp_id`, `led_id`, `date`, `media_cla_id`, `person`, `comment`) "
					+ "VALUES (NULL, :ledId, :date, :mediaClaId, :person, :comment)";

			MapSqlParameterSource params = new MapSqlParameterSource();			
			params.addValue("date", PortalTimeUtils.localDateTimeFormat(model.getDate(), "yyyy-MM-dd HH:mm:ss"));
			params.addValue("ledId", ( model.getLead().getId() ));
			params.addValue("mediaClaId", ( model.getMedia() != null ? model.getMedia().getId() : null ));
			params.addValue("person", ( model.getPerson() ));					
			params.addValue("comment", ( model.getComment() ));			
	
			log.trace( "[QUERY] leadFollowUp.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a lead: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a lead.", e);
		}
	}

	@Override
	public Optional<LeadFollowUp> update(LeadFollowUp model) throws AppException {
		try {
			String query = "UPDATE " + schemaName + "lead_fup SET "
					+ "date=:date, "
					+ "media_cla_id=:mediaClaId, "
					+ "person=:person, "
					+ "comment=:comment "
					+ "WHERE lfp_id = :id";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId() );
			params.addValue("date", PortalTimeUtils.localDateTimeFormat( model.getDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue("mediaClaId", ( model.getMedia() != null ? model.getMedia().getId() : null ));
			params.addValue("person", ( model.getPerson() ));
			params.addValue("comment", ( model.getComment() ));

			log.trace( "[QUERY] lead.leadFollowUp: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);

	        return Optional.ofNullable(model);

		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a lead: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a lead.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM " + schemaName + "lead_fup WHERE lfp_id = :id";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] leadFollowUp.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);

		} catch (Exception e) {
			log.error( "Erro ao excluir o Lead Follow Up" , e );
			throw new AppException( "Erro ao excluir o Lead Follow Up.", e );
		}
	}

	@Override
	public List<LeadFollowUp> find(LeadFollowUp model, Pageable pageable) throws AppException {
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public List<LeadFollowUp> findByLeadId(Integer leadId) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT fup.* ");			
			query.append("FROM " + schemaName + "lead_fup fup ");			
			query.append("WHERE fup.led_id = :leadId ");			
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "leadId", leadId );

			log.trace( "[QUERY] leadFollowUp.findByLeadId: {} [PARAMS]: {}", query, params.getValues() );

			Optional<List<LeadFollowUp>> followUps = Optional.ofNullable( this.getJdbcTemplatePortal().query( query.toString(), params, new LeadFollowUpMapper() ) );
			
			if(followUps.isPresent()) {
				return followUps.get();
			}
		
			return new ArrayList<LeadFollowUp>();
		} catch (Exception e) {
			log.error( "Erro ao consultar a followup lead.", e );
			throw new AppException( "Erro ao consultar a followup lead.", e );
		}
	}

}
