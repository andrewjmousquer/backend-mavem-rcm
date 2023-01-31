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
import com.portal.dao.IItemModelDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ItemModelMapper;
import com.portal.model.ItemModel;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ItemModelDAO extends BaseDAO implements IItemModelDAO {

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ItemModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ItemModel> list() throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ItemModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ItemModel> search(ItemModel model) throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}
	
	/**
	 * @deprecated Usar a função {@link #find(ItemModel, Pageable)}
	 */
	@Override
	public Optional<ItemModel> find(ItemModel model) throws AppException {
		List<ItemModel> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}


	@Override
	public List<ItemModel> find( ItemModel model, Pageable pageable ) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "imd_id");	
			}
			
			Order order = Order.asc( "imd_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT imd.* ");
			query.append("FROM item_model imd ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND imd.imd_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
				
				if( model.getModelYearStart() != null ) {
					query.append(" AND imd.model_year_start = :startYear ");
					params.addValue("startYear", model.getModelYearStart());
					hasFilter = true;
				}
				
				if( model.getModelYearEnd() != null ) {
					query.append(" AND imd.model_year_end = :endYear ");
					params.addValue("endYear", model.getModelYearEnd());
					hasFilter = true;
				}
				
				if( model.getModel() != null && model.getModel().getId() != null ) {
					query.append(" AND imd.mdl_id = :mdlId ");
					params.addValue("mdlId", model.getModel().getId());
					hasFilter = true;
				}
				
				if( model.getItem() != null && model.getItem().getId() != null ) {
					query.append(" AND imd.itm_id = :itmId ");
					params.addValue("itmId", model.getItem().getId());
					hasFilter = true;
				}
			}
			
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] itemModel.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ItemModelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento de modelo e item.", e );
			throw new AppException( "Erro ao buscar o relacionamento de modelo e item.", e );
		}
	}

	@Override
	public Optional<ItemModel> getById(Integer id) throws AppException {
		try {
			
			StringBuilder query = new StringBuilder(); 
			query.append("SELECT imd.* ");
			query.append("FROM item_model imd ");
			query.append("WHERE imd.imd_id = :id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] itemModel.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ItemModelMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o relacionamento de item e modelo.", e );
			throw new AppException( "Erro ao consultar o relacionamento de item e modelo.", e );
		}
	}

	@Override
	public Optional<ItemModel> save(ItemModel model) throws AppException {
		try {
			
			String query = "INSERT INTO item_model (imd_id, model_year_start, model_year_end, itm_id, mdl_id) " +
					 	   "VALUES (NULL, :startYear, :endYear, :itmId, :mdlId);";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("startYear", model.getModelYearStart());
			params.addValue("endYear", model.getModelYearEnd());
			params.addValue("mdlId", ( model.getModel() != null ? model.getModel().getId() : null ) );
			params.addValue("itmId", ( model.getItem() != null ? model.getItem().getId() : null ) );
	
			log.trace( "[QUERY] itemModel.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o relacionamento de item e modelo: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de item e modelo.", e);
		}
	}

	@Override
	public Optional<ItemModel> update(ItemModel model) throws AppException {
		try {
			String query = "UPDATE item_model SET model_year_start=:startYear, model_year_end=:endYear, itm_id=:itmId, mdl_id=:mdlId " +
					 	   "WHERE imd_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			params.addValue("id", model.getId() );
			params.addValue("startYear", model.getModelYearStart());
			params.addValue("endYear", model.getModelYearEnd());
			params.addValue("mdlId", ( model.getModel() != null ? model.getModel().getId() : null ) );
			params.addValue("itmId", ( model.getItem() != null ? model.getItem().getId() : null ) );
	
			log.trace( "[QUERY] itemModel.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o relacionamento de item e modelo: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o relacionamento de item e modelo.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM item_model WHERE imd_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] itemModel.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o relacionamento de item e modelo" , e );
			throw new AppException( "Erro ao excluir o relacionamento de item e modelo.", e );
		}

	}
	
	@Override
	public boolean hasPriceListRelationship(Integer prmId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT imd_id FROM price_item_model imp WHERE imd_id = :prmId OR all_models = 1 OR all_brands = 1 LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "prmId", prmId );

			log.trace( "[QUERY] itemModel.hasPriceListRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com a lista de preço." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com a lista de preço.", e );
		}
	}

	@Override
	public List<ItemModel> findDuplicated( ItemModel model ) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT imd.* ");
			query.append("FROM item_model imd ");
			query.append("WHERE ( " );
			query.append("			( model_year_start BETWEEN :startYear AND :endYear OR model_year_end BETWEEN :startYear AND :endYear ) ");
			query.append("			OR ");
			query.append("			( model_year_start <= :startYear AND model_year_end >= :endYear ) ");
			query.append("		) ");
			query.append("AND ");
			query.append("	( itm_id = :itmId AND mdl_id = :mdlId  ) ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("startYear", model.getModelYearStart());
			params.addValue("endYear", model.getModelYearEnd());
			params.addValue("mdlId", ( model.getModel() != null ? model.getModel().getId() : null ));
			params.addValue("itmId", ( model.getItem() != null ? model.getItem().getId() : null ));
			
			log.trace( "[QUERY] itemModel.findDuplicated: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ItemModelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar registros duplicados.", e );
			throw new AppException( "Erro ao buscar registros duplicados.", e );
		}
	}
}
