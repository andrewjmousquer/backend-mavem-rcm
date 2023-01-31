package com.portal.dao.impl;

import java.util.LinkedList;
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
import com.portal.dao.ICustomerDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.CustomerMapper;
import com.portal.model.CustomerModel;
import com.portal.utils.PortalNumberUtils;

@Repository
public class CustomerDAO extends BaseDAO implements ICustomerDAO {

	private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<CustomerModel> find(CustomerModel model) throws AppException {
		Optional<CustomerModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT c.* ");
			query.append("	cla.cla_id cla_id, ");
			query.append("	cla.type cla_type, ");
			query.append("	cla.value cla_value ");
			query.append("FROM " + schemaName + "customer AS c ");
			query.append("LEFT JOIN " + schemaName + "classifier AS cla ON c.type_cla = cla.cla_id ");
			query.append("WHERE c.cus_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND c.cus_id = :cusId ");
					params.addValue( "cusId", model.getId() );
				}
				
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND c.name = :name ");
					params.addValue( "name", model.getName() );
				}
				
				if(model.getCnpj() != null && !model.getCnpj().equals("")) {
					query.append("AND c.cnpj = :cnpj ");
					params.addValue( "cnpj", PortalNumberUtils.normalizeCnpj(model.getCnpj()) );
				}
				
				if(model.getHolding() != null && model.getHolding().getId() != null && model.getHolding().getId() > 0) {
					query.append(" AND c.hol_id = :holId ");
					params.addValue( "holId", model.getHolding().getId() );
				}
			}

			query.append("ORDER BY c.name ");
			
			List<CustomerModel> listSearch = this.getJdbcTemplatePortal().query( query.toString(), params, new CustomerMapper() );
			if(!CollectionUtils.isEmpty(listSearch)) {
				objReturn = Optional.ofNullable(listSearch.get(0));
			}
			
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return objReturn;
	}

	@Override
	public Optional<CustomerModel> getById(Integer id) throws AppException {
		Optional<CustomerModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT c.*, ");
			query.append("	cla.cla_id cla_id, ");
			query.append("	cla.type cla_type, ");
			query.append("	cla.value cla_value ");
			query.append(" FROM " + schemaName + "customer c ");
			query.append(" LEFT JOIN " + schemaName + "classifier AS cla ON c.type_cla = cla.cla_id ");
			query.append(" WHERE c.cus_id =  :cusId  ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "cusId", id);
			
			List<CustomerModel> listSearch = this.getJdbcTemplatePortal().query( query.toString(), params, new CustomerMapper() );
			if(!CollectionUtils.isEmpty(listSearch)) {
				objReturn = Optional.ofNullable(listSearch.get(0));
			}
			
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return objReturn;
	}

	@Override
	public List<CustomerModel> list() throws AppException {
		List<CustomerModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT c.*, " );
			query.append("	cla.cla_id cla_id, ");
			query.append("	cla.type cla_type, ");
			query.append("	cla.value cla_value ");
			query.append( "FROM " + schemaName + "customer c " );
			query.append( "LEFT JOIN " + schemaName + "classifier AS cla ON c.type_cla = cla.cla_id ");
			query.append( "ORDER BY name " );
			
			List<CustomerModel> listSearch = this.getJdbcTemplatePortal().query( query.toString(), new CustomerMapper() );
			if(!CollectionUtils.isEmpty(listSearch)) {
				listReturn = listSearch;
			}
			
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}

	@Override
	public List<CustomerModel> search(CustomerModel model) throws AppException {
		List<CustomerModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT c.*, ");
			query.append("	cla.cla_id cla_id, ");
			query.append("	cla.type cla_type, ");
			query.append("	cla.value cla_value ");
			query.append("FROM " + schemaName + "customer AS c ");
			query.append("LEFT JOIN " + schemaName + "classifier AS cla ON c.type_cla = cla.cla_id ");
			query.append("WHERE c.cus_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND c.cus_id = :cusId ");
					params.addValue( "cusId", model.getId() );
				}
				
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND c.name like :name ");
					params.addValue( "name", this.mapLike(model.getName()) );
				}
				
				if(model.getCnpj() != null && !model.getCnpj().equals("")) {
					query.append("AND c.cnpj like :cnpj ");
					params.addValue( "cnpj", this.mapLike(PortalNumberUtils.normalizeCnpj(model.getCnpj())) );
				}
				
				if(model.getHolding() != null && model.getHolding().getId() != null && model.getHolding().getId() > 0) {
					query.append(" AND c.hol_id = :holId ");
					params.addValue( "holId", model.getHolding().getId() );
				}
			}

			query.append("ORDER BY c.name ");
			
			List<CustomerModel> listSearch = this.getJdbcTemplatePortal().query( query.toString(), params, new CustomerMapper() );
			if(!CollectionUtils.isEmpty(listSearch)) {
				listReturn = listSearch;
			}
			
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}

	@Override
	public Optional<CustomerModel> save(CustomerModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			
			query.append( "INSERT INTO  " + schemaName + "customer " );
			query.append( "( " );
			query.append( "name, " );
			query.append( "cnpj, " );
			query.append( "hol_id, " );
			query.append( "type_cla " );
			query.append( ") VALUES ( " );
			query.append( ":name, " );
			query.append( ":cnpj, " );
			query.append( ":holId, " );
			query.append( ":typeCla " );
			query.append( ") " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("cnpj", PortalNumberUtils.normalizeCnpj(model.getCnpj()));
			params.addValue("holId", (model.getHolding() != null) ? model.getHolding().getId() : null);
 			params.addValue("typeCla", model.getType().getId());
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
			this.getJdbcTemplatePortal().update( query.toString(), params, keyHolder );
			model.setId( this.getKey(keyHolder) );
			
			return Optional.ofNullable(model);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<CustomerModel> update(CustomerModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append( "UPDATE " + schemaName + "customer " );
			query.append( "SET " );
			query.append( "name = :name, " );
			query.append( "cnpj = :cnpj, " );
			query.append( "hol_id = :holId, " );
			query.append( "type_cla = :typeCla " );
			query.append( "WHERE cus_id = :cusId " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("cnpj", PortalNumberUtils.normalizeCnpj(model.getCnpj()));
			params.addValue("holId", (model.getHolding() != null) ? model.getHolding().getId() : null);
 			params.addValue("typeCla", model.getType().getId());
			params.addValue("cusId", model.getId());
			
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
			StringBuilder query = new StringBuilder("");
			query.append("DELETE FROM " + schemaName + "customer WHERE cus_id = :cusId");
			
			MapSqlParameterSource params = new MapSqlParameterSource("cusId", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	public List<CustomerModel> listByUserId( Integer usrId ) throws AppException {
		List<CustomerModel> listReturn = null;
		
		StringBuilder query = new StringBuilder("");
		
		query.append("SELECT cus.cus_id, "); 
		query.append(" 		 cus.name, ");
		query.append(" 		 cus.cnpj, ");
		query.append(" 		 cus.hol_id, ");
		query.append("		 cla.cla_id cla_id, ");
		query.append("		 cla.type cla_type, ");
		query.append("		 cla.value cla_value ");
		query.append("FROM " + schemaName + "customer AS cus ");
		query.append("LEFT JOIN " + schemaName + "user_customer AS uc ON uc.cus_id = cus.cus_id ");
 		query.append("LEFT JOIN " + schemaName + "classifier AS cla ON cus.type_cla = cla.cla_id ");
		query.append("WHERE uc.usr_id = :usrId ");
		query.append("ORDER BY cus.name ");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "usrId", usrId );
		
		try {
			List<CustomerModel> listSearch = this.getJdbcTemplatePortal().query( query.toString(), new CustomerMapper() );
			if(!CollectionUtils.isEmpty(listSearch)) {
				listReturn = listSearch;
			}
		} catch(Exception e) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}

	public List<CustomerModel> listByHoldingId(Integer holId) throws AppException {
		List<CustomerModel> customers = new LinkedList<CustomerModel>();
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT cus.cus_id, ");
		query.append(" 		 cus.name, ");
		query.append(" 		 cus.cnpj, ");
		query.append(" 		 cus.hol_id, ");
		query.append(" 		 cus.type_cla, ");
		query.append("		 cla.cla_id cla_id, ");
		query.append("		 cla.type cla_type, ");
		query.append("		 cla.value cla_value ");
		query.append("FROM " + schemaName + "customer AS cus ");
 		query.append("LEFT JOIN " + schemaName + "classifier AS cla ON cus.type_cla = cla.cla_id ");
		query.append("WHERE hol_id = :holId ");
		query.append("ORDER BY cus.name ");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "holId", holId );
		
		try {
			customers = this.getJdbcTemplatePortal().query( query.toString(), params, new CustomerMapper() );
		} catch(Exception e) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return customers;
	}
	
	public List<CustomerModel> listByUserHolding(Integer usrId, Integer holId) throws AppException {
		List<CustomerModel> customers = new LinkedList<CustomerModel>();
		
		StringBuilder query = new StringBuilder("");
		query.append("SELECT cus.cus_id, ");
		query.append(" 		 cus.name, ");
		query.append(" 		 cus.cnpj, ");
		query.append(" 		 cus.hol_id, ");
		query.append(" 		 cus.type_cla, ");
		query.append("		 cla.cla_id cla_id, ");
		query.append("		 cla.type cla_type, ");
		query.append("		 cla.value cla_value ");
		query.append("FROM " + schemaName + "customer AS cus ");
		query.append("INNER JOIN " + schemaName + "user_customer AS uc ON uc.cus_id = cus.cus_id ");
		query.append("INNER JOIN " + schemaName + "holding AS hol ON hol.hol_id = cus.hol_id ");
 		query.append("LEFT JOIN " + schemaName + "classifier AS cla ON cus.type_cla = cla.cla_id ");
		query.append("WHERE uc.usr_id = :usrId ");
		query.append("AND hol.hol_id = :holId ");
		query.append("ORDER BY cus.name ");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "usrId", usrId );
		params.addValue( "holId", holId );
		
		try {
			customers = this.getJdbcTemplatePortal().query( query.toString(), params, new CustomerMapper() );
		} catch(Exception e) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return customers;
	}

	public Optional<CustomerModel> findByCNPJ(String cnpj) throws AppException {
		Optional<CustomerModel> customer = Optional.empty();
		
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT c.*, " );
			query.append("	cla.cla_id cla_id, ");
			query.append("	cla.type cla_type, ");
			query.append("	cla.value cla_value ");
			query.append( "FROM " + schemaName + "customer AS c " );
			query.append( "LEFT JOIN " + schemaName + "classifier AS cla ON c.type_cla = cla.cla_id ");
			query.append( "WHERE c.cnpj = :cnpj " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "cnpj", cnpj );
			
			List<CustomerModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new CustomerMapper() );
			if(list != null && !list.isEmpty()) {
				customer = Optional.ofNullable(list.get( 0 ));
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}

		return customer;
	}
	
	@Override
	public boolean verifyCustomerConstraint(Integer cusId) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT count(*) ");
			query.append("FROM customer AS cus ");
			query.append("INNER JOIN user_customer AS uc ON uc.cus_id = cus.cus_id ");
			query.append("LEFT JOIN " + schemaName + "classifier AS cla ON cus.type_cla = cla.cla_id ");
			query.append("WHERE cus.cus_id = " + cusId);
			
			Integer quantityKeys = this.getJdbcTemplatePortal().getJdbcOperations().queryForObject(query.toString(), Integer.class);
			if(quantityKeys > 1) {
				return true;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}

		return false;
	}
	
}
