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
import com.portal.dao.IProductDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProductMapper;
import com.portal.model.Product;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProductDAO extends BaseDAO implements IProductDAO {



	@Override
	public List<Product> listAll(Pageable pageable) throws AppException {
		try {
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id");	
			}
			
			Order order = Order.desc( "prd_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT * " +
							"FROM product prd " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] product.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ProductMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os produtos.", e );
			throw new AppException( "Erro ao listar os produtos.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Product> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Product> find(Product model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id");	
			}
			
			Order order = Order.asc( "prd_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM product prd ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND prd.prd_id = :id ");
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
				
				if(model.getProposalExpirationDays() != null ) {
					query.append(" AND proposal_expiration_days = :proposalExpirationDays " );
					params.addValue("proposalExpirationDays", model.getProposalExpirationDays());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );

			log.trace( "[QUERY] product.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProductMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os produtos.", e );
			throw new AppException( "Erro ao buscar os produtos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Product, Pageable)}
	 */
	@Override
	public Optional<Product> find(Product model) throws AppException {
		List<Product> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<Product> search(Product model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prd_id");	
			}
			
			Order order = Order.asc( "prd_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM product prd ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND prd.prd_id = :id ");
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
				
				if(model.getProposalExpirationDays() != null ) {
					query.append(" AND proposal_expiration_days = :proposalExpirationDays " );
					params.addValue("proposalExpirationDays", model.getProposalExpirationDays());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] product.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProductMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os produtos.", e );
			throw new AppException( "Erro ao procurar os produtos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Product, Pageable)}
	 */
	@Override
	public List<Product> search(Product model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<Product> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM product prd " +
							"WHERE prd.prd_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] product.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ProductMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o produto.", e );
			throw new AppException( "Erro ao consultar o produto.", e );
		}
	}

	@Override
	public Optional<Product> save(Product model) throws AppException {
		try {
			String query = "INSERT INTO product ( name, active, proposal_expiration_days, product_description ) VALUES ( :name, :active, :proposalExpirationDays, :productDescription ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			params.addValue( "proposalExpirationDays", model.getProposalExpirationDays());
			params.addValue( "productDescription", model.getProductDescription());
			
			log.trace( "[QUERY] product.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );

	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o produto: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o produto.", e);
		}
	}

	@Override
	public Optional<Product> update(Product model) throws AppException {
		try {
			String query = "UPDATE product "
					+ "SET name=:name, active=:active, proposal_expiration_days=:proposalExpirationDays, product_description=:productDescription "
					+ "WHERE prd_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			params.addValue( "proposalExpirationDays", model.getProposalExpirationDays());
			params.addValue( "productDescription", model.getProductDescription());
	
			log.trace( "[QUERY] product.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o produto: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o produto.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM product WHERE prd_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] product.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o produto" , e );
			throw new AppException( "Erro ao excluir o produto.", e );
		}

	}

	@Override
	public Optional<Product> getProductByProductModel(Integer prmId) throws AppException {
		
		try {
			
			String query = 	"select p.* from product p  "
							+ "inner join product_model pm on pm.prd_id = p.prd_id "
							+ "where pm.prm_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", prmId );

			log.trace( "[QUERY] product.getProductByProductModel: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ProductMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o produto.", e );
			throw new AppException( "Erro ao consultar o produto.", e );
		}
	}
}
