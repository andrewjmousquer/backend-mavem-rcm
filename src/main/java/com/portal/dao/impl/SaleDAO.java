package com.portal.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.portal.config.BaseDAO;
import com.portal.dao.ISaleDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.SaleMapper;
import com.portal.model.SaleModel;
import com.portal.utils.PortalTimeUtils;

@Repository
public class SaleDAO extends BaseDAO implements ISaleDAO {

	private static final Logger logger = LoggerFactory.getLogger(SaleDAO.class);	
	
	@Autowired
	private MessageSource messageSource;

	@Override
	public Optional<SaleModel> find(SaleModel model) throws AppException {
		Optional<SaleModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query
			.append( "SELECT " )
			.append( "	 s.sal_id, " )
			.append( "	 s.customer, " )
			.append( "	 s.contact, " )
			.append( "	 s.comments, " )
			.append( "	 s.date, " )
			.append( "	 s.value, " )
			.append( "	 s.first_payment, " )
			.append( "	 s.tax, " )
			.append( "	 s.portion, " )
			.append( "	 s.payment_type, " )
			.append( "	 s.tax, " )
			.append( "	 u.usr_id as usr_id, " )
			.append( "	 u.username as usr_name, " )
			.append( "	 per.name as usr_person " )
			.append( "FROM " + schemaName + "sale as s  ")
			.append( "INNER JOIN " + schemaName + "user as u on s.usr_id = u.usr_id ")
			.append( "INNER JOIN " + schemaName + "person as per on per.per_id = u.per_id ")
			.append( "WHERE s.sal_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model.getCustomer() != null) {
				query.append( " AND s.customer like :customer " ); 
				params.addValue("customer", this.mapLike(model.getCustomer()));
			}
			
			if(model.getPaymentType() != null && !model.getPaymentType().equals("")) {
				query.append( " AND s.payment_type = :paymentType " ); 
				params.addValue("paymentType", model.getPaymentType());
			}
			
			if(model.getDate() != null && model.getDateEnd() == null) {
				query.append( " AND s.date  = :date " ); 
				params.addValue("date", model.getDate().toString());
			}
			
			if(model.getDate() != null && model.getDateEnd() != null) {
				query.append( " AND s.date between :dateStart and :dateEnd " );
				params.addValue("dateStart", model.getDate().toString());
				params.addValue("dateEnd", model.getDateEnd().toString());
			}
			
			if(model.getUser() != null && model.getUser().getId() > 0) {
				query.append( " AND u.usr_id = :usrId " ); 
				params.addValue("usrId", model.getUser().getId());
			}
			
			query.append( " ORDER BY s.date DESC " );
			
			List<SaleModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new SaleMapper() );
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
	public Long getTotalRecords(SaleModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query
			.append( "SELECT count(*) as total " )
			.append( "FROM " + schemaName + "sale as s  ")
			.append( "INNER JOIN " + schemaName + "user as u on s.usr_id = u.usr_id ")
			.append( "INNER JOIN " + schemaName + "person as per on per.per_id = u.per_id ")
			.append( "WHERE s.sal_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model.getCustomer() != null) {
				query.append( " AND s.customer like :customer " ); 
				params.addValue("customer", this.mapLike(model.getCustomer()));
			}
			
			if(model.getPaymentType() != null && !model.getPaymentType().equals("")) {
				query.append( " AND s.payment_type = :paymentType " ); 
				params.addValue("paymentType", model.getPaymentType());
			}
			
			if(model.getDate() != null && model.getDateEnd() == null) {
				query.append( " AND s.date  = :date " ); 
				params.addValue("date", model.getDate().toString());
			}
			
			if(model.getDate() != null && model.getDateEnd() != null) {
				query.append( " AND s.date between :dateStart and :dateEnd " );
				params.addValue("dateStart", model.getDate().toString());
				params.addValue("dateEnd", model.getDateEnd().toString());
			}
			
			if(model.getUser() != null && model.getUser().getId() > 0) {
				query.append( " AND u.usr_id = :usrId " ); 
				params.addValue("usrId", model.getUser().getId());
			}
			
			query.append( " ORDER BY s.date DESC " );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ResultSetExtractor<Long>() {

				@Override
				public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
					Long result = 0L;
					if (rs != null ) {
						while( rs.next() ) {
							result = rs.getLong("total");
							break;
						}
					}
					return result;
				}
			} );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<SaleModel> getById(Integer id) throws AppException {
		Optional<SaleModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query
			.append( "SELECT " )
			.append( "	 s.sal_id, " )
			.append( "	 s.customer, " )
			.append( "	 s.contact, " )
			.append( "	 s.comments, " )
			.append( "	 s.date, " )
			.append( "	 s.value, " )
			.append( "	 s.first_payment, " )
			.append( "	 s.tax, " )
			.append( "	 s.portion, " )
			.append( "	 s.payment_type, " )
			.append( "	 s.tax, " )
			.append( "	 u.usr_id as usr_id, " )
			.append( "	 u.username as usr_name, " )
			.append( "	 per.name as usr_person " )
			.append( "FROM " + schemaName + "sale as s  ")
			.append( "INNER JOIN " + schemaName + "user as u on s.usr_id = u.usr_id ")
			.append( "INNER JOIN " + schemaName + "person as per on per.per_id = u.per_id ")
			.append( "WHERE s.sal_id = :sal_id ");	

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("sal_id", id);
			
			List<SaleModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new SaleMapper() );
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
	public List<SaleModel> list() throws AppException {
		List<SaleModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query
			.append( "SELECT " )
			.append( "	 s.sal_id, " )
			.append( "	 s.customer, " )
			.append( "	 s.contact, " )
			.append( "	 s.comments, " )
			.append( "	 s.date, " )
			.append( "	 s.value, " )
			.append( "	 s.first_payment, " )
			.append( "	 s.tax, " )
			.append( "	 s.portion, " )
			.append( "	 s.payment_type, " )
			.append( "	 s.tax, " )
			.append( "	 u.usr_id as usr_id, " )
			.append( "	 u.username as usr_name, " )
			.append( "	 per.name as usr_person " )
			.append( "FROM " + schemaName + "sale as s ")
			.append( "INNER JOIN " + schemaName + "user as u on s.usr_id = u.usr_id ")
			.append( "INNER JOIN " + schemaName + "person as per on per.per_id = u.per_id ")	
			.append( "ORDER BY s.date DESC " );
			
			List<SaleModel> list = this.getJdbcTemplatePortal().query( query.toString(), new SaleMapper() );
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
	public List<SaleModel> search(SaleModel model) throws AppException {
		List<SaleModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query
			.append( "SELECT " )
			.append( "	 s.sal_id, " )
			.append( "	 s.customer, " )
			.append( "	 s.contact, " )
			.append( "	 s.comments, " )
			.append( "	 s.date, " )
			.append( "	 s.value, " )
			.append( "	 s.first_payment, " )
			.append( "	 s.tax, " )
			.append( "	 s.portion, " )
			.append( "	 s.payment_type, " )
			.append( "	 s.tax, " )
			.append( "	 u.usr_id as usr_id, " )
			.append( "	 u.username as usr_name, " )
			.append( "	 per.name as usr_person " )
			.append( "FROM " + schemaName + "sale as s  ")
			.append( "INNER JOIN " + schemaName + "user as u on s.usr_id = u.usr_id ")
			.append( "INNER JOIN " + schemaName + "person as per on per.per_id = u.per_id ")				
			.append( "WHERE s.sal_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model.getCustomer() != null) {
				query.append( " AND s.customer like :customer " ); 
				params.addValue("customer", this.mapLike(model.getCustomer()));
			}
			
			if(model.getPaymentType() != null && !model.getPaymentType().equals("")) {
				query.append( " AND s.payment_type = :paymentType " ); 
				params.addValue("paymentType", model.getPaymentType());
			}
			
			if(model.getDate() != null && model.getDateEnd() == null) {
				query.append( " AND s.date  = :date " ); 
				params.addValue("date", model.getDate().toString());
			}
			
			if(model.getDate() != null && model.getDateEnd() != null) {
				query.append( " AND s.date between :dateStart and :dateEnd " );
				params.addValue("dateStart", PortalTimeUtils.dateToSQLDate(model.getDate(), "yyyy-MM-dd 00:00:00"));
				params.addValue("dateEnd", PortalTimeUtils.dateToSQLDate(model.getDateEnd(), "yyyy-MM-dd 23:59:59"));
			}
			
			if(model.getUser() != null && model.getUser().getId() > 0) {
				query.append( " AND u.usr_id = :usrId " ); 
				params.addValue("usrId", model.getUser().getId());
			}
			
			query.append( " ORDER BY s.date DESC " );
			
			List<SaleModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new SaleMapper() );
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
	public Optional<SaleModel> save(SaleModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query
			.append("INSERT INTO " + schemaName + "sale (  ")
	        .append("customer, ")
	        .append("contact, ")
	        .append("comments, ")
	        .append("date, ")
	        .append("value, ")
	        .append("first_payment, ")
	        .append("tax, ")
	        .append("portion, ")
	        .append("payment_type, ")
	        .append("usr_id ")
	        .append(") VALUES (")
	        .append(":customer, ")
	        .append(":contact, ")
	        .append(":comments, ")
	        .append(":date, ")
	        .append(":value, ")
	        .append(":first_payment, ")
	        .append(":tax, ")
	        .append(":portion, ")
	        .append(":payment_type, ")
	        .append(":usr_id ")
	        .append(")");
	
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue("customer", model.getCustomer());
	        params.addValue("contact", model.getContact() != null ? model.getContact() : null);
	        params.addValue("comments", model.getComments());
	        params.addValue("date", PortalTimeUtils.dateToSQLDate(model.getDate(), "yyyy-MM-dd HH:mm:ss"));
	        params.addValue("value", model.getValue());
	        params.addValue("first_payment", model.getFirstPayment());
	        params.addValue("tax", model.getTax());
	        params.addValue("portion", model.getPortion());
	        params.addValue("payment_type", model.getPaymentType());
	        params.addValue("usr_id", model.getUser().getId());
		
			KeyHolder keyHolder = new GeneratedKeyHolder();
			this.getJdbcTemplatePortal().update( query.toString(), params, keyHolder );
			model.setId( this.getKey(keyHolder).longValue() );
			
			return Optional.ofNullable(model);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<SaleModel> update(SaleModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
	        query
	        .append("UPDATE " + schemaName + "sale SET ")
	        .append("customer = :customer, ")
	        .append("contact = :contact, ")
	        .append("comments = :comments, ")
	        .append("date = :date, ")
	        .append("value = :value, ")
	        .append("first_payment = :first_payment, ")
	        .append("tax = :tax, ")
	        .append("portion = :portion, ")
	        .append("payment_type = :payment_type, ")
	        .append("usr_id = :usr_id ")
	        .append(" WHERE ")
	        .append("sal_id = :sal_id ");

	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue("customer", model.getCustomer());
	        params.addValue("contact", model.getContact());
	        params.addValue("comments", model.getComments());
	        params.addValue("date", PortalTimeUtils.dateToSQLDate(model.getDate(), "yyyy-MM-dd 12:00:00"));
	        params.addValue("value", model.getValue());
	        params.addValue("first_payment", model.getFirstPayment());
	        params.addValue("tax", model.getTax());
	        params.addValue("portion", model.getPortion());
	        params.addValue("payment_type", model.getPaymentType());
	        params.addValue("usr_id", model.getUser().getId());
	        params.addValue("sal_id", model.getId());


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
			query.append("DELETE FROM " + schemaName + "sale WHERE sal_id = :id");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

}
