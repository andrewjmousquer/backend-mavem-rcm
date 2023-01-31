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
import com.portal.dao.IMenuDAO;
import com.portal.enums.MenuType;
import com.portal.exceptions.AppException;
import com.portal.mapper.MenuMapper;
import com.portal.model.MenuModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class MenuDAO extends BaseDAO implements IMenuDAO {

	private static final Logger logger = LoggerFactory.getLogger(MenuDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<MenuModel> find(MenuModel model) throws AppException {
		Optional<MenuModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append(" 	m.mnu_id, ");
			query.append(" 	m.name, ");
			query.append(" 	fnMenu(m.mnu_id) as path, ");
			query.append(" 	m.url, ");
			query.append(" 	m.description, ");
			query.append(" 	m.icon, ");
			query.append(" 	m.root_id, ");
			query.append(" 	m.show, ");
			query.append(" 	mt.cla_id, ");
			query.append(" 	mt.value as cla_value, ");
			query.append(" 	mt.type as cla_type ");
			query.append(" FROM " + schemaName + "menu as m ");
			query.append(" INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
			query.append(" WHERE m.mnu_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND m.mnu_id = :mnuId ");
					params.addValue("mnuId", model.getId());
				}
				
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND m.name = :name " );
					params.addValue("name", model.getName());
				}
				
				if(model.getRoute() != null && !model.getRoute().equals("")) {
					query.append(" AND m.url = :url " );
					params.addValue("url", model.getRoute());
				}
				
				if(model.getDescription() != null && !model.getDescription().equals("")) {
					query.append(" AND m.description = :description " );
					params.addValue("description", model.getDescription());
				}
				
				if(model.getIcon() != null && !model.getIcon().equals("")) {
					query.append(" AND m.icon = :icon " );
					params.addValue("icon", model.getIcon());
				}
				
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND m.root_id = :rootId " );
					params.addValue("rootId", model.getRoot().getId());
				}
				
				if(model.getType() != null && model.getType().getId() > 0) {
					query.append(" AND m.type_cla = :type " );
					params.addValue("type", model.getType().getId());
				}
			}
			
			log.trace( "[QUERY] menu.find: {} [PARAMS]: {}", query, params.getValues() );

			List<MenuModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
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
	public Optional<MenuModel> getById(Integer id) throws AppException {
		Optional<MenuModel> objReturn = Optional.empty();
		
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append(" 	m.mnu_id, ");
			query.append(" 	m.name, ");
			query.append(" 	fnMenu(m.mnu_id) as path, ");
			query.append(" 	m.url, ");
			query.append(" 	m.description, ");
			query.append(" 	m.icon, ");
			query.append(" 	m.root_id, ");
			query.append(" 	m.show, ");
			query.append(" 	mt.cla_id, ");
			query.append(" 	mt.value as cla_value, ");
			query.append(" 	mt.type as cla_type ");
			query.append(" FROM " + schemaName + "menu as m ");
			query.append(" INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
			query.append(" WHERE m.mnu_id = :mnuId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("mnuId", id);
			
			List<MenuModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
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
	public List<MenuModel> list() throws AppException {
		List<MenuModel> listReturn = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append(" 	m.mnu_id, ");
			query.append(" 	m.name, ");
			query.append(" 	fnMenu(m.mnu_id) as path, ");
			query.append(" 	m.url, ");
			query.append(" 	m.description, ");
			query.append(" 	m.icon, ");
			query.append(" 	m.root_id, ");
			query.append(" 	m.show, ");
			query.append(" 	mt.cla_id, ");
			query.append(" 	mt.value as cla_value, ");
			query.append(" 	mt.type as cla_type ");
			query.append(" FROM " + schemaName + "menu as m ");
			query.append(" INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
			query.append(" WHERE m.mnu_id > 0 ");
			query.append(" ORDER BY m.name ");
			
			List<MenuModel> users = this.getJdbcTemplatePortal().query( query.toString(), new MenuMapper() );
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
	public List<MenuModel> search(MenuModel model) throws AppException {
		List<MenuModel> listReturn = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append(" 	m.mnu_id, ");
			query.append(" 	m.name, ");
			query.append(" 	fnMenu(m.mnu_id) as path, ");
			query.append(" 	m.url, ");
			query.append(" 	m.description, ");
			query.append(" 	m.icon, ");
			query.append(" 	m.root_id, ");
			query.append(" 	m.show, ");
			query.append(" 	mt.cla_id, ");
			query.append(" 	mt.value as cla_value, ");
			query.append(" 	mt.type as cla_type ");
			query.append(" FROM " + schemaName + "menu as m ");
			query.append(" INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
			query.append(" WHERE m.mnu_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND m.mnu_id = :mnuId ");
					params.addValue("mnuId", model.getId());
				}
				
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND m.name = :name " );
					params.addValue("name", model.getName());
				}
				
				if(model.getRoute() != null && !model.getRoute().equals("")) {
					query.append(" AND m.url = :url " );
					params.addValue("url", model.getRoute());
				}
				
				if(model.getDescription() != null && !model.getDescription().equals("")) {
					query.append(" AND m.description = :description " );
					params.addValue("description", model.getDescription());
				}
				
				if(model.getIcon() != null && !model.getIcon().equals("")) {
					query.append(" AND m.icon = :icon " );
					params.addValue("icon", model.getIcon());
				}
				
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND m.root_id = :rootId " );
					params.addValue("rootId", model.getRoot().getId());
				}
				
				if(model.getType() != null && model.getType().getId() > 0) {
					query.append(" AND m.type_cla = :type " );
					params.addValue("type", model.getType().getId());
				}
			}
			
			List<MenuModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
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
	public Optional<MenuModel> save(MenuModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query.append("INSERT INTO " + schemaName + "menu (  ");
			query.append("name, ");
			query.append("url, ");
			query.append("description, ");
			query.append("icon, ");
			query.append("type_cla, ");
			query.append("root_id, ");
			query.append("`show` ");
			query.append(") VALUES (");
			query.append(":name, ");
			query.append(":url, ");
			query.append(":description, ");
			query.append(":icon, ");
			query.append(":type_cla, ");
			query.append(":rootId, ");
			query.append(":show ");
			query.append(")");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("url", model.getRoute());
			params.addValue("description", model.getDescription());
			params.addValue("icon", model.getIcon() != null ? model.getIcon().toLowerCase() : null);
			params.addValue("type_cla", model.getType().getId());
			params.addValue("rootId", (model.getRoot() != null ? model.getRoot().getId() : null));
			params.addValue("show", model.isShow() ? 1 :  0);	
			
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
	public Optional<MenuModel> update(MenuModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE " + schemaName + "menu SET ");
			query.append("name = :name, ");
			query.append("url = :url, ");
			query.append("description = :description, ");
			query.append("icon = :icon, ");
			query.append("type_cla = :type_cla , ");
			query.append("root_id = :rootId, ");
			query.append("`show` = :show ");
			query.append(" WHERE ");
			query.append("mnu_id = :mnuId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("url", model.getRoute());
			params.addValue("description", model.getDescription());
			params.addValue("icon",  model.getIcon() != null ? model.getIcon().toLowerCase() : null);
			params.addValue("type_cla", model.getType().getId());
			params.addValue("rootId", (model.getRoot() != null ? model.getRoot().getId() : null));
			params.addValue("mnuId", model.getId());
			params.addValue("show", model.isShow() ? 1 :  0);	

			log.trace( "[QUERY] menu.update: {} [PARAMS]: {}", query, params.getValues() );
			
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
			query.append("DELETE FROM " + schemaName + "menu WHERE mnu_id = :id");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Lista os menus por usuário
	 * @param usrId
	 * @return Lista de menus
	 * @throws AppException
	 */
	@Override
	public List<MenuModel> listByUser( Integer usrId ) throws AppException {
		List<MenuModel> list = null;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(" 	m.mnu_id, ");
		query.append(" 	m.name, ");
		query.append(" 	fnMenu(m.mnu_id) as path, ");
		query.append(" 	m.url, ");
		query.append(" 	m.description, ");
		query.append(" 	m.icon, ");
		query.append(" 	m.root_id, ");
		query.append("  m.show, ");
		query.append(" 	mt.cla_id, ");
		query.append(" 	mt.value as cla_value, ");
		query.append(" 	mt.type as cla_type ");
		query.append("FROM " + schemaName + "menu as m ");
		query.append("INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
		query.append("LEFT JOIN " + schemaName + "access_list_menu as alm ON ( m.mnu_id = alm.mnu_id ) ");
		query.append("LEFT JOIN " + schemaName + "user as u ON ( u.acl_id = alm.acl_id ) ");
		query.append("WHERE u.usr_id = :usrId ");
		query.append("ORDER BY alm.mnu_order ASC ");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "usrId", usrId );
		
		try {
			list = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return list;
	}
	
	/**
	 * Lista os menus por usuário
	 * @param usrId
	 * @return Lista de menus
	 * @throws AppException
	 */
	@Override
	public List<MenuModel> list( Integer usrId ) throws AppException {
		List<MenuModel> list = null;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(" 	m.mnu_id, ");
		query.append(" 	m.name, ");
		query.append(" 	fnMenu(m.mnu_id) as path, ");
		query.append(" 	m.url, ");
		query.append(" 	m.description, ");
		query.append(" 	m.icon, ");
		query.append(" 	m.root_id, ");
		query.append("  m.show, ");
		query.append(" 	mt.cla_id, ");
		query.append(" 	mt.value as cla_value, ");
		query.append(" 	mt.type as cla_type ");
		query.append("FROM " + schemaName + "menu as m ");
		query.append("INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
		query.append("LEFT JOIN " + schemaName + "access_list_menu as alm ON ( m.mnu_id = alm.mnu_id ) ");
		query.append("LEFT JOIN " + schemaName + "user as u ON ( u.acl_id = alm.acl_id ) ");
		query.append("WHERE u.usr_id = :usrId ");
		query.append("ORDER BY alm.mnu_order ASC ");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "usrId", usrId );
		try {
			list = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return list;
	}
	
	/**
	 * Lista os menus por tipo
	 * @param type
	 * @return Lista de menus
	 * @throws AppException
	 */
	@Override
	public List<MenuModel> list( MenuType type ) throws AppException {
		List<MenuModel> menus = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append(" 	m.mnu_id, ");
			query.append(" 	m.name, ");
			query.append(" 	fnMenu(m.mnu_id) as path, ");
			query.append(" 	m.url, ");
			query.append(" 	m.description, ");
			query.append(" 	m.icon, ");
			query.append(" 	m.root_id, ");
			query.append("  m.show, ");
			query.append(" 	mt.cla_id, ");
			query.append(" 	mt.value as cla_value, ");
			query.append(" 	mt.type as cla_type ");
			query.append( "FROM " + schemaName + "menu m " );	
			query.append(" INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
			query.append( "WHERE m.mnu_id = :type " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("type", type.getType().getId());
			
			menus = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}

		return menus;
	}
	
	/**
	 * Lista os menus por tipo e usuário
	 * @param type
	 * @param usrId
	 * @return Lista de menus
	 * @throws AppException
	 */
	@Override
	public List<MenuModel> list( MenuType type, Integer usrId ) throws AppException {
		List<MenuModel> list = null;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(" 	m.mnu_id, ");
		query.append(" 	m.name, ");
		query.append(" 	fnMenu(m.mnu_id) as path, ");
		query.append(" 	m.url, ");
		query.append(" 	m.description, ");
		query.append(" 	m.icon, ");
		query.append(" 	m.root_id, ");
		query.append("  m.show, ");
		query.append(" 	mt.cla_id, ");
		query.append(" 	mt.value as cla_value, ");
		query.append(" 	mt.type as cla_type ");
		query.append("FROM " + schemaName + "menu as m ");
		query.append("INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
		query.append("INNER JOIN " + schemaName + "access_list_menu as alm ON ( m.mnu_id = alm.mnu_id ) ");
		query.append("INNER JOIN " + schemaName + "user as u ON ( u.acl_id = alm.acl_id ) ");
		query.append("WHERE m.type_cla = :type ");
		query.append("AND u.usr_id = :usrId ");
		query.append("ORDER BY alm.mnu_order, m.mnu_id ASC ");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "usrId", usrId );
		params.addValue( "type", type.getType().getId());
		
		try {
			list = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return list;
	}
	
	
	/**
	 * Lista os itens raiz do menu
	 * @return Lista de menus
	 * @throws AppException
	 */
	@Override
	public List<MenuModel> listRoots() throws AppException {
		List<MenuModel> list = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" 	m.mnu_id, ");
			query.append(" 	m.name, ");
			query.append(" 	fnMenu(m.mnu_id) as path, ");
			query.append(" 	m.url, ");
			query.append(" 	m.description, ");
			query.append(" 	m.icon, ");
			query.append(" 	m.root_id, ");
			query.append("  m.show, ");
			query.append(" 	mt.cla_id, ");
			query.append(" 	mt.value as cla_value, ");
			query.append(" 	mt.type as cla_type ");
			query.append("FROM " + schemaName + "menu as m ");
			query.append("INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
			query.append("WHERE m.root_id IS NULL ");
			
			list = this.getJdbcTemplate().query( query.toString(), new MenuMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return list;
	}
	
	/**
	 * Lista os itens de raiz do menu principal (sem os menus de configurações)
	 * @param usrId
	 * @return Lista de menus
	 * @throws AppException
	 */
	@Override
	public List<MenuModel> listRoots(Integer usrId) throws AppException {
		List<MenuModel> list = null;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(" 	m.mnu_id, ");
		query.append(" 	m.name, ");
		query.append(" 	fnMenu(m.mnu_id) as path, ");
		query.append(" 	m.url, ");
		query.append(" 	m.description, ");
		query.append(" 	m.icon, ");
		query.append(" 	m.root_id, ");
		query.append("  m.show, ");
		query.append(" 	mt.cla_id, ");
		query.append(" 	mt.value as cla_value, ");
		query.append(" 	mt.type as cla_type ");
		query.append("FROM " + schemaName + "menu as m ");
		query.append("INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
		query.append("INNER JOIN " + schemaName + "access_list_menu as alm ON (alm.mnu_id = m.mnu_id) ");
		query.append("INNER JOIN " + schemaName + "user as u ON ( u.acl_id = alm.acl_id ) ");
		query.append("WHERE m.type_cla = :type ");
		query.append("AND m.root_id is null ");
		query.append("AND u.usr_id = :usrId ");
		query.append("ORDER BY alm.mnu_order, m.mnu_id ASC ");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "type", MenuType.PORTAL_PRODUCT.getType().getId() );
		params.addValue( "usrId", usrId );
		
		try {
			list = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	
		return list;
	}
	
	/**
	 * Lista os sub-itens do menu princial (sem os menus de configurações)
	 * @param usrId
	 * @return Lista de menus
	 * @throws AppException
	 */
	@Override
	public List<MenuModel> listSub(Integer usrId) throws AppException {
		List<MenuModel> list = null;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(" 	m.mnu_id, ");
		query.append(" 	m.name, ");
		query.append(" 	fnMenu(m.mnu_id) as path, ");
		query.append(" 	m.url, ");
		query.append(" 	m.description, ");
		query.append(" 	m.icon, ");
		query.append(" 	m.root_id, ");
		query.append("  m.show, ");
		query.append(" 	mt.cla_id, ");
		query.append(" 	mt.value as cla_value, ");
		query.append(" 	mt.type as cla_type ");
		query.append("FROM " + schemaName + "menu as m ");
		query.append("INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
		query.append("LEFT JOIN " + schemaName + "access_list_menu as alm ON (alm.mnu_id = m.mnu_id) ");
		query.append("LEFT JOIN " + schemaName + "user as u ON ( u.acl_id = alm.acl_id ) ");
		query.append("WHERE m.type_cla = :type ");
		query.append("AND m.root_id is not null ");
		query.append("AND u.usr_id = :usrId ");
		query.append("ORDER BY alm.mnu_order, m.mnu_id ASC ");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "type", MenuType.PORTAL_PRODUCT.getType().getId());
		params.addValue( "usrId", usrId );
		
		try {
			list = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return list;
	}
	
	@Override
	public boolean hasChildren(Integer rootId) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT COUNT(m.mnu_id) FROM " + schemaName + "menu as m WHERE m.root_id = :rootId");
			MapSqlParameterSource params = new MapSqlParameterSource("rootId", rootId);
			Integer count = this.getJdbcTemplatePortal().queryForObject(query.toString(), params, Integer.class);
			return (count > 0);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public List<MenuModel> listByAccesslistId(Integer aclId) throws AppException {
		List<MenuModel> list = null;
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(" 	m.mnu_id, ");
		query.append(" 	m.name, ");
		query.append(" 	fnMenu(m.mnu_id) as path, ");
		query.append(" 	m.url, ");
		query.append(" 	m.description, ");
		query.append(" 	m.icon, ");
		query.append(" 	m.root_id, ");
		query.append("  m.show, ");
		query.append(" 	mt.cla_id, ");
		query.append(" 	mt.value as cla_value, ");
		query.append(" 	mt.type as cla_type ");
		query.append("FROM " + schemaName + "menu as m ");
		query.append("INNER JOIN " + schemaName + "classifier as mt ON m.type_cla = mt.cla_id ");
		query.append("LEFT JOIN " + schemaName + "access_list_menu as alm ON ( m.mnu_id = alm.mnu_id ) ");
		query.append("WHERE alm.acl_id = :aclId");
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "aclId", aclId );
		
		try {
			list = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return list;
	}
	
	@Override
	public boolean hasAccesslist(MenuModel menu) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT COUNT(m.mnu_id) ");
			query.append("FROM " + schemaName + "menu as m "); 
			query.append("INNER JOIN " + schemaName + "access_list_menu as alm ON ( m.mnu_id = alm.mnu_id ) "); 
			query.append("WHERE m.mnu_id = :mnuId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("mnuId", menu.getId());
			
			Integer count = this.getJdbcTemplatePortal().queryForObject(query.toString(), params, Integer.class);
			
			return (count > 0);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	@Override
	public boolean menuExists(MenuModel menu) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT COUNT(m.mnu_id) ");
			query.append("FROM " + schemaName + "menu as m "); 
			query.append("WHERE m.name = :name ");
			query.append("AND m.url= :url ");
			query.append("AND m.type_cla= :type ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", menu.getName());
			params.addValue("url", menu.getRoute());
			params.addValue("type", menu.getType().getId());
			
			Integer count = this.getJdbcTemplatePortal().queryForObject(query.toString(), params, Integer.class);
			
			return (count > 0);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
}
