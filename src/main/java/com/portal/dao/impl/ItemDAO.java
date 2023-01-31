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
import com.portal.dao.IItemDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ItemMapper;
import com.portal.model.Item;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ItemDAO extends BaseDAO implements IItemDAO {

	@Override
	public List<Item> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id");	
			}
			
			Order order = Order.desc( "itm_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT itm.*, " +
									"	   item_mand.cla_id as mand_cla_id, " +
									"	   item_mand.label as mand_label, " +
									"	   item_mand.value as mand_value, " +
									"	   item_mand.type as mand_type, " +
									"	   item_resp.cla_id as resp_cla_id, " +
									"	   item_resp.label as resp_label, " +
									"	   item_resp.value as resp_value, " +
									"	   item_resp.type as resp_type, " +
								    "itt.name AS itt_name, " +
								    "itt.mandatory AS itt_mandatory, " +
								    "itt.multi AS itt_multi, " +
								    "itt.seq AS itt_seq " +
							"FROM item itm " +
							"INNER JOIN classifier as item_mand on itm.mandatory_cla_id = item_mand.cla_id " +
							"INNER JOIN classifier as item_resp on itm.responsability_cla_id = item_resp.cla_id " +
							"INNER JOIN item_type itt ON itt.itt_id = itm.itt_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] item.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ItemMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os itens.", e );
			throw new AppException( "Erro ao listar os itens.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Item> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Item> find(Item model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id");	
			}
			
			Order order = Order.asc( "itm_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT itm.*, ");
			query.append("		 item_mand.cla_id as mand_cla_id, ");
			query.append("		 item_mand.label as mand_label, ");
			query.append("		 item_mand.value as mand_value, ");
			query.append("		 item_mand.type as mand_type, ");
			query.append("		 item_resp.cla_id as resp_cla_id, ");
			query.append("		 item_resp.label as resp_label, ");
			query.append("		 item_resp.value as resp_value, ");
			query.append("		 item_resp.type as resp_type, ");
			query.append("		 itt.name AS itt_name, ");
			query.append("		 itt.mandatory AS itt_mandatory, ");
			query.append("		 itt.multi AS itt_multi, ");
			query.append("		 itt.seq AS itt_seq ");
			query.append("FROM item itm ");
			query.append("INNER JOIN classifier as item_mand on itm.mandatory_cla_id = item_mand.cla_id ");
			query.append("INNER JOIN classifier as item_resp on itm.responsability_cla_id = item_resp.cla_id ");
			query.append("INNER JOIN item_type itt ON itt.itt_id = itm.itt_id " );
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND itm.itm_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND itm.name = :name " );
					params.addValue("name", model.getName());
					hasFilter = true;
				}
				
				if(model.getCod() != null && !model.getCod().equals("")) {
					query.append(" AND cod = :cod " );
					params.addValue("cod", model.getCod());
					hasFilter = true;
				}

				if( model.getSeq() != null ) {
					query.append(" AND itm.seq = :seq " );
					params.addValue("seq", model.getSeq());
					hasFilter = true;
				}
				
				if( model.getForFree() != null ) {
					query.append(" AND for_free = :forFree " );
					params.addValue("forFree", PortalNumberUtils.booleanToInt( model.getForFree() ));
					hasFilter = true;
				}
				
				if( model.getGeneric() != null ) {
					query.append(" AND generic = :generic " );
					params.addValue("generic", PortalNumberUtils.booleanToInt( model.getGeneric() ));
					hasFilter = true;
				}
				
				if( model.getMandatory() != null ) {
					query.append(" AND mandatory_cla_id = :mandatory " );
					params.addValue("mandatory", model.getMandatory().getId() );
					hasFilter = true;
				}
				
				if( model.getItemType() != null ) {
					query.append(" AND itm.itt_id = :itemType " );
					params.addValue("itemType", model.getItemType().getId() );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] item.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ItemMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os itens.", e );
			throw new AppException( "Erro ao buscar os itens.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Item, Pageable)}
	 */
	@Override
	public Optional<Item> find(Item model) throws AppException {
		List<Item> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<Item> search(Item model, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "itm_id");	
			}
			
			Order order = Order.asc( "itm_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT itm.*, ");
			query.append("		 item_mand.cla_id as mand_cla_id, ");
			query.append("		 item_mand.label as mand_label, ");
			query.append("		 item_mand.value as mand_value, ");
			query.append("		 item_mand.type as mand_type, ");
			query.append("		 item_resp.cla_id as resp_cla_id, ");
			query.append("		 item_resp.label as resp_label, ");
			query.append("		 item_resp.value as resp_value, ");
			query.append("		 item_resp.type as resp_type, ");
			query.append("		 itt.name AS itt_name, ");
			query.append("		 itt.mandatory AS itt_mandatory, ");
			query.append("		 itt.multi AS itt_multi, ");
			query.append("		 itt.seq AS itt_seq ");
			query.append("FROM item itm ");
			query.append("INNER JOIN classifier as item_mand on itm.mandatory_cla_id = item_mand.cla_id ");
			query.append("INNER JOIN classifier as item_resp on itm.responsability_cla_id = item_resp.cla_id ");
			query.append("INNER JOIN item_type itt ON itt.itt_id = itm.itt_id " );
			query.append("WHERE itm_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND itm.itm_id = :id ");
					params.addValue("id", model.getId());
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND itm.name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
				}
				
				if(model.getCod() != null && !model.getCod().equals("")) {
					query.append(" AND cod like :cod " );
					params.addValue("cod", this.mapLike(model.getCod()));
				}

				if( model.getSeq() != null ) {
					query.append(" AND itm.seq = :seq " );
					params.addValue("seq", model.getSeq());
				}
				
				if( model.getForFree() != null ) {
					query.append(" AND for_free = :forFree " );
					params.addValue("forFree", PortalNumberUtils.booleanToInt( model.getForFree() ));
				}
				
				if( model.getGeneric() != null ) {
					query.append(" AND generic = :generic " );
					params.addValue("generic", PortalNumberUtils.booleanToInt( model.getGeneric() ));
				}
				
				if( model.getMandatory() != null ) {
					query.append(" AND mandatory_cla_id = :mandatory " );
					params.addValue("mandatory", model.getMandatory().getId() );
				}
				
				if( model.getItemType() != null ) {
					query.append(" AND itm.itt_id = :itemType " );
					params.addValue("itemType", model.getItemType().getId() );
				}
			}
			
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			log.trace( "[QUERY] item.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ItemMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os itens.", e );
			throw new AppException( "Erro ao procurar os itens.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Item, Pageable)}
	 */
	@Override
	public List<Item> search(Item model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<Item> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT itm.*, " +
									"	item_mand.cla_id as mand_cla_id, " +
									"	item_mand.label as mand_label, " +
									"	item_mand.value as mand_value, " +
									"	item_mand.type as mand_type, " +
									"	item_resp.cla_id as resp_cla_id, " +
									"	item_resp.label as resp_label, " +
									"	item_resp.value as resp_value, " +
									"	item_resp.type as resp_type, " +
									"	itt.name AS itt_name, " +
									"	itt.mandatory AS itt_mandatory, " +
									"	itt.multi AS itt_multi, " +
									"	itt.seq AS itt_seq " +
							"FROM item itm " +
							"INNER JOIN classifier as item_mand on itm.mandatory_cla_id = item_mand.cla_id " +
							"INNER JOIN classifier as item_resp on itm.responsability_cla_id = item_resp.cla_id " +
							"INNER JOIN item_type itt ON itt.itt_id = itm.itt_id " +
							"WHERE itm.itm_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] item.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ItemMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o item.", e );
			throw new AppException( "Erro ao consultar o item.", e );
		}
	}

	@Override
	public Optional<Item> save(Item model) throws AppException {
		try {
			String query = "INSERT INTO item (name, cod, seq, for_free, generic, mandatory_cla_id, itt_id, file, icon, description, hyperlink, responsability_cla_id, term, term_work_day, highlight) " +
					 	   "VALUES (:name, :cod, :seq, :forFree, :generic, :mandatory, :itemType, :file, :icon, :descr, :hyperlink, :responsability, :term, :term_work_day, :highlight);";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName().toUpperCase());
			params.addValue("cod", model.getCod());
			params.addValue("seq", model.getSeq());
			params.addValue("forFree", PortalNumberUtils.booleanToInt( model.getForFree() ));
			params.addValue("generic", PortalNumberUtils.booleanToInt( model.getGeneric() ));
			params.addValue("mandatory", model.getMandatory().getId() );
			params.addValue("itemType", model.getItemType().getId() );
			params.addValue("file", model.getFile() );
			params.addValue("icon", model.getIcon() );
			params.addValue("descr", (model.getDescription() != null ? model.getDescription().toUpperCase() : null) );
			params.addValue("hyperlink", model.getHyperlink() );
			params.addValue("responsability", model.getResponsability().getId() );
			params.addValue("term", model.getTerm() );
			params.addValue("term_work_day", PortalNumberUtils.booleanToInt(model.getTermWorkDay() ));
			params.addValue("highlight", PortalNumberUtils.booleanToInt(model.getHighlight() ));
	
			log.trace( "[QUERY] item.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query, params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o item: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o item.", e);
		}
	}

	@Override
	public Optional<Item> update(Item model) throws AppException {
		try {
			String query = "UPDATE item " +
					 	   "SET name = :name, cod = :cod, seq = :seq, for_free = :forFree, generic = :generic, " +
				 	   	   "mandatory_cla_id = :mandatory, itt_id = :itemType, file = :file, icon = :icon, " +
					 	   "description = :descr, hyperlink = :hyperlink, responsability_cla_id = :responsability, "+
					 	   "term = :term, term_work_day = :term_work_day, highlight = :highlight " +
					 	   "WHERE itm_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId() );
			params.addValue("name", model.getName().toUpperCase());
			params.addValue("cod", model.getCod());
			params.addValue("seq", model.getSeq());
			params.addValue("forFree", PortalNumberUtils.booleanToInt( model.getForFree() ));
			params.addValue("generic", PortalNumberUtils.booleanToInt( model.getGeneric() ));
			params.addValue("mandatory", model.getMandatory().getId() );
			params.addValue("itemType", model.getItemType().getId() );
			params.addValue("file", model.getFile() );
			params.addValue("icon", model.getIcon() );
			params.addValue("descr", (model.getDescription() != null ? model.getDescription().toUpperCase() : null));
			params.addValue("hyperlink", model.getHyperlink() );
			params.addValue("responsability", model.getResponsability().getId() );
			params.addValue("term", model.getTerm() );
			params.addValue("term_work_day", PortalNumberUtils.booleanToInt(model.getTermWorkDay() ));
			params.addValue("highlight", PortalNumberUtils.booleanToInt(model.getHighlight() ));
	
			log.trace( "[QUERY] item.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o item: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o item.", e);
		}
	}
	
	@Override
	public void updateFile(Integer id, String column, String value) throws AppException {
		try {
			String query = "UPDATE item " +
					 	   "SET " + column + "=:value " +
					 	   "WHERE itm_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", id );
			params.addValue("value", value);
	
			log.trace( "[QUERY] item.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o item: {}", id, e );
			throw new AppException( "Erro ao tentar atualizar o item.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM item WHERE itm_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] item.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o item" , e );
			throw new AppException( "Erro ao excluir o item.", e );
		}

	}

	@Override
	public boolean hasModelRelationship(Integer itmId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT itm_id FROM item_model WHERE itm_id = :itmId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "itmId", itmId );

			log.trace( "[QUERY] item.hasModelRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com modelo." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com modelo.", e );
		}
	}
	
	@Override
	public boolean hasPriceItemRelationship( Integer itmId ) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT itm_id FROM price_item WHERE itm_id = :itmId LIMIT 1 " +
							")" +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "itmId", itmId );

			log.trace( "[QUERY] item.hasPriceListRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com lista de preço." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com lista de preço.", e );
		}
	}
	
	@Override
	public boolean hasPriceItemModelRelationship( Integer itmId ) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
							"	SELECT itm_id FROM price_item_model WHERE itm_id = :itmId LIMIT 1 " +
							")" +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "itmId", itmId );

			log.trace( "[QUERY] item.hasPriceListRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com lista de preço." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com lista de preço.", e );
		}
	}
	
	@Override
	public void saveItemImage(Integer itmId, String image) throws AppException {
		
		try {
			
	        String queryUpdate = "UPDATE item " +
				 	   "SET photo_url=:photo_url  " +
				 	   "WHERE itm_id = :itmId";
		
			MapSqlParameterSource paramsUpdate = new MapSqlParameterSource();
			paramsUpdate.addValue("itmId", itmId );
			paramsUpdate.addValue("photo_url", image );
			
			log.trace( "[QUERY] item.update: {} [PARAMS]: {}", queryUpdate, paramsUpdate.getValues() );
			this.getNamedParameterJdbcTemplate().update(queryUpdate.toString(), paramsUpdate);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar inserir imagem do item id: {}", itmId, e );
			throw new AppException( "Erro ao tentar atualizar o item.", e);
		}
	}
	
	@Override
	public String getItemImage(Integer itmId) throws AppException{
		
		try {
			
			String query = 	"select photo_url from item i "
							+ "where i.itm_id = :itmid";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "itmid", itmId );

			log.trace( "[QUERY] item.getItemImage: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getString("photo_url") );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar imagem de item." , e );
			throw new AppException( "Erro ao buscar imagem de item.", e );
		}
		
	}
}
