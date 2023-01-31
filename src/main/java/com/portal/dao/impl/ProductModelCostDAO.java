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
import com.portal.dao.IProductModelCostDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProductModelCostMapper;
import com.portal.model.ProductModelCost;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProductModelCostDAO extends BaseDAO implements IProductModelCostDAO {

	@Override
	public Optional<ProductModelCost> getById(Integer id) throws AppException {

		try {

			StringBuilder query = new StringBuilder();
			query.append("SELECT ");
			query.append("	pmc.pmc_id, pmc.prm_id, pmc.start_date, pmc.end_date, pmc.total_value, ");
			query.append("	prm.prm_id, prm.has_project, prm.model_year_start, prm.model_year_end, prm.manufacture_days, " );
			query.append("	prd.prd_id, prd.name, prd.active, prd.proposal_expiration_days, " );
			query.append("	mdl.mdl_id, mdl.name AS model_name, mdl.active AS model_active, mdl.brd_id, mdl.body_type_cla_id, mdl.category_cla_id, mdl.type_cla_id, " );
			query.append("	brd.brd_id, brd.name AS brand_name, brd.active AS brand_active " );
			query.append("FROM product_model_cost pmc " );
			query.append("	INNER JOIN product_model prm ON pmc.prm_id = prm.prm_id " );
			query.append("	INNER JOIN product prd ON prm.prd_id = prd.prd_id " );
			query.append("	INNER JOIN model mdl  ON prm.mdl_id = mdl.mdl_id " );
			query.append("	INNER JOIN brand brd  ON mdl.brd_id = brd.brd_id " );
			query.append("WHERE pmc.pmc_id = :id ");

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] productModelCost.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProductModelCostMapper() ) );

		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();

		} catch (Exception e) {
			log.error( "Erro ao consultar o relacionamento de produto e modelo.", e );
			throw new AppException( "Erro ao consultar o relacionamento de produto e modelo.", e );
		}
	}

	@Override
	public List<ProductModelCost> list() throws AppException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProductModelCost> listAll(Pageable pageable) throws AppException {

		try {

			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pmc_id");	
			}

			Order order = Order.desc( "pmc_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}

			String query = 	"SELECT  " +
							"pmc.pmc_id, pmc.prm_id, pmc.start_date, pmc.end_date, pmc.total_value, " +
							"prm.prm_id, prm.has_project, prm.model_year_start, prm.model_year_end, prm.manufacture_days, " +
							"prd.prd_id, prd.name, prd.active, prd.proposal_expiration_days, " +
							"mdl.mdl_id, mdl.name AS model_name, mdl.active AS model_active, mdl.brd_id, mdl.body_type_cla_id, mdl.category_cla_id, mdl.type_cla_id, " +
							"brd.brd_id, brd.name AS brand_name, brd.active AS brand_active " +
							"FROM product_model_cost pmc " +
							"INNER JOIN product_model prm ON pmc.prm_id = prm.prm_id " +
							"INNER JOIN product prd ON prm.prd_id = prd.prd_id " +
							"INNER JOIN model mdl  ON prm.mdl_id = mdl.mdl_id " +
							"INNER JOIN brand brd  ON mdl.brd_id = brd.brd_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] product_model_cost.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ProductModelCostMapper() );

		} catch (Exception e) {
			log.error( "Erro ao listar os produtos.", e );
			throw new AppException( "Erro ao listar os produtos.", e );
		}
	}

	@Override
	public Optional<ProductModelCost> find(ProductModelCost model) throws AppException {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public List<ProductModelCost> find(ProductModelCost model, Pageable pageable) throws AppException {

		try {
			boolean hasFilter = false;

			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pmc.pmc_id");	
			}

			Order order = Order.asc( "pmc.pmc_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}

			StringBuilder query = new StringBuilder();

			query.append("SELECT ");
			query.append("	pmc.pmc_id, pmc.prm_id, pmc.start_date, pmc.end_date, pmc.total_value, ");
			query.append("	prm.prm_id, prm.has_project, prm.model_year_start, prm.model_year_end, prm.manufacture_days, ");
			query.append("	prd.prd_id, prd.name, prd.active, prd.proposal_expiration_days, ");
			query.append("	mdl.mdl_id, mdl.name AS model_name, mdl.active AS model_active, mdl.brd_id, mdl.body_type_cla_id, mdl.category_cla_id, mdl.type_cla_id, ");
			query.append("	brd.brd_id, brd.name AS brand_name, brd.active AS brand_active ");
			query.append("FROM product_model_cost pmc ");
			query.append("	INNER JOIN product_model prm ON pmc.prm_id = prm.prm_id ");
			query.append("	INNER JOIN product prd ON prm.prd_id = prd.prd_id ");
			query.append("	INNER JOIN model mdl ON prm.mdl_id = mdl.mdl_id ");
			query.append("	INNER JOIN brand brd ON mdl.brd_id = brd.brd_id ");
			query.append("WHERE :hasFilter ");

			MapSqlParameterSource params = new MapSqlParameterSource();

			if(model != null) {

				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND pmc.pmc_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}

				if( model.getStartDate() != null) {
					query.append(" AND pmc.start_date >= :startDate ");
					params.addValue("startDate", model.getStartDate());
					hasFilter = true;
				}

				if( model.getEndDate() != null) {
					query.append(" AND pmc.end_date <= :endDate ");
					params.addValue("endDate", model.getEndDate());
					hasFilter = true;
				}

				if( model.getProductModel().getHasProject() != null ) {
					query.append(" AND prm.has_project = :hasProject ");
					params.addValue("hasProject", model.getProductModel().getHasProject());
					hasFilter = true;
				}

				if( model.getProductModel().getModelYearStart() != null ) {
					query.append(" AND prm.model_year_start = :startYear ");
					params.addValue("startYear", model.getProductModel().getModelYearStart());
					hasFilter = true;
				}

				if( model.getProductModel().getModelYearEnd() != null ) {
					query.append(" AND prm.model_year_end = :endYear ");
					params.addValue("endYear", model.getProductModel().getModelYearEnd());
					hasFilter = true;
				}

				if( model.getProductModel().getManufactureDays() != null ) {
					query.append(" AND prm.manufacture_days = :manufacturerDays ");
					params.addValue("manufacturerDays", model.getProductModel().getManufactureDays());
					hasFilter = true;
				}

				if( model.getProductModel().getModel() != null && model.getProductModel().getModel().getId() != null ) {
					query.append(" AND prm.mdl_id = :mdlId ");
					params.addValue("mdlId", model.getProductModel().getModel().getId());
					hasFilter = true;
				}

				if( model.getProductModel().getModel().getBrand() != null && model.getProductModel().getModel().getBrand().getId() != null ) {
					query.append(" AND brd.brd_id = :brdId ");
					params.addValue("brdId", model.getProductModel().getModel().getBrand().getId());
					hasFilter = true;
				}

				if( model.getProductModel().getProduct() != null && model.getProductModel().getProduct().getId() != null ) {
					query.append(" AND prm.prd_id = :prdId ");
					params.addValue("prdId", model.getProductModel().getProduct().getId());
					hasFilter = true;
				}
			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );

			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );

			log.trace( "[QUERY] product.find: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProductModelCostMapper() );

		} catch (Exception e) {
			log.error( "Erro ao buscar os produtos.", e );
			throw new AppException( "Erro ao buscar os produtos.", e );
		}
	}

	@Override
	public List<ProductModelCost> findDuplicate( ProductModelCost model ) throws AppException {
		try {
			StringBuilder query = new StringBuilder();

			query.append("SELECT ");
			query.append("	pmc.pmc_id, pmc.prm_id, pmc.start_date, pmc.end_date, pmc.total_value, ");
			query.append("	prm.prm_id, prm.has_project, prm.model_year_start, prm.model_year_end, prm.manufacture_days, ");
			query.append("	prd.prd_id, prd.name, prd.active, prd.proposal_expiration_days, ");
			query.append("	mdl.mdl_id, mdl.name AS model_name, mdl.active AS model_active, mdl.brd_id, mdl.body_type_cla_id, mdl.category_cla_id, mdl.type_cla_id, ");
			query.append("	brd.brd_id, brd.name AS brand_name, brd.active AS brand_active ");
			query.append("FROM product_model_cost pmc ");
			query.append("	INNER JOIN product_model prm ON pmc.prm_id = prm.prm_id ");
			query.append("	INNER JOIN product prd ON prm.prd_id = prd.prd_id ");
			query.append("	INNER JOIN model mdl ON prm.mdl_id = mdl.mdl_id ");
			query.append("	INNER JOIN brand brd ON mdl.brd_id = brd.brd_id ");
			query.append("WHERE ( " );
			query.append("			( prm.model_year_start BETWEEN :startYear AND :endYear OR prm.model_year_end BETWEEN :startYear AND :endYear ) ");
			query.append("			OR ");
			query.append("			( prm.model_year_start <= :startYear AND prm.model_year_end >= :endYear ) ");
			query.append("		) ");
			query.append("AND ");
			query.append("			( pmc.start_date BETWEEN :startDate AND :endDate OR pmc.end_date BETWEEN :startDate AND :endDate ) ");
			query.append("			OR ");
			query.append("			( pmc.start_date <= :startDate AND pmc.end_date >= :endDate ) ");
			query.append("AND ");
			query.append("	( prm.prd_id = :prdId AND prm.mdl_id = :mdlId  ) ");

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("startYear", model.getProductModel().getModelYearStart());
			params.addValue("endYear", model.getProductModel().getModelYearEnd());
			params.addValue("startDate", model.getStartDate());
			params.addValue("endDate", model.getEndDate());
			params.addValue("mdlId", ( model.getProductModel().getModel() != null ? model.getProductModel().getModel().getId() : null ));
			params.addValue("prdId", ( model.getProductModel().getProduct() != null ? model.getProductModel().getProduct().getId() : null ));

			log.trace( "[QUERY] productModelCost.findDuplicated: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProductModelCostMapper() );

		} catch (Exception e) {
			log.error( "Erro ao buscar registros duplicados.", e );
			throw new AppException( "Erro ao buscar registros duplicados.", e );
		}
	}

	@Override
	public List<ProductModelCost> search(ProductModelCost model) throws AppException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProductModelCost> search(ProductModelCost model, Pageable pageable) throws AppException {
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

				/*
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
				*/
			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );

			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );

			log.trace( "[QUERY] product.search: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProductModelCostMapper() );

		} catch (Exception e) {
			log.error( "Erro ao procurar os produtos.", e );
			throw new AppException( "Erro ao procurar os produtos.", e );
		}
	}

	@Override
	public Optional<ProductModelCost> save(ProductModelCost model) throws AppException {

		try {
			String query = "INSERT INTO product_model_cost ( prm_id, start_date, end_date, total_value ) VALUES ( :prm_id, :start_date, :end_date, :total_value ) ";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "prm_id", model.getProductModel().getId() );
			params.addValue( "start_date", model.getStartDate() );
			params.addValue( "end_date", model.getEndDate() );
			params.addValue( "total_value", model.getTotalValue() );

			log.trace( "[QUERY] product.save: {} [PARAMS]: {}", query, params.getValues() );

			KeyHolder keyHolder = new GeneratedKeyHolder();

	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);

	        model.setId( this.getKey(keyHolder) );

	        return Optional.ofNullable( model );

		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o produto: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o Custo por Produto e Modelo.", e);
		}
	}

	@Override
	public Optional<ProductModelCost> update(ProductModelCost model) throws AppException {

		try {
			String query = "UPDATE product_model_cost "
					+ "SET prm_id=:prm_id, start_date=:start_date, end_date=:end_date, total_value=:total_value "
					+ "WHERE pmc_id = :id";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "prm_id", model.getProductModel().getId() );
			params.addValue( "start_date", model.getStartDate() );
			params.addValue( "end_date", model.getEndDate());
			params.addValue( "total_value", model.getTotalValue());

			log.trace( "[QUERY] product_model_cost.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);

	        return Optional.ofNullable(model);

		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o produto: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o Custo por Produto e Modelo.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {

		try {
			String query = 	"DELETE FROM product_model_cost WHERE pmc_id = :id";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] product_model_cost.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);

		} catch (Exception e) {
			log.error( "Erro ao excluir o produto" , e );
			throw new AppException( "Erro ao excluir o Custo por Produto e Modelo.", e );
		}
	}
}