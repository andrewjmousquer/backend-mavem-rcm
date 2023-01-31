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
import com.portal.dao.IItemTypeDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ItemTypeMapper;
import com.portal.model.ItemType;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ItemTypeDAO extends BaseDAO implements IItemTypeDAO {

	@Override
	public List<ItemType> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id");	
			}
			
			Order order = Order.desc( "itt_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT * " +
							"FROM item_type itt " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] itemType.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ItemTypeMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os tipos de itens.", e );
			throw new AppException( "Erro ao listar os tipos de itens.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<ItemType> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<ItemType> find(ItemType model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id");	
			}
			
			Order order = Order.asc( "itt_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM item_type itt ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND itt.itt_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name = :name " );
					params.addValue("name", model.getName());
					hasFilter = true;
				}
				
				if( model.getMandatory() != null ) {
					query.append(" AND mandatory = :mandatory " );
					params.addValue("mandatory", PortalNumberUtils.booleanToInt( model.getMandatory() ) );
					hasFilter = true;
				}
				
				if( model.getMulti() != null ) {
					query.append(" AND multi = :multi " );
					params.addValue("multi", PortalNumberUtils.booleanToInt( model.getMulti() ) );
					hasFilter = true;
				}
				
				if( model.getSeq() != null ) {
					query.append(" AND seq = :seq " );
					params.addValue("seq", model.getSeq() );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] itemType.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ItemTypeMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os tipos de itens.", e );
			throw new AppException( "Erro ao buscar os tipos de itens.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(ItemType, Pageable)}
	 */
	@Override
	public Optional<ItemType> find(ItemType model) throws AppException {
		List<ItemType> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<ItemType> search(ItemType model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itt_id");	
			}
			
			Order order = Order.asc( "itt_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM item_type itt ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND itt.itt_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
					hasFilter = true;
				}
				
				if( model.getMandatory() != null ) {
					query.append(" AND mandatory = :mandatory " );
					params.addValue("mandatory", PortalNumberUtils.booleanToInt( model.getMandatory() ) );
					hasFilter = true;
				}
				
				if( model.getMulti() != null ) {
					query.append(" AND multi = :multi " );
					params.addValue("multi", PortalNumberUtils.booleanToInt( model.getMulti() ) );
					hasFilter = true;
				}
				
				if( model.getSeq() != null ) {
					query.append(" AND seq = :seq " );
					params.addValue("seq", model.getSeq() );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] itemType.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ItemTypeMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os tipos de itens.", e );
			throw new AppException( "Erro ao procurar os tipos de itens.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(ItemType, Pageable)}
	 */
	@Override
	public List<ItemType> search(ItemType model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<ItemType> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM item_type itt " +
							"WHERE itt.itt_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] itemType.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ItemTypeMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o tipo de item.", e );
			throw new AppException( "Erro ao consultar o tipo de item.", e );
		}
	}

	@Override
	public Optional<ItemType> save(ItemType model) throws AppException {
		try {
			String query = "INSERT INTO item_type ( name, mandatory, multi, seq ) VALUES ( :name, :mandatory, :multi, :seq ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "mandatory", PortalNumberUtils.booleanToInt( model.getMandatory() ) );
			params.addValue( "multi", PortalNumberUtils.booleanToInt( model.getMulti() ) );
			params.addValue( "seq", model.getSeq() );
	
			log.trace( "[QUERY] itemType.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o tipo de item: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o tipo de item.", e);
		}
	}

	@Override
	public Optional<ItemType> update(ItemType model) throws AppException {
		try {
			String query = "UPDATE item_type SET name=:name, mandatory=:mandatory, multi=:multi, seq=:seq WHERE itt_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "mandatory", PortalNumberUtils.booleanToInt( model.getMandatory() ) );
			params.addValue( "multi", PortalNumberUtils.booleanToInt( model.getMulti() ) );
			params.addValue( "seq", model.getSeq() );
	
			log.trace( "[QUERY] itemType.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o tipo de item: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o tipo de item.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM item_type WHERE itt_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] itemType.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o tipo de item" , e );
			throw new AppException( "Erro ao excluir o tipo de item.", e );
		}

	}

	@Override
	public boolean hasItemRelationship(Integer itemTypeId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT itt_id FROM item WHERE itt_id = :ittId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ittId", itemTypeId );

			log.trace( "[QUERY] itemType.hasItemRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com item." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com item.", e );
		}
	}
}
