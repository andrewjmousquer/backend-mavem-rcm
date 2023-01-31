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
import com.portal.dao.IPriceItemModelDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PriceItemModelMapper;
import com.portal.model.PriceItemModel;
import com.portal.model.ProductModel;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PriceItemModelDAO extends BaseDAO implements IPriceItemModelDAO {

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceItemModel> list() throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceItemModel> search(PriceItemModel model) throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}
	
	@Override
	public List<PriceItemModel> find( PriceItemModel model, Pageable pageable ) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pim_id");	
			}
			
			Order order = Order.asc( "pim_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT imp.* ");
			query.append("FROM price_item_model imp ");
			query.append("INNER JOIN price_list prl ON prl.prl_id = imp.prl_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND imp.pim_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
				
				if( model.getPriceList() != null && model.getPriceList().getId() != null) {
					query.append(" AND imp.prl_id = :prlId ");
					params.addValue("prlId",model.getPriceList().getId());
					hasFilter = true;
				}
				
				if( model.getAllBrands() != null ) {
					query.append(" AND imp.all_brands = :allBrands ");
					params.addValue("allBrands", PortalNumberUtils.booleanToInt( model.getAllBrands() ) );
					hasFilter = true;
				}
				
				if( model.getAllModels() != null ) {
					query.append(" AND imp.all_models = :allModels ");
					params.addValue("allModels", PortalNumberUtils.booleanToInt( model.getAllModels() ) );
					hasFilter = true;
				}
				
				if( model.getItem() != null && model.getItem().getId() != null) {
					query.append(" AND imp.itm_id = :itmId ");
					params.addValue("imdId",model.getItem().getId());
					hasFilter = true;
				}
				
				if( model.getItemModel() != null && model.getItemModel().getId() != null) {
					query.append(" AND imp.imd_id = :imdId ");
					params.addValue("imdId",model.getItemModel().getId());
					hasFilter = true;
				}
				
				if( model.getBrand() != null && model.getBrand().getId() != null) {
					query.append(" AND imp.brd_id = :brdId ");
					params.addValue("brdId",model.getBrand().getId());
					hasFilter = true;
				}
			}
			
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] priceItemModel.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceItemModelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar o preço dos itens por modelo/fabricante.", e );
			throw new AppException( "Erro ao buscar o preço dos itens por modelo/fabricante.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(PriceItemModel, Pageable)}
	 */
	@Override
	public Optional<PriceItemModel> find(PriceItemModel model) throws AppException {
		List<PriceItemModel> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public Optional<PriceItemModel> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM price_item_model imp " +
							"INNER JOIN price_list prl ON prl.prl_id = imp.prl_id " +
							"WHERE imp.pim_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] priceItemModel.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PriceItemModelMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o preço do modelo do item.", e );
			throw new AppException( "Erro ao consultar o preço do modelo do item.", e );
		}
	}

	@Override
	public Optional<PriceItemModel> save(PriceItemModel model) throws AppException {
		try {
			String query = "INSERT INTO price_item_model ( pim_id, price, all_models, all_brands, prl_id, itm_id, imd_id, brd_id ) " +
					       "VALUES ( NULL, :price, :allModels, :allBrands, :prlId, :itmId, :imdId, :brdId ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("price", model.getPrice() );
			params.addValue("allModels", PortalNumberUtils.booleanToInt( model.getAllModels() ) );
			params.addValue("allBrands", PortalNumberUtils.booleanToInt( model.getAllBrands() ) );
			params.addValue("prlId", ( model.getPriceList() == null ? null : model.getPriceList().getId() ) );
			params.addValue("itmId", ( model.getItem() == null ? null : model.getItem().getId() ) );
			params.addValue("imdId", ( model.getItemModel() == null ? null : model.getItemModel().getId() ) );
			params.addValue("brdId", ( model.getBrand() == null ? null : model.getBrand().getId() ) );
	
			log.trace( "[QUERY] priceItemModel.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o preço do modelo do item: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o preço do modelo do item.", e);
		}
	}

	@Override
	public Optional<PriceItemModel> update(PriceItemModel model) throws AppException {
		try {
			String query = "UPDATE price_item_model SET price=:price, all_models=:allModels, all_brands=:allBrands, " +
					 	   							   "prl_id=:prlId, itm_id = :itmId, imd_id=:imdId, brd_id=:brdId " +
					 	   "WHERE pim_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId() );
			params.addValue("price", model.getPrice() );
			params.addValue("allModels", PortalNumberUtils.booleanToInt( model.getAllModels() ) );
			params.addValue("allBrands", PortalNumberUtils.booleanToInt( model.getAllBrands() ) );
			params.addValue("prlId", ( model.getPriceList() == null ? null : model.getPriceList().getId() ) );
			params.addValue("itmId", ( model.getItem() == null ? null : model.getItem().getId() ) );
			params.addValue("imdId", ( model.getItemModel() == null ? null : model.getItemModel().getId() ) );
			params.addValue("brdId", ( model.getBrand() == null ? null : model.getBrand().getId() ) );
	
			log.trace( "[QUERY] priceItemModel.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o preço do modelo do item: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o preço do modelo do item.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM price_item_model WHERE pim_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] priceItemModel.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o preço do modelo do item" , e );
			throw new AppException( "Erro ao excluir o preço do modelo do item.", e );
		}

	}
	
	@Override
	public boolean hasProposalDetailRelationship(Integer ipcId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT pim_id FROM proposal_detail_vehicle_item WHERE pim_id = :pimId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "pimId", ipcId );

			log.trace( "[QUERY] priceItemModel.hasProposalDetailRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com proposta." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com proposta.", e );
		}
	}
}
