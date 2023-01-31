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
import com.portal.dao.ICheckpointDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.CheckpointMapper;
import com.portal.model.CheckpointModel;

@Repository
public class CheckpointDAO extends BaseDAO implements ICheckpointDAO {

	private static final Logger logger = LoggerFactory.getLogger(CheckpointDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<CheckpointModel> find(CheckpointModel model) throws AppException {
		Optional<CheckpointModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append("  c.ckp_id, ");
			query.append("  c.name, ");
			query.append("  c.description ");
			query.append(" FROM " + schemaName + "checkpoint as c ");
			query.append(" WHERE c.ckp_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND c.ckp_id = :ckpId ");
					params.addValue("ckpId", model.getId());
				}

				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND c.name = :name " );
					params.addValue("name", model.getName());
				}
				
				if(model.getDescription() != null  && !model.getDescription().equals("")) {
					query.append(" AND c.description like :description " );
					params.addValue("description", model.getDescription());
				}
			}
					
			query.append(" ORDER BY c.name " );
			
			List<CheckpointModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new CheckpointMapper() );
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
	public Optional<CheckpointModel> getById(Integer id) throws AppException {
		Optional<CheckpointModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append("  c.ckp_id, ");
			query.append("  c.name, ");
			query.append("  c.description ");
			query.append(" FROM " + schemaName + "checkpoint as c ");
			query.append(" WHERE c.ckp_id = :ckpId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ckpId", id );
			
			List<CheckpointModel> returnList = this.getJdbcTemplatePortal().query( query.toString(), params, new CheckpointMapper() );
			if(!CollectionUtils.isEmpty(returnList)) {
				objReturn = Optional.ofNullable(returnList.get(0));
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return objReturn;
	}

	@Override
	public List<CheckpointModel> list() throws AppException {
		List<CheckpointModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append("  c.ckp_id, ");
			query.append("  c.name, ");
			query.append("  c.description ");
			query.append(" FROM " + schemaName + "checkpoint as c ");
			query.append(" WHERE c.ckp_id > 0 ");
			query.append(" ORDER BY c.name " );
			
			List<CheckpointModel> list = this.getJdbcTemplatePortal().query( query.toString(), new CheckpointMapper() );
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
	public List<CheckpointModel> search(CheckpointModel model) throws AppException {
		List<CheckpointModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append("  c.ckp_id, ");
			query.append("  c.name, ");
			query.append("  c.description ");
			query.append(" FROM " + schemaName + "checkpoint as c ");
			query.append(" WHERE c.ckp_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND c.ckp_id = :ckpId ");
					params.addValue("ckpId", model.getId());
				}

				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND c.name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
				}
				
				if(model.getDescription() != null ) {
					query.append(" AND c.description like :description " );
					params.addValue("description", this.mapLike(model.getDescription()));
				}

			}
			
			query.append( " ORDER BY name " );
			
			List<CheckpointModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new CheckpointMapper() );
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
	public Optional<CheckpointModel> save(CheckpointModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO " + schemaName + "checkpoint ( ");
			query.append("name, ");
			query.append("description ");
			query.append(") VALUES (");
			query.append(":name, ");
			query.append(":description ");
			query.append(")");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("description", model.getDescription());
			
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
	public Optional<CheckpointModel> update(CheckpointModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE " + schemaName + "checkpoint SET ");
			query.append("name = :name, ");
			query.append("description = :description ");
			query.append("WHERE ckp_id = :ckp_id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("description", model.getDescription());
			params.addValue("ckp_id", model.getId());
			
			this.getJdbcTemplatePortal().update( query.toString(), params );
			
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
			query.append("DELETE FROM " + schemaName + "checkpoint WHERE ckp_id = :id");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
}
