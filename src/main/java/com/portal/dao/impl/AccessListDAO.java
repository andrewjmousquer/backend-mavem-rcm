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
import com.portal.dao.IAccessListDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.AccesslistMapper;
import com.portal.model.AccessListModel;

@Repository
public class AccessListDAO extends BaseDAO implements IAccessListDAO {

	private static final Logger logger = LoggerFactory.getLogger(AccessListDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<AccessListModel> find(AccessListModel model) throws AppException {
		Optional<AccessListModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" a.acl_id, ");
			query.append(" a.name, ");
			query.append(" ifnull(m.mnu_id, 0) as mnu_id, ");
			query.append(" m.name as menu_name, " );
			query.append(" m.url as url " );
			query.append("FROM " + schemaName + "access_list as a ");
			query.append("LEFT JOIN " + schemaName + "menu as m on a.mnu_id = m.mnu_id ");
			query.append("WHERE a.acl_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND a.acl_id = :aclid ");
					params.addValue("aclid", model.getId());
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name = :name " );
					params.addValue("name", model.getName());
				}
			}
						
			query.append( "ORDER BY a.name " );
			
			List<AccessListModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new AccesslistMapper() );
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
	public Optional<AccessListModel> getById(Integer id) throws AppException {
		Optional<AccessListModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" a.acl_id, ");
			query.append(" a.name, ");
			query.append(" ifnull(m.mnu_id, 0) as mnu_id, ");
			query.append(" m.name as menu_name, " );
			query.append(" m.url as url " );
			query.append("FROM " + schemaName + "access_list as a ");
			query.append("LEFT JOIN " + schemaName + "menu as m on a.mnu_id = m.mnu_id ");
			query.append("WHERE acl_id = :aclid ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("aclid",id);
			
			List<AccessListModel> returnList = this.getJdbcTemplatePortal().query( query.toString(), params, new AccesslistMapper() );
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
	public List<AccessListModel> list() throws AppException {
		List<AccessListModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" a.acl_id, ");
			query.append(" a.name, ");
			query.append(" ifnull(m.mnu_id, 0) as mnu_id, ");
			query.append(" m.name as menu_name, " );
			query.append(" m.url as url " );
			query.append("FROM " + schemaName + "access_list as a ");
			query.append("LEFT JOIN " + schemaName + "menu as m on a.mnu_id = m.mnu_id ");
			query.append("ORDER BY a.name " );
			
			List<AccessListModel> list = this.getJdbcTemplatePortal().query( query.toString(), new AccesslistMapper() );
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
	public List<AccessListModel> search(AccessListModel model) throws AppException {
		List<AccessListModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" a.acl_id, ");
			query.append(" a.name, ");
			query.append(" ifnull(m.mnu_id, 0) as mnu_id, ");
			query.append(" m.name as menu_name, " );
			query.append(" m.url as url " );
			query.append("FROM " + schemaName + "access_list as a ");
			query.append("LEFT JOIN " + schemaName + "menu as m on a.mnu_id = m.mnu_id ");
			query.append("WHERE a.acl_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND a.acl_id = :aclid ");
					params.addValue("aclid", model.getId());
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
				}
			}
						
			query.append( "ORDER BY a.name " );
			
			List<AccessListModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new AccesslistMapper() );
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
	public Optional<AccessListModel> save(AccessListModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query.append("INSERT INTO " + schemaName + "access_list (  ");
			query.append("name, ");
			query.append("mnu_id ");
			query.append(") VALUES (");
			query.append(":name, ");
			query.append(":menu ");
			query.append(")");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("menu", (model.getDefaultMenu() != null ? model.getDefaultMenu().getId() : null));
			
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
	public Optional<AccessListModel> update(AccessListModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE  " + schemaName + "access_list SET ");
			query.append("name = :name, ");
			query.append("mnu_id = :menu ");
			query.append(" WHERE ");
			query.append("acl_id = :aclId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("aclId", model.getId());
			params.addValue("menu", (model.getDefaultMenu() != null ? model.getDefaultMenu().getId() : null));

			
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
			query.append("DELETE FROM " + schemaName + "access_list WHERE acl_id = :id");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public boolean hasDuplicatedName(AccessListModel accesslist) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT COUNT(acl_id) ");
			query.append("FROM " + schemaName + "access_list ");
			query.append("WHERE name = :name ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", accesslist.getName());
			
			if(accesslist.getId() != null && accesslist.getId() != 0) {
				query.append("AND acl_id <> :aclId ");
				params.addValue("aclId", accesslist.getId());
			}
			
			Integer count = this.getJdbcTemplatePortal().queryForObject(query.toString(), params, Integer.class);
			return (count > 0);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public boolean hasUserAccessList(Integer aclId) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT COUNT(u.usr_id) FROM " + schemaName + "user as u WHERE u.acl_id = :aclId");
			
			MapSqlParameterSource params = new MapSqlParameterSource("aclId", aclId);
			Integer count = this.getJdbcTemplatePortal().queryForObject(query.toString(), params, Integer.class);
			return (count > 0);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
}
