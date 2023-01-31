package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.portal.config.BaseDAO;
import com.portal.dao.ICountryDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.CountryMapper;
import com.portal.model.CountryModel;

@Repository
public class CountryDAO extends BaseDAO implements ICountryDAO {

	private static final Logger logger = LoggerFactory.getLogger(CountryDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<CountryModel> find(CountryModel model) throws AppException {
		Optional<CountryModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT * " );
			query.append( "FROM " + schemaName + "country " );
			query.append( "WHERE " );
			query.append( " cou_id <=> coalesce(:couId, cou_id) " );
			query.append( "	AND name <=> coalesce(:name, name) " );
			query.append( "ORDER BY cou_id " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("cou_id", model.getId());
			params.addValue("name", model.getName());
			
			List<CountryModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new CountryMapper() );
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
	public Optional<CountryModel> getById(Integer id) throws AppException {
		Optional<CountryModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT * " );
			query.append( "FROM " + schemaName + "country " );
			query.append( "WHERE " );
			query.append( " cout_id :couId " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("couId", id);
			
			List<CountryModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new CountryMapper() );
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
	public List<CountryModel> list() throws AppException {
		List<CountryModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT * " );
			query.append( "FROM " + schemaName + "country " );
			query.append( "ORDER BY name " );
			
			List<CountryModel> users = this.getJdbcTemplatePortal().query( query.toString(), new CountryMapper() );
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
	public List<CountryModel> search(CountryModel model) throws AppException {
		List<CountryModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT * " );
			query.append( "FROM " + schemaName + "country " );
			query.append( "WHERE " );
			query.append( " cou_id <=> coalesce(:couId, cou_id) " );
			query.append( "	AND name <=> coalesce(:name, name) " );
			query.append( "	AND abbreviation <=> coalesce(:abbreviation, abbreviation) " );
			query.append( "ORDER BY ste_id " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("couId", model.getId());
			params.addValue("name", model.getName());
			params.addValue("abbreviation", model.getAbbreviation());
			
			List<CountryModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new CountryMapper() );
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
	public Optional<CountryModel> save(CountryModel model) throws AppException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<CountryModel> update(CountryModel model) throws AppException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Integer id) throws AppException {
		throw new UnsupportedOperationException();
	}
}
