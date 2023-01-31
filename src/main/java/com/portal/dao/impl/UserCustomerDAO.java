package com.portal.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.portal.config.BaseDAO;
import com.portal.dao.IUserCustomerDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.CustomerMapper;
import com.portal.model.CustomerModel;

@Repository
public class UserCustomerDAO extends BaseDAO implements IUserCustomerDAO {

	private static final Logger logger = LoggerFactory.getLogger(UserCustomerDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public List<CustomerModel> listUserCustomer(Integer usrId) throws AppException {
		List<CustomerModel> returnList = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT cus.*, " );
			query.append("	cla.cla_id cla_id, ");
			query.append("	cla.type cla_type, ");
			query.append("	cla.value cla_value ");
			query.append( "FROM " + schemaName + "customer cus " );
 			query.append( "JOIN " + schemaName + "user_customer uc ON cus.cus_id =  uc.cus_id " );
			query.append( "LEFT JOIN " + schemaName + "classifier AS cla ON cus.type_cla = cla.cla_id ");
			query.append( "WHERE uc.usr_id = :usrId " );

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "usrId", usrId );
			List<CustomerModel> customers = this.getJdbcTemplatePortal().query( query.toString(), params, new CustomerMapper() );
			if(!CollectionUtils.isEmpty(customers)) {
				returnList = customers;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return returnList;
	}
	
	@Override
	public void saveUserCustomer(Integer usrId, Integer cusId) throws AppException {
		try { 
			StringBuilder query = new StringBuilder();
			query.append( "INSERT INTO " + schemaName + "user_customer VALUES (:usr_id, :cus_id)" );
	
	        MapSqlParameterSource params = new MapSqlParameterSource().addValue("usr_id", usrId).addValue("cus_id", cusId);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch (DataIntegrityViolationException e) {
		}
	}

	@Override
	public void saveUserCustomer(final Integer usrId, final List<CustomerModel> list) throws AppException {
		StringBuilder query = new StringBuilder("");
		query.append( "INSERT INTO " + schemaName + "user_customer VALUES (?, ?)" );

		try {
			this.getJdbcTemplate().batchUpdate(query.toString(), new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					CustomerModel customer = list.get(i);
					ps.setInt(1, usrId);
					ps.setInt(2, customer.getId());
				}
	
				@Override
				public int getBatchSize() {
					return list.size();
				}
			});
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public void deleteUserCustomer(Integer usrId, Integer cusId) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("DELETE FROM " + schemaName + "user_customer WHERE ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(usrId != null && usrId > 0) {
				query.append("usr_id = :usrId ");
				params.addValue("usrId", usrId);
			}
			
			if(usrId != null && usrId > 0 && cusId != null && cusId > 0) {
				query.append("AND");
			}
			
			if(cusId != null && cusId > 0) {
				query.append(" cus_id = :cusId ");
				params.addValue("cusId", cusId);
			}
			
			
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public void deleteUserCustomer(final Integer usrId, final List<CustomerModel> list) throws AppException {
		StringBuilder query = new StringBuilder("");
		query.append( "DELETE FROM " + schemaName + "user_customer WHERE usr_id = ? AND cus_id = ?" );

		try {
			this.getJdbcTemplate().batchUpdate(query.toString(), new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					CustomerModel customer = list.get(i);
					ps.setInt(1, usrId);
					ps.setInt(2, customer.getId());
				}
	
				@Override
				public int getBatchSize() {
					return list.size();
				}
			});
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

}
