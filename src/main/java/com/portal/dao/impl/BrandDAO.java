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
import com.portal.dao.IBrandDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.BrandMapper;
import com.portal.model.Brand;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class BrandDAO extends BaseDAO implements IBrandDAO {

	@Override
	public List<Brand> listAll( Pageable pageable ) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "brd_id");	
			}
			
			Order order = Order.desc( "brd_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT * " +
							"FROM brand brd " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			
			log.trace( "[QUERY] brand.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new BrandMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar as marcas.", e );
			throw new AppException( "Erro ao listar as marcas.", e );
		}
	}

	@Override
	public Optional<Brand> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM brand brd " +
							"WHERE brd.brd_id = :id " +
							"LIMIT 1";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] brand.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new BrandMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a marca.", e );
			throw new AppException( "Erro ao consultar a marca.", e );
		}
	}
	
	@Override
	public Optional<Brand> save(Brand brand) throws AppException {
		try {
			String query = "INSERT INTO brand ( name, active ) VALUES ( :name, :active ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", brand.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( brand.getActive() ) );

			log.trace( "[QUERY] brand.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        brand.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( brand );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a marca: {}", brand, e );
			throw new AppException( "Erro ao tentar salvar a marca.", e);
		}
	}

	@Override
	public Optional<Brand> update(Brand brand) throws AppException {
		try {
			String query = "UPDATE brand SET name=:name, active=:active WHERE brd_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", brand.getId() );
			params.addValue( "name", brand.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( brand.getActive() ) );

			log.trace( "[QUERY] brand.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(brand);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a marca: {}", brand, e );
			throw new AppException( "Erro ao tentar atualizar a marca.", e);
		}
	}
	
	@Override
	public void delete(Integer id) throws AppException {

		try {
			String query = 	"DELETE FROM brand WHERE brd_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] brand.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a marca.", e );
			throw new AppException( "Erro ao excluir a marca.", e );
		}
		
	}
	
	@Override
	public List<Brand> find(Brand brand, Pageable pageable) throws AppException {
		
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "brd_id");	
			}
			
			Order order = Order.asc( "brd_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM brand brd ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(brand != null) {
				if(brand.getId() != null && brand.getId() > 0) {
					query.append(" AND brd.brd_id = :id ");
					params.addValue("id", brand.getId());
					hasFilter = true;
				}
		
				if(brand.getName() != null && !brand.getName().equals("")) {
					query.append(" AND brd.name LIKE :name " );
					params.addValue("name", mapLike(brand.getName()));
					hasFilter = true;
				}
				
				if( brand.getActive() != null ) {
					query.append(" AND active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( brand.getActive() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] brand.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new BrandMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as marcas.", e );
			throw new AppException( "Erro ao buscar as marcas.", e );
		}
	}
	
	@Override
	public List<Brand> search(Brand brand, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "brd_id");	
			}
			
			Order order = Order.asc( "brd_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM brand brd ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(brand != null) {
				if(brand.getId() != null && brand.getId() > 0) {
					query.append(" AND brd.brd_id = :id ");
					params.addValue("id", brand.getId());
					hasFilter = true;
				}
		
				if(brand.getName() != null && !brand.getName().equals("")) {
					query.append(" AND name like :name " );
					params.addValue("name", this.mapLike(brand.getName()));
					hasFilter = true;
				}
				
				if( brand.getActive() != null ) {
					query.append(" AND active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( brand.getActive() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] brand.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new BrandMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar as marcas.", e );
			throw new AppException( "Erro ao procurar as marcas.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Brand, Pageable)}
	 */
	@Override
	public Optional<Brand> find( Brand brand ) throws AppException {
		List<Brand> brands = this.find( brand, null );
		return Optional.ofNullable( ( brands != null ? brands.get(0) : null ) ); 
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	@Deprecated
	public List<Brand> list() throws AppException {
		return this.listAll(null);
	}

	/**
	 * @deprecated Usar a função {@link #search(Brand, Pageable)}
	 */
	@Override
	public List<Brand> search(Brand model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public boolean hasModelRelationship(Integer brandId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT brd_id FROM model WHERE brd_id = :brdId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "brdId", brandId );

			log.trace( "[QUERY] brand.hasModelRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com modelo." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com modelo.", e );
		}
	}

	@Override
	public boolean hasLeadRelationship(Integer brandId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT brd_id FROM `lead` WHERE brd_id = :brdId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "brdId", brandId );

			log.trace( "[QUERY] brand.hasLeadRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com lead." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com lead.", e );
		}
	}

	@Override
	public boolean hasPartnerRelationship(Integer brandId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT brd_id FROM partner_brand WHERE brd_id = :brdId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "brdId", brandId );

			log.trace( "[QUERY] brand.hasLeadRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com lead." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com lead.", e );
		}
	}
}
