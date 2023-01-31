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
import com.portal.dao.IHoldingDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.HoldingMapper;
import com.portal.model.HoldingModel;
import com.portal.utils.PortalNumberUtils;

@Repository
public class HoldingDAO extends BaseDAO implements IHoldingDAO {

	private static final Logger logger = LoggerFactory.getLogger(HoldingDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<HoldingModel> find(HoldingModel model) throws AppException {
		Optional<HoldingModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" h.hol_id, ");
			query.append(" h.name, ");
			query.append(" h.cnpj, ");
			query.append(" h.social_name, ");
			query.append(" h.state_registration, ");
			query.append(" h.municipal_registration, ");
			query.append(" h.add_id, ");
			query.append(" h.per_id, ");
			query.append(" h.type_cla, ");
			query.append(" h.type_cla, ");
			query.append("FROM " + schemaName + "holding AS h ");
			query.append("WHERE h.hol_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model.getId() != null && model.getId() > 0) {
				query.append(" AND h.hol_id = :holId ");
				params.addValue( "holId",  model.getId());
			}
			
			if(model.getName() != null) {
				query.append(" AND h.name = :name ");
				params.addValue( "name",  model.getName());
			}
			
			if(model.getCnpj() != null) {
				query.append(" AND h.cnpj = :cnpj ");
				params.addValue( "cnpj",  PortalNumberUtils.normalizeCnpj(model.getCnpj()));
			}
			
			if(model.getSocialName() != null) {
				query.append(" AND h.social_name = :razaoSocial ");
				params.addValue( "razaoSocial",  model.getSocialName());
			}
			
			if(model.getStateRegistration() != null) {
				query.append(" AND h.state_registration = :inscricaoEstadual ");
				params.addValue( "inscricaoEstadual",  model.getStateRegistration());
			}
			
			if(model.getStateRegistration() != null) {
				query.append(" AND h.municipal_registration = :inscricaoMunicipal ");
				params.addValue( "inscricaoMunicipal",  model.getStateRegistration());
			}
			
			if(model.getAddress() != null && model.getAddress().getId() > 0) {
				query.append(" AND h.add_id = :addId");
				params.addValue( "addId", model.getAddress().getId());
			}
			
			if(model.getPerson() != null && model.getPerson().getId() > 0) {
				query.append(" AND h.per_id = :perId ");
				params.addValue( "perId", model.getPerson().getId());
			}
			
			if(model.getType() != null && model.getType().getId() > 0) {
				query.append(" AND h.type_cla = :type_cla ");
				params.addValue( "perId", model.getType().getId());
			}
			
			List<HoldingModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new HoldingMapper() );
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
	public Optional<HoldingModel> getById(Integer id) throws AppException {
		Optional<HoldingModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" h.hol_id, " );
			query.append(" h.name," );
			query.append(" h.cnpj," );
			query.append(" h.social_name," );
			query.append(" h.state_registration," );
			query.append(" h.municipal_registration," );
			query.append(" h.add_id," );
			query.append(" h.per_id," );
			query.append(" h.type_cla ");
			query.append("FROM " + schemaName + "holding AS h ");
			query.append("WHERE h.hol_id = :holId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "holId", id);
			
			List<HoldingModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new HoldingMapper() );
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
	public List<HoldingModel> list() throws AppException {
		List<HoldingModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" h.hol_id, " );
			query.append(" h.name," );
			query.append(" h.cnpj," );
			query.append(" h.social_name," );
			query.append(" h.state_registration," );
			query.append(" h.municipal_registration," );
			query.append(" h.add_id," );
			query.append(" h.per_id, " );
			query.append(" h.type_cla ");
			query.append("FROM " + schemaName + "holding AS h ");
			query.append("ORDER BY h.name ");
						
			List<HoldingModel> list = this.getJdbcTemplatePortal().query( query.toString(), new HoldingMapper() );
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
	public List<HoldingModel> search(HoldingModel model) throws AppException {
		List<HoldingModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" h.hol_id, " );
			query.append(" h.name," );
			query.append(" h.cnpj," );
			query.append(" h.social_name," );
			query.append(" h.state_registration," );
			query.append(" h.municipal_registration," );
			query.append(" h.add_id," );
			query.append(" h.per_id, " );
			query.append(" h.type_cla ");
			query.append("FROM " + schemaName + "holding AS h ");
			query.append("WHERE h.hol_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model.getId() != null && model.getId() > 0) {
				query.append(" AND h.hol_id = :holId ");
				params.addValue( "holId",  model.getId());
			}
			
			if(model.getName() != null) {
				query.append(" AND (h.name like :name OR h.cnpj like :cnpj) ");
				params.addValue( "name",  this.mapLike(model.getName()));
				params.addValue( "cnpj",  this.mapLike(PortalNumberUtils.normalizeCnpj(model.getName())));
			}
			
			if(model.getCnpj() != null) {
				query.append(" AND h.cnpj like :cnpj ");
				params.addValue( "cnpj",  this.mapLike(PortalNumberUtils.normalizeCnpj(model.getCnpj())));
			}
			
			if(model.getSocialName() != null) {
				query.append(" AND h.social_name = :razaoSocial ");
				params.addValue( "razaoSocial",  model.getSocialName());
			}
			
			if(model.getStateRegistration() != null) {
				query.append(" AND h.state_registration = :inscricaoEstadual ");
				params.addValue( "inscricaoEstadual",  model.getStateRegistration());
			}
			
			if(model.getStateRegistration() != null) {
				query.append(" AND h.municipal_registration = :inscricaoMunicipal ");
				params.addValue( "inscricaoMunicipal",  model.getStateRegistration());
			}
			
			if(model.getAddress() != null && model.getAddress().getId() > 0) {
				query.append(" AND h.add_id = :addId");
				params.addValue( "addId", model.getAddress().getId());
			}
			
			if(model.getPerson() != null && model.getPerson().getId() > 0) {
				query.append(" AND h.per_id = :perId ");
				params.addValue( "perId", model.getPerson().getId());
			}
			
			if(model.getType() != null) {
				query.append(" AND h.type_cla = :type_cla ");
				params.addValue( "typeCla",  model.getType());
			}
			
			List<HoldingModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new HoldingMapper() );
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
	public Optional<HoldingModel> save(HoldingModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query.append("INSERT INTO  " + schemaName + "holding " );
			query.append("( " );
			query.append("name, " );
			query.append("logo, " );
			query.append("cnpj, " );
			query.append("social_name, " );
			query.append("state_registration, " );
			query.append("municipal_registration, " );
			query.append("add_id, " );
			query.append("per_id, " );
			query.append("type_cla) ");
			query.append("VALUES ( " );
			query.append(":name, " );
			query.append(":logo, " );
			query.append(":cnpj, " );
			query.append(":razaoSocial, " );
			query.append(":inscricaoEstadual, " );
			query.append(":inscricaoMunicipal, " );
			query.append(":addId, " );
			query.append(":perId, " );
			query.append(":type_cla)" );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("logo", model.getLogo());
			params.addValue("cnpj", PortalNumberUtils.normalizeCnpj(model.getCnpj()));
			params.addValue("razaoSocial", model.getSocialName());
			params.addValue("inscricaoEstadual", model.getStateRegistration());
			params.addValue("inscricaoMunicipal", model.getMunicipalRegistration());
			params.addValue("addId", (model.getAddress() != null) ? model.getAddress().getId() : null);
			params.addValue("perId", (model.getPerson() != null) ? model.getPerson().getId() : null);
			params.addValue("type_cla", (model.getType() != null) ? model.getType().getId() : null);
	
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
	public Optional<HoldingModel> update(HoldingModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE " + schemaName + "holding " );
			query.append("SET " );
			query.append("name = :name, " );
			query.append("logo = :logo, " );
			query.append("cnpj = :cnpj, " );
			query.append("social_name = :razaoSocial, " );
			query.append("state_registration = :inscricaoEstadual, " );
			query.append("municipal_registration = :inscricaoMunicipal, " );
			query.append("add_id = :addId, " );
			query.append("per_id = :perId, " );
			query.append("type_cla = :typeCla ");
			query.append("WHERE hol_id = :holId " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("logo", model.getLogo());
			params.addValue("cnpj", PortalNumberUtils.normalizeCnpj(model.getCnpj()));
			params.addValue("razaoSocial", model.getSocialName());
			params.addValue("inscricaoEstadual", model.getStateRegistration());
			params.addValue("inscricaoMunicipal", model.getMunicipalRegistration());
			params.addValue("addId", (model.getAddress() != null) ? model.getAddress().getId() : null);
			params.addValue("perId", (model.getPerson() != null) ? model.getPerson().getId() : null);
			params.addValue("holId", model.getId());
			params.addValue("typeCla", (model.getType() != null) ? model.getType().getId() : null);
			
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
			query.append("DELETE FROM " + schemaName + "holding WHERE hol_id = :holId");
			
			MapSqlParameterSource params = new MapSqlParameterSource("holId", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	public List<HoldingModel> listByUserId(Integer usrId) throws AppException {
		List<HoldingModel> holders = null;
		
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append(" hol.hol_id, " );
			query.append("	hol.name," );
			query.append("	hol.cnpj," );
			query.append("	hol.social_name," );
			query.append("	hol.state_registration," );
			query.append("	hol.municipal_registration," );
			query.append("	hol.add_id," );
			query.append("  hol.per_id, " );
			query.append( " hol.type_cla" );
			query.append("FROM " + schemaName + "holding AS hol " );
			query.append("INNER JOIN " + schemaName + "customer cus ON cus.hol_id = hol.hol_id " );
			query.append("INNER JOIN " + schemaName + "user_customer ucu ON ucu.cus_id = cus.cus_id " );
			query.append("WHERE ucu.usr_id = :usrId " );
			query.append("GROUP BY hol.hol_id " );
			query.append("ORDER BY hol.name " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "usrId", usrId );
	
			holders = this.getJdbcTemplatePortal().query( query.toString(), params, new HoldingMapper() );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return holders;
	}
	
	public boolean hasUser(Integer holId) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT COUNT(uc.usr_id) ");
			query.append("FROM " + schemaName + "user_customer as uc "); 
			query.append("JOIN " + schemaName + "customer as c ON ( uc.cus_id = c.cus_id ) ");
			query.append("WHERE c.hol_id = :holId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("holId", holId);
			
			Integer count = this.getJdbcTemplatePortal().queryForObject(query.toString(), params, Integer.class);
			return (count > 0);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<HoldingModel> getLogo( HoldingModel holding ) throws AppException {
		try {
			Optional<HoldingModel> objReturn = Optional.ofNullable(holding);
			
			StringBuilder query = new StringBuilder();
			query.append("SELECT logo ");
			query.append("FROM " + schemaName + "holding hol "); 
			query.append("WHERE hol.hol_id = :holId LIMIT 1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("holId", holding.getId());
			
			byte[] logo = this.getJdbcTemplatePortal().queryForObject( query.toString(), params, byte[].class );
			objReturn.get().setLogo( logo );
			return objReturn;
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

	@Override
	public Optional<HoldingModel> getDefaultHolding(Integer usrId) throws AppException {
		Optional<HoldingModel> objReturn = Optional.empty();
		
		StringBuilder query = new StringBuilder("");
		query.append("SELECT ");
		query.append(" hol.hol_id, " );
		query.append(" hol.name," );
		query.append(" hol.cnpj," );
		query.append(" hol.social_name," );
		query.append(" hol.state_registration," );
		query.append(" hol.municipal_registration," );
		query.append(" hol.add_id," );
		query.append(" hol.per_id, " );
		query.append(" hol.type_cla ");
		query.append("FROM " + schemaName + "holding AS hol " );
		query.append("INNER JOIN " + schemaName + "customer cus ON cus.hol_id = hol.hol_id " );
		query.append("INNER JOIN " + schemaName + "user_customer ucu ON ucu.cus_id = cus.cus_id " );
		query.append("WHERE ucu.usr_id = :usrId " );
		query.append("GROUP BY hol.hol_id " );
		query.append("ORDER BY hol.name " );
		query.append("LIMIT 1 " );
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "usrId", usrId );
		
		try {
			List<HoldingModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new HoldingMapper() );
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
	public Optional<HoldingModel> getHoldingByCustomer(Integer cusId) throws AppException {
		Optional<HoldingModel> objReturn = Optional.empty();
		
		StringBuilder query = new StringBuilder("");
		query.append("SELECT TOP 1 ");
		query.append(" hol.hol_id, " );
		query.append(" hol.name," );
		query.append(" hol.cnpj," );
		query.append(" hol.social_name," );
		query.append(" hol.state_registration," );
		query.append(" hol.municipal_registration," );
		query.append(" hol.add_id," );
		query.append(" hol.per_id, " );
		query.append(" hol.cod_clifor, " );
		query.append(" ifnull(fc.fcd_id, 0) as fcd_id, ");
		query.append(" fc.name as fiscal_condition, ");
		query.append(" hol.type_cla ");
		query.append("FROM " + schemaName + "holding AS hol " );
		query.append("INNER JOIN " + schemaName + "customer cus ON cus.hol_id = hol.hol_id " );
		query.append("LEFT JOIN " + schemaName + "fiscal_condition AS fc on hol.fcd_id = fc.fcd_id ");
		query.append("WHERE cus.cus_id = :cusId " );
		query.append("ORDER BY hol.name " );
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue( "cusId", cusId );
		
		try {
			List<HoldingModel> list = this.getJdbcTemplatePortal().query( query.toString(), params, new HoldingMapper() );
			if(!CollectionUtils.isEmpty(list)) {
				objReturn = Optional.ofNullable(list.get(0));
			}
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
		
		return objReturn;
	}
}