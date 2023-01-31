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
import com.portal.dao.IProductModelDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProductModelMapper;
import com.portal.model.ProductModel;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProductModelDAO extends BaseDAO implements IProductModelDAO {

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ProductModel> list() throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<ProductModel> search(ProductModel model) throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

	/**
	 * @deprecated Usar a função {@link #find(ProductModel, Pageable)}
	 */
	@Override
	public Optional<ProductModel> find(ProductModel model) throws AppException {
		List<ProductModel> models = this.find(model, null);
		return Optional.ofNullable((models != null ? models.get(0) : null));
	}

	@Override
	public List<ProductModel> listAll(Pageable pageable) throws AppException {
		try {

			if (pageable == null) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "prm_id");
			}

			Order order = Order.desc("prm_id");
			if (pageable.getSort().get().findFirst().isPresent()) {
				order = pageable.getSort().get().findFirst().orElse(order);
			}

			String query = "SELECT * " +
					"FROM product_model prm " +
					"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
					"LIMIT " + pageable.getPageSize() + " " +
					"OFFSET " + pageable.getPageNumber();

			log.trace("[QUERY] product_model.listAll: {} [PARAMS]: {}", query);

			return this.getJdbcTemplatePortal().query(query, new ProductModelMapper());

		} catch (Exception e) {
			log.error("Erro ao listar os produtos modelos.", e);
			throw new AppException("Erro ao listar os produtos modelos.", e);
		}
	}

	@Override
	public List<ProductModel> getByProduct(Integer id) throws AppException {

		try {
			Pageable pageable = null;
			if (pageable == null) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "prm_id");
			}

			Order order = Order.desc("prm_id");
			if (pageable.getSort().get().findFirst().isPresent()) {
				order = pageable.getSort().get().findFirst().orElse(order);
			}

			StringBuilder query = new StringBuilder();

			query.append("SELECT prm.prm_id, prm.has_project, prm.model_year_start, prm.model_year_end, prm.manufacture_days, ");
			query.append(" prd.prd_id, prd.name, prd.active, prd.proposal_expiration_days, " );
			query.append(" mdl.mdl_id, mdl.name AS model_name, mdl.active AS model_active, mdl.brd_id, mdl.body_type_cla_id, mdl.category_cla_id, mdl.type_cla_id, " );
			query.append(" brd.brd_id, brd.name AS brand_name, brd.active AS brand_active " );
			query.append("FROM product_model prm ");
			query.append("INNER JOIN product prd ON prm.prd_id = prd.prd_id ");
			query.append("INNER JOIN model mdl  ON prm.mdl_id = mdl.mdl_id ");
			query.append("INNER JOIN brand brd  ON mdl.brd_id = brd.brd_id ");
			query.append("WHERE prm.prd_id = " + id);
			query.append(" ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " ");

			log.trace("[QUERY] product_model.listAll: {} [PARAMS]: {}", query);

			return this.getJdbcTemplatePortal().query(query.toString(), new ProductModelMapper());

		} catch (Exception e) {
			log.error("Erro ao buscar ", e);
			throw new AppException("Erro ao buscar ", e);
		}
	}


	@Override
	public List<ProductModel> find(ProductModel model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;

			if (pageable == null) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString("DESC"), "prm_id");
			}

			Order order = Order.asc("prm_id");
			if (pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent()) {
				order = pageable.getSort().get().findFirst().orElse(order);
			}

			StringBuilder query = new StringBuilder();

			query.append("SELECT prm.prm_id, prm.has_project, prm.model_year_start, prm.model_year_end, prm.manufacture_days, ");
			query.append(" prd.prd_id, prd.name, prd.active, prd.proposal_expiration_days, " );
			query.append(" mdl.mdl_id, mdl.name AS model_name, mdl.active AS model_active, mdl.brd_id, mdl.body_type_cla_id, mdl.category_cla_id, mdl.type_cla_id, " );
			query.append(" brd.brd_id, brd.name AS brand_name, brd.active AS brand_active " );
			query.append("FROM product_model prm ");
			query.append("INNER JOIN product prd ON prm.prd_id = prd.prd_id ");
			query.append("INNER JOIN model mdl  ON prm.mdl_id = mdl.mdl_id ");
			query.append("INNER JOIN brand brd  ON mdl.brd_id = brd.brd_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND prm.prm_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
				
				if( model.getHasProject() != null ) {
					query.append(" AND prm.has_project = :hasProject ");
					params.addValue("hasProject", model.getHasProject());
					hasFilter = true;
				}
				
				if( model.getModelYearStart() != null ) {
					query.append(" AND prm.model_year_start = :startYear ");
					params.addValue("startYear", model.getModelYearStart());
					hasFilter = true;
				}
				
				if( model.getModelYearEnd() != null ) {
					query.append(" AND prm.model_year_end = :endYear ");
					params.addValue("endYear", model.getModelYearEnd());
					hasFilter = true;
				}
				
				if( model.getManufactureDays() != null ) {
					query.append(" AND prm.manufacture_days = :manufacturerDays ");
					params.addValue("manufacturerDays", model.getManufactureDays());
					hasFilter = true;
				}
				
				if( model.getModel() != null && model.getModel().getId() != null ) {
					query.append(" AND prm.mdl_id = :mdlId ");
					params.addValue("mdlId", model.getModel().getId());
					hasFilter = true;
				}
				
				if( model.getProduct() != null && model.getProduct().getId() != null ) {
					query.append(" AND prm.prd_id = :prdId ");
					params.addValue("prdId", model.getProduct().getId());
					hasFilter = true;
				}
			}
			
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );

			log.trace("[QUERY] productModel.find: {} [PARAMS]: {}", query, params.getValues());

			return this.getJdbcTemplatePortal().query(query.toString(), params, new ProductModelMapper());

		} catch (Exception e) {
			log.error("Erro ao buscar o relacionamento de modelo e produto.", e);
			throw new AppException("Erro ao buscar o relacionamento de modelo e produto.", e);
		}
	}


	@Override
	public Optional<ProductModel> getById(Integer id) throws AppException {
		try {

			StringBuilder query = new StringBuilder();
			query.append("SELECT prm.prm_id, prm.has_project, prm.model_year_start, prm.model_year_end, prm.manufacture_days, ");
			query.append(" prd.prd_id, prd.name, prd.active, prd.proposal_expiration_days, " );
			query.append(" mdl.mdl_id, mdl.name AS model_name, mdl.active AS model_active, mdl.brd_id, mdl.body_type_cla_id, mdl.category_cla_id, mdl.type_cla_id, " );
			query.append(" brd.brd_id, brd.name AS brand_name, brd.active AS brand_active " );
			query.append("FROM product_model prm ");
			query.append("INNER JOIN product prd ON prd.prd_id = prm.prd_id ");
			query.append("INNER JOIN model mdl ON mdl.mdl_id = prm.mdl_id ");
			query.append("INNER JOIN brand brd  ON mdl.brd_id = brd.brd_id ");
			query.append("WHERE prm.prm_id = :id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] productModel.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProductModelMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o relacionamento de produto e modelo.", e );
			throw new AppException( "Erro ao consultar o relacionamento de produto e modelo.", e );
		}
	}

	@Override
	public Optional<ProductModel> save(ProductModel model) throws AppException {
		try {
			
			String query = "INSERT INTO product_model (prm_id, has_project, model_year_start, model_year_end, manufacture_days, mdl_id, prd_id) " +
					 	   "VALUES (NULL, :hasProject, :startYear, :endYear, :manufacturerDays, :mdlId, :prdId);";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			params.addValue("hasProject", model.getHasProject());
			params.addValue("startYear", model.getModelYearStart());
			params.addValue("endYear", model.getModelYearEnd());
			params.addValue("manufacturerDays", model.getManufactureDays());
			params.addValue("mdlId", ( model.getModel() != null ? model.getModel().getId() : null ) );
			params.addValue("prdId", ( model.getProduct() != null ? model.getProduct().getId() : null ) );
	
			log.trace( "[QUERY] productModel.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o relacionamento de produto e modelo: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de produto e modelo.", e);
		}
	}

	@Override
	public Optional<ProductModel> update(ProductModel model) throws AppException {
		try {
			String query = "UPDATE product_model SET has_project=:hasProject, model_year_start=:startYear, model_year_end=:endYear, manufacture_days=:manufacturerDays, prd_id=:prdId, mdl_id=:mdlId " +
					 	   "WHERE prm_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			params.addValue("id", model.getId() );
			params.addValue("hasProject", model.getHasProject());
			params.addValue("startYear", model.getModelYearStart());
			params.addValue("endYear", model.getModelYearEnd());
			params.addValue("manufacturerDays", model.getManufactureDays());
			params.addValue("mdlId", ( model.getModel() != null ? model.getModel().getId() : null ) );
			params.addValue("prdId", ( model.getProduct() != null ? model.getProduct().getId() : null ) );
	
			log.trace( "[QUERY] productModel.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o relacionamento de produto e modelo: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o relacionamento de produto e modelo.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM product_model WHERE prm_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] productModel.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o relacionamento de produto e modelo" , e );
			throw new AppException( "Erro ao excluir o relacionamento de produto e modelo.", e );
		}

	}
	
	@Override
	public boolean hasPriceListRelationship(Integer prmId) throws AppException {
		try {
			String query = "SELECT CASE WHEN EXISTS ( " +
					"SELECT prm_id FROM price_product WHERE prm_id = :prmId LIMIT 1 " +
					") " +
					"THEN TRUE " +
					"ELSE FALSE " +
					"END AS `exists` ";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("prmId", prmId);

			log.trace("[QUERY] productModel.hasPriceListRelationship: {} [PARAMS]: {}", query, params.getValues());

			return this.getJdbcTemplatePortal().queryForObject(query, params, (rs, rowNum) -> rs.getBoolean("exists"));

		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com a lista de preço." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com a lista de preço.", e );
		}
	}

	@Override
	public List<ProductModel> findDuplicated( ProductModel model ) throws AppException {
		try {
			StringBuilder query = new StringBuilder();

			query.append("SELECT prm.prm_id, prm.has_project, prm.model_year_start, prm.model_year_end, prm.manufacture_days, ");
			query.append(" prd.prd_id, prd.name, prd.active, prd.proposal_expiration_days, " );
			query.append(" mdl.mdl_id, mdl.name AS model_name, mdl.active AS model_active, mdl.brd_id, mdl.body_type_cla_id, mdl.category_cla_id, mdl.type_cla_id, " );
			query.append(" brd.brd_id, brd.name AS brand_name, brd.active AS brand_active " );
			query.append("FROM product_model prm ");
			query.append("INNER JOIN product prd ON prm.prd_id = prd.prd_id ");
			query.append("INNER JOIN model mdl  ON prm.mdl_id = mdl.mdl_id ");
			query.append("INNER JOIN brand brd  ON mdl.brd_id = brd.brd_id ");
			query.append("WHERE ( " );
			query.append("			( prm.model_year_start BETWEEN :startYear AND :endYear OR prm.model_year_end BETWEEN :startYear AND :endYear ) ");
			query.append("			OR ");
			query.append("			( prm.model_year_start <= :startYear AND prm.model_year_end >= :endYear ) ");
			query.append("		) ");
			query.append("AND ");
			query.append("	( prm.prd_id = :prdId AND prm.mdl_id = :mdlId  ) ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("startYear", model.getModelYearStart());
			params.addValue("endYear", model.getModelYearEnd());
			params.addValue("mdlId", ( model.getModel() != null ? model.getModel().getId() : null ));
			params.addValue("prdId", ( model.getProduct() != null ? model.getProduct().getId() : null ));
			
			log.trace( "[QUERY] productModel.findDuplicated: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProductModelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar registros duplicados.", e );
			throw new AppException( "Erro ao buscar registros duplicados.", e );
		}
	}
}
