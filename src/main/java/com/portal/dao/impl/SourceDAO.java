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
import com.portal.dao.ISourceDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.SourceMapper;
import com.portal.model.Source;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SourceDAO extends BaseDAO implements ISourceDAO {

	@Override
	public List<Source> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id");	
			}
			
			Order order = Order.desc( "src_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}

			String query = 	"SELECT * " +
							"FROM source src " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			
			log.trace( "[QUERY] source.listAll: {}", query );

			return this.getJdbcTemplatePortal().query( query, new SourceMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar as fontes.", e );
			throw new AppException( "Erro ao listar as fontes.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Source> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Source> find(Source model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id");	
			}
			
			Order order = Order.asc( "src_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM source src ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND src.src_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name = :name " );
					params.addValue("name", model.getName());
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
			
			log.trace( "[QUERY] source.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new SourceMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as fontes.", e );
			throw new AppException( "Erro ao buscar as fontes.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Source, Pageable)}
	 */
	@Override
	public Optional<Source> find(Source model) throws AppException {
		List<Source> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<Source> search(Source model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "src_id");	
			}
			
			Order order = Order.asc( "src_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM source src ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND src.src_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
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
			
			log.trace( "[QUERY] source.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new SourceMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar as fontes.", e );
			throw new AppException( "Erro ao procurar as fontes.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Source, Pageable)}
	 */
	@Override
	public List<Source> search(Source model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<Source> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM source src " +
							"WHERE src.src_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] source.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new SourceMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a  fonte.", e );
			throw new AppException( "Erro ao consultar a  fonte.", e );
		}
	}

	@Override
	public Optional<Source> save(Source model) throws AppException {
		try {
			String query = "INSERT INTO source ( name, active ) VALUES ( :name, :active ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase());
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
	
			log.trace( "[QUERY] source.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a fonte: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a fonte.", e);
		}
	}

	@Override
	public Optional<Source> update(Source model) throws AppException {
		try {
			String query = "UPDATE source SET name=:name, active=:active WHERE src_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
	
			log.trace( "[QUERY] source.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a fonte: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a fonte.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM source WHERE src_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] source.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a fonte" , e );
			throw new AppException( "Erro ao excluir a fonte.", e );
		}

	}
	
	@Override
	public boolean hasLeadRelationship(Integer srcId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT src_id FROM `lead` WHERE src_id = :srcId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "srcId", srcId );

			log.trace( "[QUERY] source.hasLeadRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com lead." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com lead.", e );
		}
	}
}
