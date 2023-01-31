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
import com.portal.dao.IAddressDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.AddressMapper;
import com.portal.model.AddressModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class AddressDAO extends BaseDAO implements IAddressDAO {

	private static final Logger logger = LoggerFactory.getLogger(AddressDAO.class);	
	
	@Autowired
	private MessageSource messageSource;
	
	@Override
	public Optional<AddressModel> find(AddressModel model) throws AppException {
		Optional<AddressModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	a.add_id, ");
			query.append( "	a.street, ");
			query.append( "	a.number, ");
			query.append( "	a.district, ");
			query.append( "	a.complement, ");
			query.append( "	a.zip_code,");
			query.append( "	a.latitude, ");
			query.append( "	a.longitude, ");
			query.append( "	c.cit_id, ");
			query.append( "	c.name as nameCity, " );
			query.append( "	s.ste_id, ");
			query.append( "	s.name as nameState, " );
			query.append( "	s.abbreviation as abbrevState, " );
			query.append( "	co.cou_id, " );
			query.append( "	co.name as nameCountry, " );
			query.append( "	co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "address as a ");
			query.append( "INNER JOIN " + schemaName + "city as c on c.cit_id = a.cit_id " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append("WHERE ");
			query.append("	a.add_id = :addId ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "addId", ((model.getId() > 0) ? model.getId() : null) );
						
			query.append( "ORDER BY a.add_id " );
			log.trace( "[QUERY] ProposalForm.getListProposalFrontForm: {} [PARAMS]: {}", query, params.getValues() );
			
			List<AddressModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new AddressMapper() );
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
	public Optional<AddressModel> getById(Integer id) throws AppException {
		Optional<AddressModel> objReturn = Optional.empty();
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	a.add_id, ");
			query.append( "	a.street, ");
			query.append( "	a.number, ");
			query.append( "	a.district, ");
			query.append( "	a.complement, ");
			query.append( "	a.zip_code,");
			query.append( "	a.latitude, ");
			query.append( "	a.longitude, ");
			query.append( "	c.cit_id, ");
			query.append( "	c.name as nameCity, " );
			query.append( "	s.ste_id, ");
			query.append( "	s.name as nameState, " );
			query.append( "	s.abbreviation as abbrevState, " );
			query.append( "	co.cou_id, " );
			query.append( "	co.name as nameCountry, " );
			query.append( "	co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "address as a ");
			query.append( "INNER JOIN " + schemaName + "city as c on c.cit_id = a.cit_id " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append( "WHERE a.add_id = :addId " );
			query.append( "ORDER BY a.street " );
						
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("addId", id);
			
			List<AddressModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new AddressMapper() );
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
	public List<AddressModel> list() throws AppException {
		List<AddressModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	a.add_id, ");
			query.append( "	a.street, ");
			query.append( "	a.number, ");
			query.append( "	a.district, ");
			query.append( "	a.complement, ");
			query.append( "	a.zip_code,");
			query.append( "	a.latitude, ");
			query.append( "	a.longitude, ");
			query.append( "	c.cit_id, ");
			query.append( "	c.name as nameCity, " );
			query.append( "	s.ste_id, ");
			query.append( "	s.name as nameState, " );
			query.append( "	s.abbreviation as abbrevState, " );
			query.append( "	co.cou_id, " );
			query.append( "	co.name as nameCountry, " );
			query.append( "	co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "address as a ");
			query.append( "INNER JOIN " + schemaName + "city as c on c.cit_id = a.cit_id " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append( "ORDER BY a.street " );
			
			List<AddressModel> users = this.getJdbcTemplatePortal().query( query.toString(), new AddressMapper() );
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
	public List<AddressModel> search(AddressModel model) throws AppException {
		List<AddressModel> listReturn = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append( "SELECT " );
			query.append( "	a.add_id, ");
			query.append( "	a.street, ");
			query.append( "	a.number, ");
			query.append( "	a.district, ");
			query.append( "	a.complement, ");
			query.append( "	a.zip_code,");
			query.append( "	a.latitude, ");
			query.append( "	a.longitude, ");
			query.append( "	c.cit_id, ");
			query.append( "	c.name as nameCity, " );
			query.append( "	s.ste_id, ");
			query.append( "	s.name as nameState, " );
			query.append( "	s.abbreviation as abbrevState, " );
			query.append( "	co.cou_id, " );
			query.append( "	co.name as nameCountry, " );
			query.append( "	co.abbreviation as abbrevCountry " );
			query.append( "FROM " + schemaName + "address as a ");
			query.append( "INNER JOIN " + schemaName + "city as c on c.cit_id = a.cit_id " );
			query.append( "INNER JOIN " + schemaName + "state as s on s.ste_id = c.ste_id " );
			query.append( "INNER JOIN " + schemaName + "country as co on co.cou_id = s.cou_id " );
			query.append("WHERE ");
			query.append("	a.add_id <=> coalesce(:addId, a.add_id) ");
			query.append("	OR a.street <=> coalesce(:street, a.street) ");
			query.append("	OR a.number <=> coalesce(:number, a.number) ");
			query.append("	OR a.district <=> coalesce(:district, a.district) ");
			query.append("	OR a.complement <=> coalesce(:complement, a.complement) ");
			query.append("	OR a.zip_code <=> coalesce(:zip_code, a.zip_code) ");
			query.append("	OR a.latitude <=> coalesce(:latitude, a.latitude) ");
			query.append("	OR a.longitude <=> coalesce(:longitude, a.longitude) ");
			query.append("	OR a.cit_id <=> coalesce(:citId, a.cit_id) ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "addId", ((model.getId() > 0) ? model.getId() : null) );
			params.addValue( "street", model.getStreet() );
			params.addValue( "number", model.getNumber() );
			params.addValue( "district", model.getDistrict() );
			params.addValue( "complement", model.getComplement() );
			params.addValue( "zip_code", model.getZipCode() );
			params.addValue( "latitude", model.getLatitude() );
			params.addValue( "longitude", model.getLongitude() );
			params.addValue( "citId", ( model.getCity() != null && model.getCity().getId() > 0 ) ? model.getCity().getId() : null );
						
			query.append( "ORDER BY a.street " );
			
			List<AddressModel> users = this.getJdbcTemplatePortal().query( query.toString(), params, new AddressMapper() );
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
	public Optional<AddressModel> save(AddressModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder("");
			query.append( "INSERT INTO " + schemaName + "address ( " );
			query.append( "street, " );
			query.append( "number, " );
			query.append( "district, " );
			query.append( "complement, " );
			query.append( "zip_code, " );
			query.append( "latitude, " );
			query.append( "longitude, " );
			query.append( "cit_id) " );
			query.append( "VALUES ( " );
			query.append( ":street, " );
			query.append( ":number, " );
			query.append( ":district, " );
			query.append( ":complement, " );
			query.append( ":zip_code, " );
			query.append( ":latitude, " );
			query.append( ":longitude, " );
			query.append( ":citId " );
			query.append( ") " );
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("street", (model.getStreet() != null ? model.getStreet().toUpperCase() : null));
			params.addValue("number", (model.getNumber() != null ? model.getNumber().toUpperCase() : null)); 
			params.addValue("district", (model.getDistrict() != null ? model.getDistrict().toUpperCase() : null)); 
			params.addValue("complement", (model.getComplement() != null ? model.getComplement().toUpperCase() : null));
			params.addValue("zip_code", (model.getZipCode() != null ? model.getZipCode().toUpperCase() : null));
			params.addValue("latitude", (model.getLatitude() != null ? model.getLatitude().toUpperCase() : null));
			params.addValue("longitude", (model.getLongitude() != null ? model.getLongitude().toUpperCase() : null));
			params.addValue("citId", (model.getCity() != null) ? model.getCity().getId() : null);
			
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
	public Optional<AddressModel> update(AddressModel model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append( "UPDATE " + schemaName + "address " );
			query.append( "SET ");
			query.append( "street = :street, ");
			query.append( "number = :number, ");
			query.append( "district = :district, ");
			query.append( "complement = :complement, ");
			query.append( "zip_code = :zip_code, ");
			query.append( "latitude = :latitude, ");
			query.append( "longitude = :longitude, ");
			query.append( "cit_id = :citId ");
			query.append( "WHERE add_id = :addId ");

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("street", (model.getStreet() != null ? model.getStreet().toUpperCase() : null));
			params.addValue("number", (model.getNumber() != null ? model.getNumber().toUpperCase() : null)); 
			params.addValue("district", (model.getDistrict() != null ? model.getDistrict().toUpperCase() : null)); 
			params.addValue("complement", (model.getComplement() != null ? model.getComplement().toUpperCase() : null));
			params.addValue("zip_code", (model.getZipCode() != null ? model.getZipCode().toUpperCase() : null));
			params.addValue("latitude", (model.getLatitude() != null ? model.getLatitude().toUpperCase() : null));
			params.addValue("longitude", (model.getLongitude() != null ? model.getLongitude().toUpperCase() : null));
			params.addValue("citId", (model.getCity() != null) ? model.getCity().getId() : null);
			params.addValue("addId", model.getId());
			
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
			query.append("DELETE FROM " + schemaName + "address WHERE add_id = :id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource("id", id);
			this.getJdbcTemplatePortal().update( query.toString(), params );
		} catch( Exception e ) {
			logger.error(e.getMessage());
			throw new AppException(this.messageSource.getMessage("error.generic", null, LocaleContextHolder.getLocale()));
		}
	}

}
