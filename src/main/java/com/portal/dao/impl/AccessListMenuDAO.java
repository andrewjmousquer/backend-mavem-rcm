package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.portal.config.BaseDAO;
import com.portal.dao.IAccessListMenuDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.MenuMapper;
import com.portal.model.AccessListModel;
import com.portal.model.MenuModel;

@Repository
public class AccessListMenuDAO extends BaseDAO implements IAccessListMenuDAO {
	
	private static final Logger logger = LoggerFactory.getLogger(AccessListMenuDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public List<MenuModel> listMenuByAccessList(Integer id) throws AppException {
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
			query.append(" 	ifnull(m.root_id,0) as root_id, ");
			query.append(" 	m.show, ");
			query.append(" 	cla.cla_id, ");
			query.append(" 	cla.value as cla_value, ");
			query.append(" 	cla.type as cla_type ");
			query.append(" FROM " + schemaName + "menu as m ");
			query.append(" INNER JOIN " + schemaName + "classifier as cla on m.type_cla = cla.cla_id ");
			query.append(" INNER JOIN " + schemaName + "access_list_menu as am on m.mnu_id = am.mnu_id ");
			query.append(" WHERE am.acl_id = :acl_id ");
			query.append(" ORDER BY am.mnu_order ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("acl_id", id);
			
			List<MenuModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new MenuMapper() );
			if(!CollectionUtils.isEmpty(users)) {
				listReturn = users;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}
	
	public Optional<AccessListModel> save(AccessListModel model, MenuModel menu) throws AppException {
		try { 
			StringBuilder query = new StringBuilder("");
			query.append( "INSERT INTO " + schemaName + "access_list_menu ( " );
			query.append( "acl_id, " );
			query.append( "mnu_id, " );
			query.append( "mnu_order ) " );
			query.append( "VALUES ( " );
			query.append( ":aclId, " );
			query.append( ":mnuId, " );
			query.append( ":mnuOrder " );
			query.append( ") " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("aclId", model.getId());
			params.addValue("mnuId", menu.getId());
			params.addValue("mnuOrder", menu.getMnuOrder() == null ? 0 : menu.getMnuOrder() );
			
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
			query.append("DELETE FROM " + schemaName + "access_list_menu WHERE acl_id = :id");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
}
