package com.portal.dao.impl;

import java.util.ArrayList;
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
import com.portal.dao.IAccessListCheckPointDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.CheckpointMapper;
import com.portal.model.AccessListModel;
import com.portal.model.CheckpointModel;

@Repository
public class AccessListCheckPointDAO extends BaseDAO implements IAccessListCheckPointDAO {
	
	private static final Logger logger = LoggerFactory.getLogger(AccessListCheckPointDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public List<CheckpointModel> listCheckpointByAccessList(Integer ckpId, Integer aclId) throws AppException {
		List<CheckpointModel> listReturn = new ArrayList<>();
		
		try {
			StringBuilder query = new StringBuilder();
			query.append(" SELECT ");
			query.append(" 	c.ckp_id, ");
			query.append(" 	c.name, ");
			query.append(" 	c.description ");
			query.append(" FROM " + schemaName + "checkpoint as c ");
			query.append(" INNER JOIN " + schemaName + "access_list_checkpoint as ac on c.ckp_id = ac.ckp_id ");
			query.append(" WHERE TRUE ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(ckpId != null && ckpId > 0) {
				query.append(" AND ac.ckp_id = :ckpId ");
				params.addValue("ckpId", ckpId);
			}
			
			if(aclId != null && aclId > 0) {
				query.append(" AND ac.acl_id = :acl_id ");
				params.addValue("acl_id", aclId);
			}
			
			List<CheckpointModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new CheckpointMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				listReturn = list;
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return listReturn;
	}
	
	public Optional<AccessListModel> save(AccessListModel model, CheckpointModel checkpoint) throws AppException {
		try { 
			StringBuilder query = new StringBuilder("");
			query.append( "INSERT INTO " + schemaName + "access_list_checkpoint ( " );
			query.append( "acl_id, " );
			query.append( "ckp_id ) " );
			query.append( "VALUES ( " );
			query.append( ":aclId, " );
			query.append( ":ckpId " );
			query.append( ") " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("aclId", model.getId());
			params.addValue("ckpId", checkpoint.getId());
			
			this.getJdbcTemplatePortal().update( query.toString(), params);
			
			return Optional.ofNullable(model);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
	
	
	@Override
	public void delete(Integer ckpId, Integer aclId) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("DELETE FROM " + schemaName + "access_list_checkpoint WHERE ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(ckpId != null && ckpId > 0) {
				query.append(" ckp_id = :ckpId");
				params.addValue("ckpId", ckpId);
			}
			
			if(aclId != null && aclId > 0) {
				query.append(" acl_id = :aclId");
				params.addValue("aclId", aclId);
			}
			
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}
		
}
