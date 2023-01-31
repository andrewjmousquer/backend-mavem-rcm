package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IBankDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.BankMapper;
import com.portal.model.Bank;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class BankDAO extends BaseDAO implements IBankDAO {
	
	@Override
	public List<Bank> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id");	
			}
			
			Order order = Order.desc( "bnk_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT * " +
							"FROM bank bnk " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] bank.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new BankMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os bancos.", e );
			throw new AppException( "Erro ao listar os bancos.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Bank> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Bank> find(Bank model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id");	
			}
			
			Order order = Order.asc( "bnk_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM bank bnk ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND bnk.bnk_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name = :name " );
					params.addValue("name", model.getName());
					hasFilter = true;
				}
				
				if( model.getCode() != null ) {
					query.append(" AND code = :code " );
					params.addValue("code", model.getCode() );
					hasFilter = true;
				}
				
				if( model.getActive() != null ) {
					query.append(" AND active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] bank.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new BankMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os bancos.", e );
			throw new AppException( "Erro ao buscar os bancos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Bank, Pageable)}
	 */
	@Override
	public Optional<Bank> find(Bank model) throws AppException {
		List<Bank> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<Bank> search(Bank model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id");	
			}
			
			Order order = Order.asc( "bnk_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM bank bnk ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND bnk.bnk_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
					hasFilter = true;
				}
				
				if( model.getCode() != null ) {
					query.append(" AND code LIKE :code " );
					params.addValue("code", this.mapLike(model.getCode()));
					hasFilter = true;
				}
				
				if( model.getActive() != null ) {
					query.append(" AND active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] bank.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new BankMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os bancos.", e );
			throw new AppException( "Erro ao procurar os bancos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Bank, Pageable)}
	 */
	@Override
	public List<Bank> search(Bank model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<Bank> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM bank bnk " +
							"WHERE bnk.bnk_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] bank.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new BankMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o banco.", e );
			throw new AppException( "Erro ao consultar o banco.", e );
		}
	}

	@Override
	public Optional<Bank> save(Bank model) throws AppException {
		try {
			String query = "INSERT INTO bank ( name, code, active ) VALUES ( :name, :code, :active ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "code", model.getCode().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
	
			log.trace( "[QUERY] bank.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o banco: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o banco.", e);
		}
	}

	@Override
	public Optional<Bank> update(Bank model) throws AppException {
		try {
			String query = "UPDATE bank SET name=:name, code=:code, active=:active WHERE bnk_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "code", model.getCode().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
	
			log.trace( "[QUERY] bank.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o banco: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o banco.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM bank WHERE bnk_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] bank.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o banco" , e );
			throw new AppException( "Erro ao excluir o banco.", e );
		}
	}
	
	@Override
	public boolean hasBankAccountRelationship( Integer bankId ) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT act_id FROM bank_account WHERE bnk_id = :bnkId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "bnkId", bankId );

			log.trace( "[QUERY] bank.hasBankAccountRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com conta bancária." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com conta bancária.", e );
		}
	}
}
