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
import com.portal.dao.IPriceProductDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PriceProductMapper;
import com.portal.model.PriceProduct;
import com.portal.model.ProductModel;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PriceProductDAO extends BaseDAO implements IPriceProductDAO {

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceProduct> list() throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceProduct> search(PriceProduct model) throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}
	
	@Override
	public List<PriceProduct> find( PriceProduct model, Pageable pageable ) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppr_id");	
			}
			
			Order order = Order.asc( "ppr_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT ppr.*, ");
			query.append("		 prl.name as price_list " ) ;
			query.append("FROM price_product ppr ");
			query.append("INNER JOIN price_list prl ON prl.prl_id = ppr.prl_id ");
			query.append("INNER JOIN product_model prm ON prm.prm_id = ppr.prm_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND ppr.ppr_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
				
				if( model.getProductModel() != null && model.getProductModel().getId() != null) {
					query.append(" AND ppr.prm_id = :prmId ");
					params.addValue("prmId",model.getProductModel().getId());
					hasFilter = true;
				}

				if( model.getPriceList() != null && model.getPriceList().getId() != null) {
					query.append(" AND ppr.prl_id = :prlId ");
					params.addValue("prlId",model.getPriceList().getId());
					hasFilter = true;
				}
			}
			
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] priceProduct.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceProductMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar o preço dos produtos.", e );
			throw new AppException( "Erro ao buscar o preço dos produtos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(PriceProduct, Pageable)}
	 */
	@Override
	public Optional<PriceProduct> find(PriceProduct model) throws AppException {
		List<PriceProduct> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public Optional<PriceProduct> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT ppr.*, " +
							"		prl.name as price_list " +
							"FROM price_product ppr " +
							"INNER JOIN price_list prl ON prl.prl_id = ppr.prl_id " +
							"INNER JOIN product_model prm ON prm.prm_id = ppr.prm_id " +
							"WHERE ppr.ppr_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] priceProduct.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PriceProductMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o preço do produto.", e );
			throw new AppException( "Erro ao consultar o preço do produto.", e );
		}
	}

	@Override
	public Optional<PriceProduct> save(PriceProduct model) throws AppException {
		try {
			String query = "INSERT INTO price_product ( ppr_id, price, prm_id, prl_id ) VALUES ( NULL, :price, :prmId, :prlId ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "price", model.getPrice() );
			params.addValue( "prmId", ( model.getProductModel() == null ? null : model.getProductModel().getId() ) );
			params.addValue( "prlId", ( model.getPriceList() == null ? null : model.getPriceList().getId() ) );
	
			log.trace( "[QUERY] priceProduct.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o preço do produto: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o preço do produto.", e);
		}
	}

	@Override
	public Optional<PriceProduct> update(PriceProduct model) throws AppException {
		try {
			String query = "UPDATE price_product SET price=:price, prm_id=:prmId, prl_id=:prlId WHERE ppr_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "price", model.getPrice() );
			params.addValue( "prmId", ( model.getProductModel() == null ? null : model.getProductModel().getId() ) );
			params.addValue( "prlId", ( model.getPriceList() == null ? null : model.getPriceList().getId() ) );
	
			log.trace( "[QUERY] priceProduct.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o preço do produto: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o preço do produto.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM price_product WHERE ppr_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] priceProduct.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o preço do produto" , e );
			throw new AppException( "Erro ao excluir o preço do produto.", e );
		}

	}
	
	@Override
	public boolean hasProposalDetailRelationship(Integer pprId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT ppr_id FROM proposal_detail_vehicle WHERE ppr_id = :pprId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "pprId", pprId );

			log.trace( "[QUERY] priceProduct.hasProposalDetailRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com proposta." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com proposta.", e );
		}
	}
}
