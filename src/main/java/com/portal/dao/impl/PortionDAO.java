package com.portal.dao.impl;

import java.util.ArrayList;
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
import com.portal.dao.IPortionDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PortionMapper;
import com.portal.model.PortionModel;

@Repository
public class PortionDAO extends BaseDAO implements IPortionDAO {

	private static final Logger logger = LoggerFactory.getLogger(PortionDAO.class);	
	
	@Autowired
	private MessageSource messageSource;

	@Override
	public Optional<PortionModel> find(PortionModel model) throws AppException {
		Optional<PortionModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 p.por_id, " );
			query.append( "	 p.name, " );
			query.append( "	 p.tax, " );
			query.append( "	 c.cla_id, " );
			query.append( "	 c.value as cla_value, " );
			query.append( "	 c.type as cla_type " );
			query.append( "FROM " + schemaName + "portion as p  ");
			query.append( "INNER JOIN " + schemaName + "classifier as c on c.cla_id = p.payment_type ");
			query.append( "WHERE p.por_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model.getName() != null && model.getName() > 0) {
				query.append( " AND p.name like :name " ); 
				params.addValue("name", model.getName());
			}
			
			if(model.getPaymentType() != null && model.getPaymentType().getId() != null && model.getPaymentType().getId() > 0) {
				query.append( " AND c.cla_id  = :payment_type " ); 
				params.addValue("payment_type", model.getPaymentType().getId());
			}
						
			query.append( "ORDER BY c.value, p.name " );
			
			List<PortionModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new PortionMapper() );
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
	public Optional<PortionModel> getById(Integer id) throws AppException {
		Optional<PortionModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 p.por_id, " );
			query.append( "	 p.name, " );
			query.append( "	 p.tax, " );
			query.append( "	 c.cla_id, " );
			query.append( "	 c.value as cla_value, " );
			query.append( "	 c.type as cla_type " );
			query.append( "FROM " + schemaName + "portion as p  ");
			query.append( "INNER JOIN " + schemaName + "classifier as c on c.cla_id = p.payment_type ");
			query.append( "WHERE p.por_id = :por_id ");

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("por_id", id);
			
			List<PortionModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new PortionMapper() );
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
	public List<PortionModel> list() throws AppException {
		List<PortionModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 p.por_id, " );
			query.append( "	 p.name, " );
			query.append( "	 p.tax, " );
			query.append( "	 c.cla_id, " );
			query.append( "	 c.value as cla_value, " );
			query.append( "	 c.type as cla_type " );
			query.append( "FROM " + schemaName + "portion as p  ");
			query.append( "INNER JOIN " + schemaName + "classifier as c on c.cla_id = p.payment_type ");
			query.append( "WHERE p.por_id > 0 ");						
			query.append( "ORDER BY c.value, p.name " );
			
			List<PortionModel> list = this.getJdbcTemplatePortal().query( query.toString(), new PortionMapper() );
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
	public List<PortionModel> search(PortionModel model) throws AppException {
		return new ArrayList<>();
	}

	@Override
	public List<PortionModel> search(final String text) throws AppException {
		List<PortionModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	 p.por_id, " );
			query.append( "	 p.name, " );
			query.append( "	 p.tax, " );
			query.append( "	 c.cla_id, " );
			query.append( "	 c.value as cla_value, " );
			query.append( "	 c.type as cla_type " );
			query.append( "FROM " + schemaName + "portion as p  ");
			query.append( "INNER JOIN " + schemaName + "classifier as c on c.cla_id = p.payment_type ");
			query.append( "WHERE p.por_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();

			if (text != null) {
				query.append( " AND (p.name LIKE :text " );
				query.append( " OR c.value LIKE :text) " );
				params.addValue("text", mapLike(text));
			}
						
			query.append( "ORDER BY c.value, p.name " );
			
			List<PortionModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new PortionMapper() );
			if(!CollectionUtils.isEmpty(users)) {
				listReturn = users;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()), e);
		}
		
		return listReturn;
	}

	@Override
	public Optional<PortionModel> save(PortionModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query.append("INSERT INTO " + schemaName + "portion (  ");
	        query.append("name, ");
	        query.append("tax, ");
	        query.append("payment_type ");
	        query.append(") VALUES (");
	        query.append(":name, ");
	        query.append(":tax, ");
	        query.append(":payment_type ");
	        query.append(")");
	
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue("name", model.getName());
	        params.addValue("tax", model.getTax());
	        params.addValue("payment_type", model.getPaymentType().getId());
		
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
	public Optional<PortionModel> update(PortionModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
	        query.append("UPDATE " + schemaName + "portion SET ");
	        query.append("name = :name, ");
	        query.append("tax = :tax, ");
	        query.append("payment_type = :payment_type ");
	        query.append(" WHERE ");
	        query.append("por_id = :id ");

	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue("id", model.getId());
	        params.addValue("name", model.getName());
	        params.addValue("tax", model.getTax());
	        params.addValue("payment_type", model.getPaymentType().getId());

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
			query.append("DELETE FROM " + schemaName + "portion WHERE por_id = :id");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

}
