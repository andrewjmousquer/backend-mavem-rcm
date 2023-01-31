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
import com.portal.dao.ICityDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.CityMapper;
import com.portal.model.CityModel;

@Repository
public class CityDAO extends BaseDAO implements ICityDAO {

	private static final Logger logger = LoggerFactory.getLogger(CityDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<CityModel> getById(Integer id) throws AppException {
		Optional<CityModel> city = Optional.empty();
		
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 c.cit_id, ");
			query.append( "	 c.name as nameCity, " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, " );
			query.append( "	 s.abbreviation as abbrevState, " );
			query.append( "	 co.cou_id, " );
			query.append( "	 co.name as nameCountry, " );
			query.append( "	 co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "city as c " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append( "WHERE c.cit_id = :citId " );
			query.append( "ORDER BY c.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("citId", id);
			
			List<CityModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new CityMapper() );
			if(list != null && !list.isEmpty()) {
				city = Optional.ofNullable(list.get(0));
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}

		return city;
	}
	
	@Override
	public Optional<CityModel> find(CityModel model) throws AppException {
		Optional<CityModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 c.cit_id, ");
			query.append( "	 c.name as nameCity, " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, " );
			query.append( "	 s.abbreviation as abbrevState, " );
			query.append( "	 co.cou_id, " );
			query.append( "	 co.name as nameCountry, " );
			query.append( "	 co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "city as c " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append(" WHERE c.cit_id > 0 ");

			MapSqlParameterSource params = new MapSqlParameterSource();

			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND c.cit_id = :citId ");
					params.addValue( "citId", model.getId() );
				}
				
				if(model.getName() != null && !model.getName().equals("")) {
					query.append("	AND c.name = :name ");
					params.addValue( "name", model.getName() );
				}
				
				if(model.getCodIbge() != null && model.getCodIbge() > 0) {
					query.append("	AND c.cod_ibge = :codIbge ");
					params.addValue( "codIbge", model.getCodIbge() );
				}
			}
			
			if(model.getState() != null) {
				if(model.getState().getId() != null && model.getState().getId() > 0) {
					query.append("	AND c.ste_id = :steId ");				
					params.addValue( "steId", model.getState().getId());
				}
				
				if(model.getState().getName() != null && !model.getState().getName().equals("")) {
					query.append("	AND s.name = :stateName ");
					params.addValue( "stateName", model.getState().getName() );
				}
			}
			
			List<CityModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new CityMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				objReturn = Optional.ofNullable(list.get(0));
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return objReturn;
	}

	@Override
	public List<CityModel> list() throws AppException {
		List<CityModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 c.cit_id, ");
			query.append( "	 c.name as nameCity, " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, " );
			query.append( "	 s.abbreviation as abbrevState, " );
			query.append( "	 co.cou_id, " );
			query.append( "	 co.name as nameCountry, " );
			query.append( "	 co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "city as c " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append(" ORDER BY c.name " );
			
			List<CityModel> list = this.getJdbcTemplatePortal().query( query.toString(), new CityMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				listReturn = list;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}

	@Override
	public List<CityModel> search(CityModel model) throws AppException {
		List<CityModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 c.cit_id, ");
			query.append( "	 c.name as nameCity, " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, " );
			query.append( "	 s.abbreviation as abbrevState, " );
			query.append( "	 co.cou_id, " );
			query.append( "	 co.name as nameCountry, " );
			query.append( "	 co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "city as c " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append(" WHERE c.cit_id <=> coalesce(:citId, c.cit_id) ");
			query.append("	OR c.name <=> coalesce(:name, c.name) ");
			query.append("	OR c.ste_id <=> coalesce(:steId, c.ste_id) ");
			query.append(" ORDER BY c.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "citId", model.getId() );
			params.addValue( "name", model.getName() );
			params.addValue( "steId", ((model.getState() != null && model.getState().getId() > 0) ? model.getState().getId() : null) );
			
			List<CityModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new CityMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				listReturn = list;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}

	
	@Override
	public Optional<CityModel> save(CityModel model) throws AppException {
		throw new NotImplementedException( "CityDAO.save Not Implemented" );
	}
	
	@Override
	public Optional<CityModel> update(CityModel model) throws AppException {
		throw new NotImplementedException( "CityDAO.update Not Implemented" );
	}
	
	@Override
	public void delete(Integer id) throws AppException {
		throw new NotImplementedException( "CityDAO.delete Not Implemented" );
	}
	
	public List<CityModel> listAllFillState() throws AppException{
		List<CityModel> listReturn = null;

		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 c.cit_id, ");
			query.append( "	 c.name as nameCity, " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, " );
			query.append( "	 s.abbreviation as abbrevState, " );
			query.append( "	 co.cou_id, " );
			query.append( "	 co.name as nameCountry, " );
			query.append( "	 co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "city as c " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append(" ORDER BY c.name " );
			
			List<CityModel> list = this.getJdbcTemplatePortal().query( query.toString(), new CityMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				listReturn = list;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}
		
	@Override
	public List<CityModel> findFillState(CityModel model) throws AppException {
		List<CityModel> listReturn = null;
		
		try {
			StringBuilder query = new StringBuilder("");
			query.append( "SELECT " );
			query.append( "	 c.cit_id, ");
			query.append( "	 c.name as nameCity, " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, " );
			query.append( "	 s.abbreviation as abbrevState, " );
			query.append( "	 co.cou_id, " );
			query.append( "	 co.name as nameCountry, " );
			query.append( "	 co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "city as c " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append(" WHERE c.cit_id <=> coalesce(:citId, c.cit_id) ");
			query.append("	OR c.name <=> coalesce(:name, c.name) ");
			query.append("	OR c.ste_id <=> coalesce(:steId, c.ste_id) ");
			query.append(" ORDER BY c.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "citId", model.getId() );
			params.addValue( "name", model.getName() );
			params.addValue( "steId", ((model.getState() != null && model.getState().getId() > 0) ? model.getState().getId() : null) );
			
			List<CityModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new CityMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				listReturn = list;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}
	
	
	@Override
	public List<CityModel> getByState(Integer id) throws AppException {
		List<CityModel> listReturn = null;
		
		try {
			StringBuilder query = new StringBuilder("");
			query.append( "SELECT " );
			query.append( "	 c.cit_id, ");
			query.append( "	 c.name as nameCity, " );
			query.append( "	 s.ste_id, ");
			query.append( "	 s.name as nameState, " );
			query.append( "	 s.abbreviation as abbrevState, " );
			query.append( "	 co.cou_id, " );
			query.append( "	 co.name as nameCountry, " );
			query.append( "	 co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "city as c " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append(" WHERE s.ste_id = :steId ");
			query.append(" ORDER BY c.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "steId", id );
			
			List<CityModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new CityMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				listReturn = list;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}

}
