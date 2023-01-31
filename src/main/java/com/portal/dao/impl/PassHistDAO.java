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

import com.portal.config.BaseDAO;
import com.portal.dao.IPassHistDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PassHistMapper;
import com.portal.model.PassHistModel;
import com.portal.model.UserModel;
import com.portal.utils.PortalTimeUtils;

@Repository
public class PassHistDAO extends BaseDAO implements IPassHistDAO {

	private static final Logger logger = LoggerFactory.getLogger(PassHistDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public List<PassHistModel> getPassHistDescLimit(UserModel user, int limit) throws AppException {
		List<PassHistModel> hists = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT 	pas.pas_id, " );
			query.append( "			pas.password, " );
			query.append( "			pas.change_date, " );
			query.append( "			pas.usr_id " );
			query.append( "FROM " + schemaName + "pass_hist pas " );
			query.append( "WHERE pas.usr_id = :userId " );
			query.append( "ORDER BY pas.change_date DESC " );
			query.append( "LIMIT :limit " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "userId", user.getId() );
			params.addValue( "limit", limit );
			
			hists = this.getJdbcTemplatePortal().query( query.toString(), params, new PassHistMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}

		return hists;
	}
	
	public List<PassHistModel> getPassHistByUser( UserModel user ) throws AppException {
		List<PassHistModel> hists = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT 	pas.pas_id, " );
			query.append( "			pas.password, " );
			query.append( "			pas.change_date, " );
			query.append( "			pas.usr_id " );
			query.append( "FROM " + schemaName + "pass_hist pas " );
			query.append( "WHERE pas.usr_id = :userId " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "userId", user.getId() );
			
			hists = this.getJdbcTemplatePortal().query( query.toString(), params, new PassHistMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}

		return hists;
	}
	
	@Override
	public Optional<PassHistModel> save(PassHistModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append( "INSERT INTO " + schemaName + "pass_hist ( password, change_date, usr_id ) " );
			query.append( "VALUES ( :password, :changeDate, :usrId )" );
	
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "password", model.getPassword() );
			params.addValue( "changeDate", PortalTimeUtils.dateToSQLDate( model.getChangeDate() ) );
			params.addValue( "usrId", model.getUser().getId() );
			
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
	public void deleteByUser(Integer usrId) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query.append("DELETE FROM " + schemaName + "pass_hist WHERE usr_id = :usrId");
			
			MapSqlParameterSource params = new MapSqlParameterSource("usrId", usrId);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

}
