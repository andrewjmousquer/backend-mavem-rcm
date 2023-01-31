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
import com.portal.dao.IPartnerGroupDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PartnerGroupMapper;
import com.portal.model.PartnerGroup;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PartnerGroupDAO extends BaseDAO implements IPartnerGroupDAO {

	@Override
	public List<PartnerGroup> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id");	
			}
			
			Order order = Order.desc( "ptg_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT ptg.ptg_id , ptg.name, ptg.active   " +
							"FROM partner_group ptg " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] partnerGroup.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new PartnerGroupMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os grupos de parceiros.", e );
			throw new AppException( "Erro ao listar os grupos de parceiros.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<PartnerGroup> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<PartnerGroup> find(PartnerGroup model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id");	
			}
			
			Order order = Order.asc( "ptg_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  ptg.ptg_id , ptg.name, ptg.active  ");
			query.append("FROM partner_group ptg ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND ptg.ptg_id = :id ");
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
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] partnerGroup.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PartnerGroupMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os grupos de parceiros.", e );
			throw new AppException( "Erro ao buscar os grupos de parceiros.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(PartnerGroup, Pageable)}
	 */
	@Override
	public Optional<PartnerGroup> find(PartnerGroup model) throws AppException {
		List<PartnerGroup> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<PartnerGroup> search(PartnerGroup model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptg_id");	
			}
			
			Order order = Order.asc( "ptg_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  ptg.ptg_id , ptg.name, ptg.active  ");
			query.append("FROM partner_group ptg ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND ptg.ptg_id = :id ");
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
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] partnerGroup.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PartnerGroupMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os grupos de parceiros.", e );
			throw new AppException( "Erro ao procurar os grupos de parceiros.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(PartnerGroup, Pageable)}
	 */
	@Override
	public List<PartnerGroup> search(PartnerGroup model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<PartnerGroup> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT  ptg.ptg_id , ptg.name, active  " +
							"FROM partner_group ptg " +
							"WHERE ptg.ptg_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] partnerGroup.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PartnerGroupMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o grupo de parceiro.", e );
			throw new AppException( "Erro ao consultar o grupo de parceiro.", e );
		}
	}

	@Override
	public Optional<PartnerGroup> save(PartnerGroup model) throws AppException {
		try {
			String query = "INSERT INTO partner_group ( name, active ) VALUES ( :name, :active ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			
			log.trace( "[QUERY] partnerGroup.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o grupo de parceiros: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o grupo de parceiros.", e);
		}
	}

	@Override
	public Optional<PartnerGroup> update(PartnerGroup model) throws AppException {
		try {
			String query = "UPDATE partner_group SET name=:name, active=:active WHERE ptg_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
	
			log.trace( "[QUERY] partnerGroup.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o grupo de parceiros: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o grupo de parceiros.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM partner_group WHERE ptg_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] partnerGroup.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o grupo de parceiros.", e );
			throw new AppException( "Erro ao excluir o grupo de parceiros.", e );
		}

	}

	@Override
	public boolean hasPartnerRelationship(Integer ptgId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT ptg_id FROM partner WHERE ptg_id = :ptgId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ptgId", ptgId );

			log.trace( "[QUERY] partnerGroup.hasPartnerRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com parceiro." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com parceiro.", e );
		}
	}
}
