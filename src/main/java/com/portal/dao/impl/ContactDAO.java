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
import com.portal.dao.IContactDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ContactMapper;
import com.portal.model.Contact;

@Repository
public class ContactDAO extends BaseDAO implements IContactDAO {

	private static final Logger logger = LoggerFactory.getLogger(ContactDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<Contact> find(Contact model) throws AppException {
		Optional<Contact> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" 		c.cot_id, ");
			query.append(" 		c.value, ");
			query.append(" 		c.complement, ");
			query.append(" 		c.per_id, ");
			query.append(" 		ct.cla_id, ");
			query.append(" 		ct.value as cla_value, ");
			query.append(" 		ct.label as cla_label, ");
			query.append(" 		ct.type as cla_type ");
			query.append("FROM contact as c ");
			query.append("INNER JOIN  classifier as ct on c.type_cla = ct.cla_id ");
			query.append("WHERE (:cotId is not null and c.cotId = :cotId) ");
			query.append("OR (:value is not null and c.value like :value) " );
			query.append("OR (:complement is not null and c.complement like :complement) " );
			query.append("OR (:type is not null and c.type_cla like :type) " );
			query.append("OR (:perId is not null and c.per_id like :perId) " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("cotId", model.getId());
			params.addValue("value", model.getValue());
			params.addValue("complement", model.getComplement());
			params.addValue("type", model.getType().getId());
			params.addValue("perId", (model.getPerson() != null ? model.getPerson().getId() : null ));
			
			List<Contact> list = this.getJdbcTemplatePortal().query( query.toString(), params, new ContactMapper() );
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
	public Optional<Contact> getById(Integer id) throws AppException {
		Optional<Contact> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" 		c.cot_id, ");
			query.append(" 		c.value, ");
			query.append(" 		c.complement, ");
			query.append(" 		c.per_id, ");
			query.append(" 		ct.cla_id, ");
			query.append(" 		ct.value as cla_value, ");
			query.append(" 		ct.label as cla_label, ");
			query.append(" 		ct.type as cla_type ");
			query.append("FROM contact as c ");
			query.append("INNER JOIN classifier as ct on c.type_cla = ct.cla_id ");
			query.append("WHERE cot_id = :cotId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("cotId", id);
			
			List<Contact> list = this.getJdbcTemplatePortal().query( query.toString(), params, new ContactMapper() );
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
	public List<Contact> list() throws AppException {
		List<Contact> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" 		c.cot_id, ");
			query.append(" 		c.value, ");
			query.append(" 		c.complement, ");
			query.append(" 		c.per_id, ");
			query.append(" 		ct.cla_id, ");
			query.append(" 		ct.value as cla_value, ");
			query.append(" 		ct.label as cla_label, ");
			query.append(" 		ct.type as cla_type ");
			query.append("FROM contact as c ");
			query.append("INNER JOIN classifier as ct on c.type_cla = ct.cla_id ");

			List<Contact> list = this.getJdbcTemplatePortal().query( query.toString(), new ContactMapper() );
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
	public List<Contact> search(Contact model) throws AppException {
		List<Contact> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" 		c.cot_id, ");
			query.append(" 		c.value, ");
			query.append(" 		c.complement, ");
			query.append(" 		c.per_id, ");
			query.append(" 		ct.cla_id, ");
			query.append(" 		ct.value as cla_value, ");
			query.append(" 		ct.type as cla_type ");
			query.append("FROM contact as c ");
			query.append("INNER JOIN classifier as ct on c.type_cla = ct.cla_id ");
			query.append("WHERE (:cotId is not null and c.cotId = :cotId) ");
			query.append("OR (:value is not null and c.value like :value) " );
			query.append("OR (:complement is not null and c.complement like :complement) " );
			query.append("OR (:type is not null and c.type_cla like :type) " );
			query.append("OR (:perId is not null and c.per_id like :perId) " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("cotId", model.getId());
			params.addValue("value", model.getValue());
			params.addValue("complement", model.getComplement());
			params.addValue("type", model.getType().getId());
			params.addValue("perId", (model.getPerson() != null ? model.getPerson().getId() : null ));
			
			List<Contact> users = this.getJdbcTemplatePortal().query( query.toString(), params, new ContactMapper() );
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
	public Optional<Contact> save(Contact model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query.append("INSERT INTO contact (  ");
			query.append("	value, ");
			query.append("	complement, ");
			query.append("	type_cla, ");
			query.append("	per_id ");
			query.append(") VALUES (");
			query.append("	:value, ");
			query.append("	:complement, ");
			query.append("	:type_cla, ");
			query.append("	:perId ");
			query.append(")");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("value", model.getValue());
			params.addValue("complement", model.getComplement());
			params.addValue("type_cla", model.getType().getId());
			params.addValue("perId", (model.getPerson() != null ? model.getPerson().getId() : null ));
		
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
	public Optional<Contact> update(Contact model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE contact SET ");
			query.append("	value = :value, ");
			query.append("	complement = :complement, ");
			query.append("	type_cla = :type_cla, ");
			query.append("	per_id = :perId ");
			query.append("WHERE ");
			query.append("	cot_id = :cot_id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("value", model.getValue());
			params.addValue("complement", model.getComplement());
			params.addValue("type_cla", model.getType().getId());
			params.addValue("cot_id", model.getId());
			params.addValue("perId", (model.getPerson() != null ? model.getPerson().getId() : null ));
			
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
			query.append("DELETE FROM contact WHERE cot_id = :id");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public List<Contact> findByPerson(Integer id) throws AppException {
		List<Contact> list = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" 	c.cot_id, ");
			query.append(" 	c.value, ");
			query.append(" 	c.complement, ");
			query.append(" 		c.per_id, ");
			query.append(" 	ct.cla_id, ");
			query.append(" 	ct.value as cla_value, ");
			query.append(" 	ct.label as cla_label, ");
			query.append(" 	ct.type as cla_type ");
			query.append("FROM contact as c ");
			query.append("INNER JOIN classifier as ct on c.type_cla = ct.cla_id ");
            query.append("WHERE c.per_id = :id "); 
            query.append("ORDER BY c.value "); 

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", id);

			logger.trace("[QUERY] contact.findByPerson: {} [PARAMS]: {}", query, params.getValues() );

			list = this.getJdbcTemplatePortal().query(query.toString(), params, new ContactMapper());
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return list;
	}
}
