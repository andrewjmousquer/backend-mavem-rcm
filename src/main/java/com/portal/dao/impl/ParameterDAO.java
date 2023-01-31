package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.portal.config.BaseDAO;
import com.portal.dao.IParameterDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ParameterMapper;
import com.portal.model.ParameterModel;

@Repository
public class ParameterDAO extends BaseDAO implements IParameterDAO {

	private static final Logger logger = LoggerFactory.getLogger(ParameterDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<ParameterModel> find(ParameterModel model) throws AppException {
		Optional<ParameterModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT ");
			query.append( " prm.prm_id, " );
			query.append( "	prm.name, " );
			query.append( "	prm.value, " );
			query.append( "	prm.description " );
			query.append( "FROM " + schemaName + "parameter prm " );
			query.append( "WHERE prm.prm_id > 0 " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getId() != null && model.getId() > 0){
					query.append(" AND prm.prm_id = :prmId ");
					params.addValue( "prmId", (model.getId() != null ?model.getId() : null) );
				}
		
				if(model.getName()!= null && !model.getName().equals("")) {
					query.append(" AND prm.name = :name " );
					params.addValue( "name", model.getName() );
				}
				
				if(model.getValue()!= null && !model.getValue().equals("") ) {
					query.append(" AND prm.value = :name " );
					params.addValue( "value", model.getValue() );
				}
			}
			
			List<ParameterModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new ParameterMapper() );
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
	public Optional<ParameterModel> getById(Integer id) throws AppException {
		Optional<ParameterModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT ");
			query.append( " prm.prm_id, " );
			query.append( "	prm.name, " );
			query.append( "	prm.value, " );
			query.append( "	prm.description " );
			query.append( "FROM " + schemaName + "parameter prm " );
			query.append("WHERE prm.prm_id = :prmId ");

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "prmId", id);
			
			List<ParameterModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new ParameterMapper() );
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
	public List<ParameterModel> list() throws AppException {
		List<ParameterModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT ");
			query.append( " prm.prm_id, " );
			query.append( "	prm.name, " );
			query.append( "	prm.value, " );
			query.append( "	prm.description " );
			query.append( "FROM " + schemaName + "parameter prm " );
			query.append( "ORDER BY prm.name " );
			
			List<ParameterModel> users = this.getJdbcTemplatePortal().query( query.toString(), new ParameterMapper() );
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
	public List<ParameterModel> search(ParameterModel model) throws AppException {
		List<ParameterModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT ");
			query.append( " prm.prm_id, " );
			query.append( "	prm.name, " );
			query.append( "	prm.value, " );
			query.append( "	prm.description " );
			query.append( "FROM " + schemaName + "parameter prm " );
			query.append( "WHERE prm.prm_id > 0 " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getId() != null && model.getId() > 0){
					query.append(" AND prm.prm_id = :prmId ");
					params.addValue( "prmId", (model.getId() != null ?model.getId() : null) );
				}
		
				if(model.getName()!= null && !model.getName().equals("")) {
					query.append(" AND prm.name like :name " );
					params.addValue( "name", this.mapLike(model.getName()));
				}
				
				if(model.getValue()!= null && !model.getValue().equals("") ) {
					query.append(" AND prm.value like :value " );
					params.addValue( "value", this.mapLike(model.getValue()) );
				}
			}
			
			List<ParameterModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new ParameterMapper() );
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
	public Optional<ParameterModel> save(ParameterModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query.append("INSERT INTO " + schemaName + "parameter( name, value, description ) VALUES ");
			query.append("( :name, :value, :description )");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName() );
			params.addValue("value", model.getValue() );
			params.addValue("description", model.getDescription() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
			this.getJdbcTemplatePortal().update( query.toString(), params, keyHolder );
			model.setId( this.getKey(keyHolder) );
			
			return Optional.ofNullable(model);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<ParameterModel> update(ParameterModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE " + schemaName + "parameter SET ");
			query.append("value = :value, description = :description ");
			query.append("WHERE prm_id = :prmId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("value", model.getValue() );
			params.addValue("description", model.getDescription() );
			params.addValue("prmId", model.getId());
			
			this.getJdbcTemplatePortal().update( query.toString(), params);

			return Optional.ofNullable(model);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("DELETE FROM " + schemaName + "parameter WHERE prm_id = :id");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
}
