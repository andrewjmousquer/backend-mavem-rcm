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
import com.portal.dao.IModelItemCostDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ModelItemCostMapper;
import com.portal.model.ModelItemCost;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ModelItemCostDAO extends BaseDAO implements IModelItemCostDAO {

	@Override
	public List<ModelItemCost> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "start_date");	
			}
			
			Order order = Order.desc( "start_date" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT mic.*,  " +
							" itm.name as itm_name, " + 
							" itm.itm_id as itm_id, " + 
							" brd.name as brd_name, " + 
							" brd.brd_id as brd_id, " + 
							" imd.*, " + 
							" imd_itm.name as imd_itm_name, " + 
							" imd_itm.itm_id as imd_itm_id, " + 
							" imd_mdl.name as imd_mdl_name, " + 
							" imd_mdl.mdl_id as imd_mdl_id " + 
							"FROM model_item_cost mic " +
							"INNER JOIN item itm on itm.itm_id = mic.itm_id " +
							"LEFT JOIN brand brd on brd.brd_id = mic.brd_id " +
							"LEFT JOIN item_model imd on imd.imd_id = mic.imd_id " +
							"LEFT JOIN item imd_itm on imd_itm.itm_id = imd.itm_id " +
							"LEFT JOIN model imd_mdl on imd_mdl.mdl_id = imd.mdl_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] model_item_cost.listAll: {}", query);

			return this.getJdbcTemplatePortal().query( query, new ModelItemCostMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar custos por modelo e item.", e );
			throw new AppException( "Erro ao listar custos por modelo e item.", e );
		}
	}
	
	@Override
	public Optional<ModelItemCost> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT mic.*,  " +
					" itm.name as itm_name, " + 
					" itm.itm_id as itm_id, " + 
					" brd.name as brd_name, " + 
					" brd.brd_id as brd_id, " + 
					" imd.*, " + 
					" imd_itm.name as imd_itm_name, " + 
					" imd_itm.itm_id as imd_itm_id, " + 
					" imd_mdl.name as imd_mdl_name, " + 
					" imd_mdl.mdl_id as imd_mdl_id " + 
					"FROM model_item_cost mic " +
					"INNER JOIN item itm on itm.itm_id = mic.itm_id " +
					"LEFT JOIN brand brd on brd.brd_id = mic.brd_id " +
					"LEFT JOIN item_model imd on imd.imd_id = mic.imd_id " +
					"LEFT JOIN item imd_itm on imd_itm.itm_id = imd.itm_id " +
					"LEFT JOIN model imd_mdl on imd_mdl.mdl_id = imd.mdl_id " +
					"WHERE mic.mic_id = :id " +
					"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] model_item_cost.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ModelItemCostMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o custo por modelo e item.", e );
			throw new AppException( "Erro ao consultar o custo por modelo e item.", e );
		}
	}
	
	@Override
	public Optional<ModelItemCost> save(ModelItemCost model) throws AppException {
		try {
			String query = "INSERT INTO model_item_cost ( price, all_models, all_brands, imd_id, brd_id, itm_id, start_date, end_date ) VALUES ( :price, :allModels, :allBrands, :itemModelId, :brandId, :itemId, :startDate, :endDate ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "price", model.getPrice() );
			params.addValue( "allModels", PortalNumberUtils.booleanToInt( model.getAllModels() ) );
			params.addValue( "allBrands", PortalNumberUtils.booleanToInt( model.getAllBrands() ) );
			params.addValue( "itemModelId", model.getItemModel() != null ? model.getItemModel().getId() : null );
			params.addValue( "brandId", model.getBrand() != null ? model.getBrand().getId() : null );
			params.addValue( "itemId", model.getItem().getId() );
			params.addValue( "startDate", PortalTimeUtils.localDateTimeFormat( model.getStartDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "endDate", PortalTimeUtils.localDateTimeFormat( model.getEndDate(), "yyyy-MM-dd HH:mm:ss" ) );
	
			log.trace( "[QUERY] model_item_cost.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o Custo do Modelo e Item: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o Custo do Modelo e Item.", e);
		}
	}
	
	@Override
	public Optional<ModelItemCost> update(ModelItemCost model) throws AppException {
		try {
			String query = "UPDATE model_item_cost SET price = :price, all_models = :allModels, all_brands = :allBrands, "
					+ "imd_id = :itemModelId, brd_id = :brandId, itm_id = :itemId, start_date = :startDate, end_date = :endDate "
					+ "WHERE mic_id = :id ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "price", model.getPrice() );
			params.addValue( "allModels", PortalNumberUtils.booleanToInt( model.getAllModels() ) );
			params.addValue( "allBrands", PortalNumberUtils.booleanToInt( model.getAllBrands() ) );
			params.addValue( "itemModelId", model.getItemModel() != null ? model.getItemModel().getId() : null );
			params.addValue( "brandId", model.getBrand() != null ? model.getBrand().getId() : null );
			params.addValue( "itemId", model.getItem().getId() );
			params.addValue( "startDate", PortalTimeUtils.localDateTimeFormat( model.getStartDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "endDate", PortalTimeUtils.localDateTimeFormat( model.getEndDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "id", model.getId() );
	
			log.trace( "[QUERY] model_item_cost.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o Custo do Modelo e Item: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o Custo do Modelo e Item.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM model_item_cost WHERE mic_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] model_item_cost.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o Custo do Modelo e Item" , e );
			throw new AppException( "Erro ao excluir o Custo do Modelo e Item.", e );
		}
	}

	@Override
	public List<ModelItemCost> find(ModelItemCost model, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "start_date");	
			}
			
			Order order = Order.desc( "start_date" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT mic.*,  " +
							" itm.name as itm_name, " + 
							" itm.itm_id as itm_id, " + 
							" brd.name as brd_name, " + 
							" brd.brd_id as brd_id, " + 
							" imd.*, " + 
							" imd_itm.name as imd_itm_name, " + 
							" imd_itm.itm_id as imd_itm_id, " + 
							" imd_mdl.name as imd_mdl_name, " + 
							" imd_mdl.mdl_id as imd_mdl_id " + 
							"FROM model_item_cost mic " +
							"INNER JOIN item itm on itm.itm_id = mic.itm_id " +
							"LEFT JOIN brand brd on brd.brd_id = mic.brd_id " +
							"LEFT JOIN item_model imd on imd.imd_id = mic.imd_id " +
							"LEFT JOIN item imd_itm on imd_itm.itm_id = imd.itm_id " +
							"LEFT JOIN model imd_mdl on imd_mdl.mdl_id = imd.mdl_id " +
							"WHERE 1=1 ";
							if (model.getItem() != null && model.getItem().getId() != null) {
								query += "AND itm.itm_id = :idItem ";
							}
							if (model.getBrand() != null && model.getBrand().getId() != null) {
								query += "AND brd.brd_id = :idBrand ";
							}
							if (model.getItemModel() != null && model.getItemModel().getModel() != null 
									&& model.getItemModel().getModel().getId() != null) {
								query += "AND imd.mdl_id = :idModel ";
							}
							if (model.getDateFilter() != null) {
								query += "AND :date BETWEEN mic.start_date AND mic.end_date ";
							}
							
							query += "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if (model.getItem() != null && model.getItem().getId() != null) {
				params.addValue( "idItem", model.getItem().getId() );
			}
			if (model.getBrand() != null && model.getBrand().getId() != null) {
				params.addValue( "idBrand", model.getBrand().getId() );
			}
			if (model.getItemModel() != null && model.getItemModel().getModel() != null 
					&& model.getItemModel().getModel().getId() != null) {
				params.addValue( "idModel", model.getItemModel().getModel().getId() );
			}
			if (model.getDateFilter() != null) {
				params.addValue( "date", PortalTimeUtils.localDateTimeFormat( model.getDateFilter(), "yyyy-MM-dd HH:mm:ss" ) );
			}

			log.trace( "[QUERY] model_item_cost.find: {}", query);

			return this.getJdbcTemplatePortal().query( query, params, new ModelItemCostMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar custos por modelo e item filtrado.", e );
			throw new AppException( "Erro ao listar custos por modelo e item filtrado.", e );
		}
		
	}
	
	public boolean hasDuplicate(ModelItemCost model) throws AppException {
		try {
			
			String query = 	"SELECT count(*)" + 
							"FROM model_item_cost mic " +
							"INNER JOIN item itm on itm.itm_id = mic.itm_id " +
							"LEFT JOIN brand brd on brd.brd_id = mic.brd_id " +
							"LEFT JOIN item_model imd on imd.imd_id = mic.imd_id " +
							"LEFT JOIN item imd_itm on imd_itm.itm_id = imd.itm_id " +
							"LEFT JOIN model imd_mdl on imd_mdl.mdl_id = imd.mdl_id " +
							"WHERE " +
							" itm.itm_id = :idItem ";
							if (model.getId() != null) {
								query += "AND mic.mic_id != :id ";
							}
							if (model.getBrand() != null && model.getBrand().getId() != null) {
								query += "AND brd.brd_id = :idBrand ";
							} else {
								query += "AND mic.all_brands IS TRUE ";
							}
							if (model.getItemModel() != null && model.getItemModel().getId() != null) {
								query += "AND imd.imd_id = :idItemModel ";
							} else {
								query += "AND mic.all_models IS TRUE ";
							}
							query +=  "AND (mic.start_date BETWEEN :startDate AND :endDate OR mic.end_date BETWEEN :startDate AND :endDate) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "idItem", model.getItem().getId() );
			if (model.getId() != null) {
				params.addValue( "id", model.getId() );
			}
			if (model.getBrand() != null && model.getBrand().getId() != null) {
				params.addValue( "idBrand", model.getBrand().getId() );
			}
			if (model.getItemModel() != null && model.getItemModel().getId() != null) {
				params.addValue( "idItemModel", model.getItemModel().getId() );
			}
			params.addValue( "startDate", PortalTimeUtils.localDateTimeFormat( model.getStartDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "endDate", PortalTimeUtils.localDateTimeFormat( model.getEndDate(), "yyyy-MM-dd HH:mm:ss" ) );

			log.trace( "[QUERY] model_item_cost.hasDuplicate: {}", query);

			Integer duplicated = this.getJdbcTemplatePortal().queryForObject( query, params, Integer.class );
			return duplicated.intValue() > 0;
			
		} catch (Exception e) {
			log.error( "Erro ao verificar se existe Custo por Item e Modelo duplicado.", e );
			throw new AppException( "Erro ao verificar se existe Custo por Item e Modelo duplicado.", e );
		}
		
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ModelItemCost, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ModelItemCost> list() throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ModelItemCost, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public Optional<ModelItemCost> find(ModelItemCost model) throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find com paginação" );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ModelItemCost, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ModelItemCost> search(ModelItemCost model) throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

}
