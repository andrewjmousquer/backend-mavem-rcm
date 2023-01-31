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
import com.portal.dao.IUserDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.UserMapper;
import com.portal.model.UserModel;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class UserDAO extends BaseDAO implements IUserDAO {

	private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<UserModel> findLogin(UserModel model) throws AppException {
		Optional<UserModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT " );
			query.append("	 u.usr_id, " );
			query.append("	 u.username, " );
			query.append("	 u.password, " );
			query.append("	 u.enabled, " );
			query.append("	 u.per_id, " );
			query.append("	 u.acl_id, " );
			query.append("	 u.change_pass, " );
			query.append("	 u.expire_pass, " );
			query.append("	 u.pass_error_count, " );
			query.append("	 u.forgot_key, " );
			query.append("	 u.forgot_key_created, " );
			query.append("	 u.last_pass_change, " );
			query.append("	 u.blocked, " );
			query.append("	 u.type_cla, " );
			query.append("	 u.last_login, " );
			query.append("	 u.last_error_count, " );
			query.append("	 u.config, " );
			query.append("	 p.per_id, " );
			query.append("	 p.name as person, " );
			query.append("	 p.job_title, " );
			query.append("	 p.cpf, " );
			query.append("	 p.cnpj, " );
			query.append("	 p.rne, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );
			query.append("	 a.acl_id, " );
			query.append("	 a.name as accesslits, " );
			query.append("	 ut.cla_id, " );
			query.append("	 ut.value as cla_value, " );
			query.append("	 ut.type as cla_type, " );
			query.append("	 ifnull(m.mnu_id, 0) as mnu_id, ");
			query.append(" 	 m.name as menu_name, " );
			query.append("	 m.url as url, " );
			query.append("	 c.cus_id as cus_id, " );
			query.append("	 c.name as cus_name, " );
			query.append("	 c.cnpj as cus_cnpj " );
			query.append("FROM " + schemaName + "user as u " );
			query.append("INNER JOIN " + schemaName + "person as p on p.per_id = u.per_id ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON p.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN " + schemaName + "access_list as a on a.acl_id = u.acl_id ");
			query.append("LEFT JOIN " + schemaName + "menu as m on a.mnu_id = m.mnu_id ");
			query.append("LEFT JOIN " + schemaName + "customer as c ON c.cus_id = u.cus_id ");
			query.append("INNER JOIN " + schemaName + "classifier as ut on ut.cla_id = u.type_cla ");
			query.append("WHERE u.usr_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			query.append("AND username = :username " ); 
			params.addValue("username", model.getUsername());
						
			List<UserModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new UserMapper() );
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
	public Optional<UserModel> find(UserModel model) throws AppException {
		Optional<UserModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT " );
			query.append("	 u.usr_id, " );
			query.append("	 u.username, " );
			query.append("	 null as password, " );
			query.append("	 u.enabled, " );
			query.append("	 u.per_id, " );
			query.append("	 u.acl_id, " );
			query.append("	 u.change_pass, " );
			query.append("	 u.expire_pass, " );
			query.append("	 u.pass_error_count, " );
			query.append("	 u.forgot_key, " );
			query.append("	 u.forgot_key_created, " );
			query.append("	 u.last_pass_change, " );
			query.append("	 u.blocked, " );
			query.append("	 u.type_cla, " );
			query.append("	 u.last_login, " );
			query.append("	 u.last_error_count, " );
			query.append("	 u.config, " );
			query.append("	 p.per_id, " );
			query.append("	 p.name as person, " );
			query.append("	 p.job_title, " );
			query.append("	 p.cpf, " );
			query.append("	 p.cnpj, " );
			query.append("	 p.rne, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );
			query.append("	 a.acl_id, " );
			query.append("	 a.name as accesslits, " );
			query.append("	 ut.cla_id, " );
			query.append("	 ut.value as cla_value, " );
			query.append("	 ut.type as cla_type, " );
			query.append("	 c.cus_id as cus_id, " );
			query.append("	 c.name as cus_name, " );
			query.append("	 c.cnpj as cus_cnpj, " );
			query.append("   m.mnu_id as mnu_id, " );
			query.append("   m.name as menu_name, ");
			query.append("   m.url as url ");
			query.append("FROM " + schemaName + "user as u " );
			query.append("INNER JOIN " + schemaName + "person as p on p.per_id = u.per_id ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON p.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN " + schemaName + "access_list as a on a.acl_id = u.acl_id ");
			query.append("INNER JOIN " + schemaName + "classifier as ut on ut.cla_id = u.type_cla ");
			query.append("LEFT JOIN " + schemaName + "customer as c ON c.cus_id = u.cus_id ");
			query.append("LEFT JOIN " + schemaName + "user_customer uc ON u.usr_id = uc.usr_id ");
			query.append("LEFT JOIN " + schemaName + "menu as m ON a.mnu_id = m.mnu_id ");
			query.append("WHERE u.usr_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model.getUsername() != null) {
				query.append("AND username like :username " ); 
				params.addValue("username", this.mapLike(model.getUsername()));
			}
			
			if(model.getPerson() != null && model.getPerson().getId() != null) {
				query.append("AND p.per_id = :perId " ); 
				params.addValue("perId", model.getPerson().getId());
			}
			
			if(model.getPerson() != null && model.getPerson().getName() != null) {
				query.append("AND p.name = :name " ); 
				params.addValue("name", model.getPerson().getName());
			}
			
			if(model.getUserType() != null) {
				query.append("AND u.type_cla = :type " ); 
				params.addValue("type", model.getUserType().getId());
			}
			
			if(model.getCustomer() != null && model.getCustomer().getId() != null) {
				query.append("AND uc.cus_id = :cus_id " ); 
				params.addValue("cus_id", model.getCustomer().getId());
			}
			
			query.append("ORDER BY u.usr_id " );
			
			log.trace( "[QUERY] user.find: {} [PARAMS]: {}", query, params.getValues() );
			
			List<UserModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new UserMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				objReturn = Optional.ofNullable(list.get(0));
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.user.usernameExists", null, LocaleContextHolder.getLocale()));		}
		
		return objReturn;
	}

	@Override
	public Optional<UserModel> getById(Integer id) throws AppException {
		Optional<UserModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT " );
			query.append("	 u.usr_id, " );
			query.append("	 u.username, " );
			query.append("	 null as password, " );
			query.append("	 u.enabled, " );
			query.append("	 u.per_id, " );
			query.append("	 u.acl_id, " );
			query.append("	 u.change_pass, " );
			query.append("	 u.expire_pass, " );
			query.append("	 u.pass_error_count, " );
			query.append("	 u.forgot_key, " );
			query.append("	 u.forgot_key_created, " );
			query.append("	 u.last_pass_change, " );
			query.append("	 u.blocked, " );
			query.append("	 u.type_cla, " );
			query.append("	 u.last_login, " );
			query.append("	 u.last_error_count, " );
			query.append("	 u.config, " );
			query.append("	 p.per_id, " );
			query.append("	 p.name as person, " );
			query.append("	 p.job_title, " );
			query.append("	 p.cpf, " );
			query.append("	 p.cnpj, " );
			query.append("	 p.rne, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );
			query.append("	 a.acl_id, " );
			query.append("	 a.name as accesslits, " );
			query.append("	 ut.cla_id, " );
			query.append("	 ut.value as cla_value, " );
			query.append("	 ut.type as cla_type, " );
			query.append("	 ifnull(m.mnu_id, 0) as mnu_id, ");
			query.append(" 	 m.name as menu_name, " );
			query.append("	 m.url as url, " );
			query.append("	 c.cus_id as cus_id, " );
			query.append("	 c.name as cus_name, " );
			query.append("	 c.cnpj as cus_cnpj " );
			query.append("FROM " + schemaName + "user as u " );
			query.append("INNER JOIN " + schemaName + "person as p on p.per_id = u.per_id ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON p.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN " + schemaName + "access_list as a on a.acl_id = u.acl_id ");
			query.append("LEFT JOIN " + schemaName + "menu as m on a.mnu_id = m.mnu_id ");
			query.append("INNER JOIN " + schemaName + "classifier as ut on ut.cla_id = u.type_cla ");
			query.append("LEFT JOIN " + schemaName + "customer as c ON c.cus_id = u.cus_id ");
			query.append("WHERE u.usr_id = :usr_id" );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("usr_id", id); 
			
			List<UserModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new UserMapper() );
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
	public List<UserModel> list() throws AppException {
		List<UserModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT " );
			query.append("	 u.usr_id, " );
			query.append("	 u.username, " );
			query.append("	 null as password, " );
			query.append("	 u.enabled, " );
			query.append("	 u.per_id, " );
			query.append("	 u.acl_id, " );
			query.append("	 u.change_pass, " );
			query.append("	 u.expire_pass, " );
			query.append("	 u.pass_error_count, " );
			query.append("	 u.forgot_key, " );
			query.append("	 u.forgot_key_created, " );
			query.append("	 u.last_pass_change, " );
			query.append("	 u.blocked, " );
			query.append("	 u.type_cla, " );
			query.append("	 u.last_login, " );
			query.append("	 u.last_error_count, " );
			query.append("	 u.config, " );
			query.append("	 p.per_id, " );
			query.append("	 p.name as person, " );
			query.append("	 p.job_title, " );
			query.append("	 p.cpf, " );
			query.append("	 p.cnpj, " );
			query.append("	 p.rne, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );
			query.append("	 a.acl_id, " );
			query.append("	 a.name as accesslits, " );
			query.append("	 ut.cla_id, " );
			query.append("	 ut.value as cla_value, " );
			query.append("	 ut.type as cla_type, " );
			query.append("	 ifnull(m.mnu_id, 0) as mnu_id, ");
			query.append(" 	 m.name as menu_name, " );
			query.append("	 m.url as url, " );
			query.append("	 c.cus_id as cus_id, " );
			query.append("	 c.name as cus_name, " );
			query.append("	 c.cnpj as cus_cnpj " );
			query.append("FROM " + schemaName + "user as u " );
			query.append("INNER JOIN " + schemaName + "person as p on p.per_id = u.per_id ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON p.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN " + schemaName + "access_list as a on a.acl_id = u.acl_id ");
			query.append("LEFT JOIN " + schemaName + "menu as m on a.mnu_id = m.mnu_id ");
			query.append("INNER JOIN " + schemaName + "classifier as ut on ut.cla_id = u.type_cla ");
			query.append("LEFT JOIN " + schemaName + "customer as c ON c.cus_id = u.cus_id ");
			query.append("ORDER BY username" );
			
			List<UserModel> list = this.getJdbcTemplatePortal().query( query.toString(), new UserMapper() );
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
	public List<UserModel> search(UserModel model) throws AppException {
		List<UserModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT " );
			query.append("	 u.usr_id, " );
			query.append("	 u.username, " );
			query.append("	 null as password, " );
			query.append("	 u.enabled, " );
			query.append("	 u.per_id, " );
			query.append("	 u.acl_id, " );
			query.append("	 u.change_pass, " );
			query.append("	 u.expire_pass, " );
			query.append("	 u.pass_error_count, " );
			query.append("	 u.forgot_key, " );
			query.append("	 u.forgot_key_created, " );
			query.append("	 u.last_pass_change, " );
			query.append("	 u.blocked, " );
			query.append("	 u.type_cla, " );
			query.append("	 u.last_login, " );
			query.append("	 u.last_error_count, " );
			query.append("	 u.config, " );
			query.append("	 p.per_id, " );
			query.append("	 p.name as person, " );
			query.append("	 p.job_title, " );
			query.append("	 p.cpf, " );
			query.append("	 p.cnpj, " );
			query.append("	 p.rne, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );
			query.append("	 a.acl_id, " );
			query.append("	 a.name as accesslits, " );
			query.append("	 ut.cla_id, " );
			query.append("	 ut.value as cla_value, " );
			query.append("	 ut.type as cla_type, " );
			query.append("	 ifnull(m.mnu_id, 0) as mnu_id, ");
			query.append(" 	 m.name as menu_name, " );
			query.append("	 m.url as url, " );
			query.append("	 c.cus_id as cus_id, " );
			query.append("	 c.name as cus_name, " );
			query.append("	 c.cnpj as cus_cnpj " );
			query.append("FROM " + schemaName + "user as u " );
			query.append("INNER JOIN " + schemaName + "person as p on p.per_id = u.per_id ");
            query.append("INNER JOIN " + schemaName + "classifier typ ON p.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN " + schemaName + "access_list as a on a.acl_id = u.acl_id ");
			query.append("LEFT JOIN " + schemaName + "menu as m on a.mnu_id = m.mnu_id ");
			query.append("LEFT JOIN " + schemaName + "user_customer uc ON u.usr_id = uc.usr_id ");
			query.append("LEFT JOIN " + schemaName + "customer as c ON c.cus_id = u.cus_id ");
			query.append("INNER JOIN " + schemaName + "classifier as ut on ut.cla_id = u.type_cla ");
			query.append("WHERE u.usr_id > 0 " ); 
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model.getUsername() != null) {
				query.append("AND ( ");
				query.append(" u.username like :text");
				query.append(" OR p.name like :text");
				query.append(" OR a.name like :text");
				query.append(") ");
				params.addValue("text", this.mapLike(model.getUsername()));
			}
			
			
			
			query.append("ORDER BY username " );
			
			List<UserModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new UserMapper() );
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
	public Optional<UserModel> save(UserModel model) throws AppException {
		StringBuilder query = new StringBuilder("");
		
		query.append("INSERT INTO " + schemaName + "user " );
		query.append("( " );
		query.append("username, " );
		query.append("password, " );
		query.append("enabled, " );
		query.append("per_id, " );
		query.append("acl_id, " );
		query.append("change_pass, " );
		query.append("expire_pass, " );
		query.append("pass_error_count, " );
		query.append("forgot_key, " );
		query.append("forgot_key_created, " );
		query.append("last_pass_change, " );
		query.append("blocked, " );
		query.append("type_cla , " );
		query.append("cus_id ) " );
		query.append("VALUES ( " );
		query.append(":username, " );
		query.append(":password, " );
		query.append(":enabled, " );
		query.append(":perId, " );
		query.append(":aclId, " );
		query.append(":changePass, " );
		query.append(":expirePass, " );
		query.append(":passErrorCount, " );
		query.append(":forgotKey, " );
		query.append(":forgotKeyCreated, " );
		query.append(":lastPassChange, " );
		query.append(":blocked, " );
		query.append(":type_cla , " );
		query.append(":cus_id ) " );
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("username", model.getUsername());
		params.addValue("password", model.getPassword());
		params.addValue("enabled", PortalNumberUtils.booleanToInt(model.getEnabled()));
		params.addValue("perId", (model.getPerson() != null) ? model.getPerson().getId() : null);
		params.addValue("aclId", (model.getAccessList() != null) ? model.getAccessList().getId() : null);
		params.addValue("changePass", 0 );
		params.addValue("expirePass", PortalNumberUtils.booleanToInt(model.getExpirePass()));
		params.addValue("passErrorCount", (model.getPassErrorCount() != null) ? model.getPassErrorCount() : 0);
		params.addValue("forgotKey", model.getForgotKey());
		params.addValue("forgotKeyCreated", model.getForgotKeyCreated());
		params.addValue("lastPassChange", model.getLastPassChange());
		params.addValue("blocked", PortalNumberUtils.booleanToInt(model.getBlocked()));
		params.addValue("type_cla", (model.getUserType() != null) ? model.getUserType().getId() : null);
		params.addValue("cus_id", (model.getCustomer() != null) ? model.getCustomer().getId() : null);
		
		try {
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
	public Optional<UserModel> update(UserModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			query.append("UPDATE " + schemaName + "user SET " );
			query.append("username = :username,");
			
			if(model.getChangePass()) {
				query.append("password = :password,");
				params.addValue( "password", model.getPassword() );
				query.append("change_pass = 0, " );
			} else {
				query.append("change_pass = :change_pass, " );
				params.addValue( "change_pass", 0 );
			}
			
			if(model.getCustomer() != null) {
				query.append("cus_id = :cus_id, " );
				params.addValue( "cus_id", model.getCustomer().getId() );
			}
			
			query.append("type_cla = :type_cla," );
			query.append("enabled = :enabled," );
			query.append("per_id = :per_id," );
			query.append("acl_id = :acl_id," );
			
			query.append("expire_pass = :expire_pass," );
			query.append("pass_error_count = :pass_error_count," );
			query.append("forgot_key = :forgot_key," );
			query.append("forgot_key_created = :forgot_key_created," );
			query.append("last_pass_change = :last_pass_change," );
			query.append("blocked = :blocked " );
			query.append("WHERE usr_id = :usrId " );
			
			params.addValue( "username", model.getUsername() );
			params.addValue( "type_cla", model.getUserType().getId() );
			params.addValue( "enabled", PortalNumberUtils.booleanToInt( model.getEnabled() ) );
			params.addValue( "per_id", model.getPerson().getId() );
			params.addValue( "acl_id", model.getAccessList().getId() );
			
			params.addValue( "expire_pass", PortalNumberUtils.booleanToInt( model.getExpirePass() ) );
			params.addValue( "pass_error_count", model.getPassErrorCount() );
			params.addValue( "forgot_key", model.getForgotKey() );
			params.addValue( "forgot_key_created", PortalTimeUtils.dateToSQLDate( model.getForgotKeyCreated() ) );
			params.addValue( "last_pass_change", PortalTimeUtils.dateToSQLDate( model.getLastPassChange() ) );
			params.addValue( "blocked", PortalNumberUtils.booleanToInt( model.getBlocked() ) );
			params.addValue( "usrId", model.getId() );
			
			this.getJdbcTemplatePortal().update( query.toString(), params);
			return Optional.ofNullable(model);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.user.usernameExists", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<UserModel> saveUserConfig(UserModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			query.append("UPDATE " + schemaName + "user SET " );

			if(model.getCustomer() != null) {
				query.append("cus_id = :cus_id , " );
				params.addValue( "cus_id", model.getCustomer().getId() );
			}
			
			query.append("config = :user_config " );
			query.append("WHERE usr_id = :usrId " );
			
			params.addValue( "user_config", model.getConfig() );
			params.addValue( "usrId", model.getId() );
			
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
			StringBuilder query = new StringBuilder("");
			query.append("DELETE FROM " + schemaName + "user WHERE usr_id = :usrId");
			
			MapSqlParameterSource params = new MapSqlParameterSource("usrId", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	@Override
	public Optional<UserModel> changePassword(UserModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			MapSqlParameterSource params = new MapSqlParameterSource();

			query.append("UPDATE " + schemaName + "user SET " );
			query.append("change_pass = 0, " );
			query.append("expire_pass = 0, " );
			query.append("pass_error_count = 0, " );
			query.append("password = :password, ");
			query.append("last_pass_change = :last_pass_change " );
			query.append("WHERE usr_id = :usrId " );

			params.addValue( "password", model.getPassword() );
			params.addValue( "last_pass_change", PortalTimeUtils.dateToSQLDate( model.getLastPassChange() ) );
			params.addValue( "usrId", model.getId() );

			this.getJdbcTemplatePortal().update( query.toString(), params);
			return Optional.ofNullable(model);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

}
