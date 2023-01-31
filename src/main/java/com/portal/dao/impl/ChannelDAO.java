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
import com.portal.dao.IChannelDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ChannelMapper;
import com.portal.model.Channel;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ChannelDAO extends BaseDAO implements IChannelDAO {

	@Override
	public List<Channel> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");	
			}
			
			Order order = Order.desc( "chn_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT * " +
							"FROM channel chn " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] channel.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ChannelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os canais.", e );
			throw new AppException( "Erro ao listar os canais.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Channel> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Channel> find(Channel model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");	
			}
			
			Order order = Order.asc( "chn_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM channel chn ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND chn.chn_id = :id ");
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

				if( model.getHasPartner() != null ) {
					query.append(" AND has_partner = :hasPartner " );
					params.addValue("hasPartner", PortalNumberUtils.booleanToInt( model.getHasPartner() ) );
					hasFilter = true;
				}
				
				if( model.getHasInternalSale() != null ) {
					query.append(" AND has_internal_sale = :hasInternalSale " );
					params.addValue("hasInternalSale", PortalNumberUtils.booleanToInt( model.getHasInternalSale() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] channel.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ChannelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os canais.", e );
			throw new AppException( "Erro ao buscar os canais.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Channel, Pageable)}
	 */
	@Override
	public Optional<Channel> find(Channel model) throws AppException {
		List<Channel> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<Channel> search(Channel model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "chn_id");	
			}
			
			Order order = Order.asc( "chn_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM channel chn ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND chn.chn_id = :id ");
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
				
				if( model.getHasPartner() != null ) {
					query.append(" AND has_partner = :hasPartner " );
					params.addValue("hasPartner", PortalNumberUtils.booleanToInt( model.getHasPartner() ) );
					hasFilter = true;
				}
				
				if( model.getHasInternalSale() != null ) {
					query.append(" AND has_internal_sale = :hasInternalSale " );
					params.addValue("hasInternalSale", PortalNumberUtils.booleanToInt( model.getHasInternalSale() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] channel.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ChannelMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os canais.", e );
			throw new AppException( "Erro ao procurar os canais.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Channel, Pageable)}
	 */
	@Override
	public List<Channel> search(Channel model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<Channel> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM channel chn " +
							"WHERE chn.chn_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] channel.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ChannelMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o canal.", e );
			throw new AppException( "Erro ao consultar o canal.", e );
		}
	}

	@Override
	public Optional<Channel> save(Channel model) throws AppException {
		try {
			String query = "INSERT INTO channel ( name, active, has_partner, has_internal_sale ) " +
					 	   "VALUES ( :name, :active, :hasPartner, :hasInternalSale ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			params.addValue( "hasPartner", PortalNumberUtils.booleanToInt( model.getHasPartner() ) );
			params.addValue( "hasInternalSale", PortalNumberUtils.booleanToInt( model.getHasInternalSale() ) );
			
			log.trace( "[QUERY] channel.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o canal: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o canal.", e);
		}
	}

	@Override
	public Optional<Channel> update(Channel model) throws AppException {
		try {
			String query = "UPDATE channel SET name=:name, active=:active, has_partner=:hasPartner, has_internal_sale=:hasInternalSale " +
					 	   "WHERE chn_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			params.addValue( "hasPartner", PortalNumberUtils.booleanToInt( model.getHasPartner() ) );
			params.addValue( "hasInternalSale", PortalNumberUtils.booleanToInt( model.getHasInternalSale() ) );
			
			log.trace( "[QUERY] channel.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o canal: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o canal.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM channel WHERE chn_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] channel.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o canal" , e );
			throw new AppException( "Erro ao excluir o canal.", e );
		}

	}
	
	@Override
	public boolean hasPartnerRelationship(Integer chnId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT chn_id FROM partner WHERE chn_id = :chnId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "chnId", chnId );

			log.trace( "[QUERY] channel.hasPartnerRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com parceiro." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com parceiro.", e );
		}
	}
	
	@Override
	public boolean hasPriceListRelationship(Integer chnId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT chn_id FROM price_list WHERE chn_id = :chnId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "chnId", chnId );

			log.trace( "[QUERY] channel.hasPriceListRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com lista de preço." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com lista de preço.", e );
		}
	}

	@Override
	public  Optional<Channel> getChannelByProposal(Integer ppsId) throws AppException {
		try {
			
			String query = 	"select c.* from proposal p "
							+ "inner join proposal_detail pd on pd.pps_id = p.pps_id "
							+ "inner join proposal_detail_vehicle pdv on pdv.ppd_id = pd.ppd_id "
							+ "inner join price_product ppr on ppr.ppr_id = pdv.ppr_id "
							+ "inner join price_list prl on prl.prl_id = ppr.prl_id "
							+ "inner join channel c on c.chn_id = prl.chn_id "
							+ "where "
							+ "	p.pps_id = :id ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", ppsId );

			log.trace( "[QUERY] channel.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ChannelMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o canal.", e );
			throw new AppException( "Erro ao consultar o canal.", e );
		}
	}
}
