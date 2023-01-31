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
import com.portal.dao.IModelDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ModelMapper;
import com.portal.model.Model;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ModelDAO extends BaseDAO implements IModelDAO {

	@Override
	public List<Model> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mdl_id");	
			}
			
			Order order = Order.desc( "mdl_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT mdl.mdl_id, " +
							"		mdl.name, " + 
							"		mdl.active, " + 
							"		mdl.body_type_cla_id, " + 
							"		mdl.type_cla_id, " +
							"		mdl.category_cla_id, " + 
							"		mdl.cod_fipe, " +
							"		brd.brd_id, " + 
							"		brd.name AS brand_name, " + 
							"		brd.active AS brand_active " +
							"FROM model mdl " +
							"INNER JOIN brand brd ON brd.brd_id = mdl.brd_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " ;

			log.trace( "[QUERY] model.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ModelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os modelos.", e );
			throw new AppException( "Erro ao listar os modelos.", e );
		}
	}
	@Override
	public List<Model> listAllByBrand(int id, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mdl_id");
			}

			Order order = Order.desc( "mdl_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}

			String query = 	"SELECT mdl.mdl_id, " +
									"mdl.name, " +
									"mdl.active, " +
									"mdl.body_type_cla_id, " +
									"mdl.type_cla_id, " +
									"mdl.category_cla_id, " +
									"mdl.cod_fipe, " +
									"brd.brd_id, " +
									"brd.name AS brand_name, " +
									"brd.active AS brand_active " +
					"FROM model mdl " +
					"INNER JOIN brand brd ON brd.brd_id = mdl.brd_id " +
					"WHERE brd.brd_id = " + id + " " +
					" AND mdl.active = 1  " +
					"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
					"LIMIT " + pageable.getPageSize() + " " +
					"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] model.listAllByBrand: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ModelMapper() );

		} catch (Exception e) {
			log.error( "Erro ao listar os modelos.", e );
			throw new AppException( "Erro ao listar os modelos.", e );
		}
	}


	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Model> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Model> find(Model model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mdl_id");	
			}
			
			Order order = Order.asc( "mdl_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT mdl.mdl_id, ");
			query.append("		mdl.name, ");
			query.append("		mdl.active, ");
			query.append("		mdl.body_type_cla_id, "); 
			query.append("		mdl.type_cla_id, ");
			query.append("		mdl.category_cla_id, "); 
			query.append("		mdl.cod_fipe, "); 
			query.append("		brd.brd_id, ");
			query.append("		brd.name AS brand_name, ");
			query.append("		brd.active AS brand_active ");
			query.append("FROM model mdl ");
			query.append("INNER JOIN brand brd ON brd.brd_id = mdl.brd_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND mdl.mdl_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND mdl.name = :name " );
					params.addValue("name", model.getName());
					hasFilter = true;
				}
				
				if( model.getCodFipe() != null ) {
					query.append(" AND mdl.cod_fipe = :codFipe ");
					params.addValue("codFipe", model.getCodFipe());
					hasFilter = true;
				}
				
				if( model.getActive() != null ) {
					query.append(" AND mdl.active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}
				
				if( model.getBrand() != null && model.getBrand().getId() != null ) {
					query.append(" AND mdl.brd_id = :brdId ");
					params.addValue("brdId", model.getBrand().getId());
					hasFilter = true;
				}
				
				if( model.getBodyType() != null ) {
					query.append(" AND mdl.body_type_cla_id = :bodyType ");
					params.addValue("bodyType", model.getBodyType().getType().getId());
					hasFilter = true;
				}

				if( model.getSize() != null ) {
					query.append(" AND mdl.type_cla_id = :type_cla_id ");
					params.addValue("type_cla_id", model.getSize().getType().getId());
					hasFilter = true;
				}
				
				if( model.getCategory() != null ) {
					query.append(" AND mdl.category_cla_id = :categoryType ");
					params.addValue("categoryType", model.getCategory().getType().getId());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] model.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ModelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os modelos.", e );
			throw new AppException( "Erro ao buscar os modelos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Model, Pageable)}
	 */
	@Override
	public Optional<Model> find(Model model) throws AppException {
		List<Model> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<Model> search(Model model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "mdl_id");	
			}
			
			Order order = Order.asc( "mdl_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT mdl.mdl_id, ");
			query.append("		mdl.name, ");
			query.append("		mdl.active, ");
			query.append("		mdl.body_type_cla_id, "); 
			query.append("		mdl.type_cla_id, ");
			query.append("		mdl.category_cla_id, "); 
			query.append("		mdl.cod_fipe, "); 
			query.append("		brd.brd_id, ");
			query.append("		brd.name AS brand_name, ");
			query.append("		brd.active AS brand_active ");
			query.append("FROM model mdl ");
			query.append("INNER JOIN brand brd ON brd.brd_id = mdl.brd_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND mdl.mdl_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND mdl.name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
					hasFilter = true;
					
					if(model.getBrand() != null && model.getBrand().getName() != null) {
						query.append(" OR brd.name like :brandName ");
						params.addValue("brandName", this.mapLike(model.getBrand().getName()));
					}
				}
				
				if( model.getCodFipe() != null ) {
					query.append(" AND mdl.cod_fipe = :codFipe ");
					params.addValue("codFipe", model.getCodFipe());
					hasFilter = true;
				}
				
				if( model.getActive() != null ) {
					query.append(" AND mdl.active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}
				
				if(model.getBrand() != null && model.getBrand().getId() != null) {
					query.append(" AND mdl.brd_id = :brdId ");
					params.addValue("brdId", model.getBrand().getId());
					hasFilter = true;
				}
				
				if( model.getBodyType() != null ) {
					query.append(" AND mdl.body_type_cla_id = :bodyType ");
					params.addValue("bodyType", model.getBodyType().getType().getId());
					hasFilter = true;
				}

				if( model.getSize() != null ) {
					query.append(" AND mdl.type_cla_id = :type_cla_id ");
					params.addValue("type_cla_id", model.getSize().getType().getId());
					hasFilter = true;
				}
				
				if( model.getCategory() != null ) {
					query.append(" AND mdl.category_cla_id = :categoryType ");
					params.addValue("categoryType", model.getCategory().getType().getId());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] model.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ModelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os modelos.", e );
			throw new AppException( "Erro ao procurar os modelos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Model, Pageable)}
	 */
	@Override
	public List<Model> search(Model model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<Model> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT mdl.mdl_id, " +
							"		mdl.name, " + 
							"		mdl.active, " + 
							"		mdl.body_type_cla_id, " + 
							"		mdl.type_cla_id, " +
							"		mdl.category_cla_id, " + 
							"		mdl.cod_fipe, " + 
							"		brd.brd_id, " + 
							"		brd.name AS brand_name, " + 
							"		brd.active AS brand_active " +
							"FROM model mdl " +
							"INNER JOIN brand brd ON brd.brd_id = mdl.brd_id " +
							"WHERE mdl.mdl_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] model.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ModelMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o modelo.", e );
			throw new AppException( "Erro ao consultar o modelo.", e );
		}
	}

	@Override
	public Optional<Model> save(Model model) throws AppException {
		try {
			String query = "INSERT INTO model ( name, active, brd_id, body_type_cla_id, type_cla_id, category_cla_id, cod_fipe ) " +
						   "VALUES ( :name, :active, :brdId, :bodyType, :type_cla_id, :categoryType, :codFipe ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			params.addValue( "brdId", (model.getBrand() != null ? model.getBrand().getId() : null ) );
			params.addValue( "bodyType", model.getBodyType().getType().getId());
			params.addValue( "type_cla_id", model.getSize().getType().getId());
			params.addValue( "categoryType", model.getCategory().getType().getId());
			params.addValue( "codFipe", model.getCodFipe());

			log.trace( "[QUERY] model.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o modelo: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o modelo.", e);
		}
	}

	@Override
	public Optional<Model> update(Model model) throws AppException {
		try {
			String query = "UPDATE model SET name=:name, active=:active, brd_id=:brdId, body_type_cla_id=:bodyType, type_cla_id=:type_cla_id, category_cla_id=:categoryType, cod_fipe=:codFipe " +
						   "WHERE mdl_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			params.addValue( "brdId", (model.getBrand() != null ? model.getBrand().getId() : null ) );
			params.addValue( "bodyType", model.getBodyType().getType().getId());
			params.addValue( "type_cla_id", model.getSize().getType().getId());
			params.addValue( "categoryType", model.getCategory().getType().getId());
			params.addValue( "codFipe", model.getCodFipe());

			log.trace( "[QUERY] model.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o modelo: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o modelo.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM model WHERE mdl_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] model.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o modelo" , e );
			throw new AppException( "Erro ao excluir o modelo.", e );
		}

	}

	@Override
	public boolean hasLeadRelationship(Integer modelId) throws AppException {
		try {
			String query = "SELECT CASE WHEN EXISTS ( " +
					"SELECT mdl_id FROM " + schemaName + "lead WHERE mdl_id = :mdlId LIMIT 1 " +
					") " +
					"THEN TRUE " +
					"ELSE FALSE " +
					"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "mdlId", modelId );

			log.trace( "[QUERY] model.hasLeadRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com lead." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com lead.", e );
		}
	}
	

	@Override
	public boolean hasItemRelationship(Integer modelId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT mdl_id FROM item_model WHERE mdl_id = :mdlId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "mdlId", modelId );

			log.trace( "[QUERY] model.hasItemRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com item." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com item.", e );
		}
	}

	@Override
	public boolean hasProductRelationship(Integer modelId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT mdl_id FROM product_model WHERE mdl_id = :mdlId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "mdlId", modelId );

			log.trace( "[QUERY] model.hasProductRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com produto." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com produto.", e );
		}
	}

	@Override
	public boolean hasVehicleRelationship(Integer modelId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT mdl_id FROM vehicle WHERE mdl_id = :mdlId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "mdlId", modelId );

			log.trace( "[QUERY] model.hasVehicleRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com veículo." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com veículo.", e );
		}
	}


}
