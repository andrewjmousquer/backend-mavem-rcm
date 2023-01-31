package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.portal.config.BaseDAO;
import com.portal.dao.IStateDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.StateMapper;
import com.portal.model.StateModel;

@Repository
public class StateDAO extends BaseDAO implements IStateDAO {
	
	private static final Logger logger = LoggerFactory.getLogger(StateDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<StateModel> find(StateModel model) throws AppException {
		Optional<StateModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, ");
			query.append( "	 s.abbreviation as abbrevState, ");
			query.append( "	 co.cou_id, ");
			query.append( "	 co.name as nameCountry, ");
			query.append( "	 co.abbreviation as abbrevCountry ");
			query.append( "FROM " + schemaName + "state as s " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append( "WHERE s.ste_id <=> coalesce(:steId, ste_id) " );
			query.append( "	AND s.name <=> coalesce(:name, name) " );
			query.append( "	AND s.abbreviation <=> coalesce(:abbreviation, abbreviation) " );
			query.append( "ORDER BY s.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("steId", model.getId());
			params.addValue("name", model.getName());
			params.addValue("abbreviation", model.getAbbreviation());
			
			List<StateModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new StateMapper() );
			if(!CollectionUtils.isEmpty(users)) {
				objReturn = Optional.ofNullable(users.get(0));
			}
			
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return objReturn;
	}

	@Override
	public Optional<StateModel> getById(Integer id) throws AppException {
		Optional<StateModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, ");
			query.append( "	 s.abbreviation as abbrevState, ");
			query.append( "	 co.cou_id, ");
			query.append( "	 co.name as nameCountry, ");
			query.append( "	 co.abbreviation as abbrevCountry ");
			query.append( "FROM " + schemaName + "state as s " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append( "WHERE s.ste_id = :steId " );
			query.append( "ORDER BY s.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("steId", id);
			
			List<StateModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new StateMapper() );
			if(!CollectionUtils.isEmpty(users)) {
				objReturn = Optional.ofNullable(users.get(0));
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return objReturn;
	}

	@Override
	public List<StateModel> list() throws AppException {
		List<StateModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, ");
			query.append( "	 s.abbreviation as abbrevState, ");
			query.append( "	 co.cou_id, ");
			query.append( "	 co.name as nameCountry, ");
			query.append( "	 co.abbreviation as abbrevCountry ");
			query.append( "FROM " + schemaName + "state as s " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append( "ORDER BY s.name " );
			
			List<StateModel> users = this.getJdbcTemplatePortal().query( query.toString(), new StateMapper() );
			if(!CollectionUtils.isEmpty(users)) {
				listReturn = users;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}

	@Override
	public List<StateModel> search(StateModel model) throws AppException {
		List<StateModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, ");
			query.append( "	 s.abbreviation as abbrevState, ");
			query.append( "	 co.cou_id, ");
			query.append( "	 co.name as nameCountry, ");
			query.append( "	 co.abbreviation as abbrevCountry ");
			query.append( "FROM " + schemaName + "state as s " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append( "WHERE s.ste_id <=> coalesce(:steId, ste_id) " );
			query.append( "	AND s.name <=> coalesce(:name, name) " );
			query.append( "	AND s.abbreviation <=> coalesce(:abbreviation, abbreviation) " );
			query.append( "ORDER BY s.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("steId", model.getId());
			params.addValue("name", model.getName());
			params.addValue("abbreviation", model.getAbbreviation());
			
			List<StateModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new StateMapper() );
			if(!CollectionUtils.isEmpty(users)) {
				listReturn = users;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}

	@Override
	public Optional<StateModel> save(StateModel model) throws AppException {
		throw new NotImplementedException( "StateDAO.save Not Implemented" );
	}

	@Override
	public Optional<StateModel> update(StateModel model) throws AppException {
		throw new NotImplementedException( "StateDAO.update Not Implemented" );
	}

	@Override
	public void delete(Integer id) throws AppException {
		throw new NotImplementedException( "StateDAO.delete Not Implemented" );
	}
	
	@Override
	public List<StateModel> getByCountryId(Integer couId) throws AppException {
		List<StateModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, ");
			query.append( "	 s.abbreviation as abbrevState, ");
			query.append( "	 co.cou_id, ");
			query.append( "	 co.name as nameCountry, ");
			query.append( "	 co.abbreviation as abbrevCountry ");
			query.append( "FROM " + schemaName + "state as s " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append( "WHERE s.cou_id = :cou_id " );
			query.append( "ORDER BY s.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("cou_id", couId);
			
			List<StateModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new StateMapper() );
			if(!CollectionUtils.isEmpty(users)) {
				listReturn = users;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}
}
