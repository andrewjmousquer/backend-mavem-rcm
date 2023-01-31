package com.portal.dao.impl;

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
import com.portal.dao.IAuditDAO;
import com.portal.exceptions.AppException;
import com.portal.model.AuditModel;

@Repository
public class AuditDAO extends BaseDAO implements IAuditDAO {

	private static final Logger logger = LoggerFactory.getLogger(AuditDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	public void save(AuditModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO " + schemaName + "audit ( log_date, ip, hostname, username, operation, details ) ");
			query.append("VALUES ( :logDate, :ip, :hostname, :username, :operation, :details )");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("logDate", model.getDate() );
			params.addValue("ip", model.getIp() );
			params.addValue("hostname", model.getHostname() );
			params.addValue("username", model.getUsername() );
			params.addValue("operation", model.getOperation().toString() );
			params.addValue("details", model.getDetails() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
			this.getJdbcTemplatePortal().update( query.toString(), params, keyHolder );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
}
