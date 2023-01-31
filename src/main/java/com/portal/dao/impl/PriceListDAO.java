package com.portal.dao.impl;

import java.time.LocalDateTime;
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
import com.portal.dao.IPriceListDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PriceListMapper;
import com.portal.model.PriceList;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PriceListDAO extends BaseDAO implements IPriceListDAO {
	
	@Override
	public List<PriceList> listAll(Pageable pageable) throws AppException {
		try {
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");	
			}
			
			Order order = Order.desc( "prl_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT prl.*, " +
								   "chn.name AS chn_name, " +
								   "chn.active AS chn_active, " +
								   "chn.has_partner AS chn_has_partner " +
							"FROM price_list prl " +
							"INNER JOIN channel chn ON chn.chn_id = prl.chn_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			
			log.trace( "[QUERY] priceList.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new PriceListMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar as listas de preços.", e );
			throw new AppException( "Erro ao listar as listas de preços.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<PriceList> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<PriceList> find( PriceList model, Pageable pageable ) throws AppException {
		try {
			
			boolean hasFilter = false;
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");	
			}
			
			Order order = Order.asc( "prl_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  	prl.*, ");
			query.append("			chn.name AS chn_name, ");
			query.append("			chn.active AS chn_active, ");
			query.append("			chn.has_partner AS chn_has_partner " );
			query.append("FROM price_list prl ");
			query.append("INNER JOIN channel chn ON chn.chn_id = prl.chn_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND prl.prl_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND prl.name = :name " );
					params.addValue("name", model.getName());
					hasFilter = true;
				}
				
				if( model.getChannel() != null && model.getChannel().getId() != null ) {
					query.append(" AND prl.chn_id = :chnId " );
					params.addValue("chnId", model.getChannel().getId());
					hasFilter = true;
				}
				
				if( model.getStart() != null ) {
					query.append(" AND prl.start_date = :startDate " );
					params.addValue("startDate", PortalTimeUtils.localDateTimeFormat( model.getStart(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				}
				
				if( model.getEnd() != null ) {
					query.append(" AND prl.end_date = :endDate " );
					params.addValue("endDate", PortalTimeUtils.localDateTimeFormat( model.getEnd(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				}
				
				if( model.getAllPartners() != null ) {
					query.append(" AND prl.all_partners = :allPartners ");
					params.addValue("allPartners", PortalNumberUtils.booleanToInt( model.getAllPartners() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] priceList.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceListMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as listas de preços.", e );
			throw new AppException( "Erro ao buscar as listas de preços.", e );
		}
	}
	
	@Override
	public List<PriceList> findByStartPeriod(PriceList model, LocalDateTime start, LocalDateTime end, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");	
			}
			
			Order order = Order.asc( "prl_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  	prl.*, ");
			query.append("			chn.name AS chn_name, ");
			query.append("			chn.active AS chn_active, ");
			query.append("			chn.has_partner AS chn_has_partner " );
			query.append("FROM price_list prl ");
			query.append("INNER JOIN channel chn ON chn.chn_id = prl.chn_id ");
			query.append("WHERE start_date BETWEEN :startDate AND :endStart ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND prl.prl_id = :id ");
					params.addValue("id", model.getId());
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND prl.name = :name " );
					params.addValue("name", model.getName());
				}
				
				if( model.getChannel() != null && model.getChannel().getId() != null ) {
					query.append(" AND prl.chn_id = :chnId " );
					params.addValue("chnId", model.getChannel().getId());
				}
				
				if( model.getAllPartners() != null ) {
					query.append(" AND prl.all_partners = :allPartners ");
					params.addValue("allPartners", PortalNumberUtils.booleanToInt( model.getAllPartners() ) );
				}
			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "startDate", PortalTimeUtils.localDateTimeFormat( start, "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "endStart", PortalTimeUtils.localDateTimeFormat( end, "yyyy-MM-dd HH:mm:ss" ) );
			
			log.trace( "[QUERY] priceList.findByStartPeriod: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceListMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as listas de preços.", e );
			throw new AppException( "Erro ao buscar as listas de preços.", e );
		}
	}

	@Override
	public List<PriceList> findByEndPeriod(PriceList model, LocalDateTime start, LocalDateTime end, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");	
			}
			
			Order order = Order.asc( "prl_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  	prl.*, ");
			query.append("			chn.name AS chn_name, ");
			query.append("			chn.active AS chn_active, ");
			query.append("			chn.has_partner AS chn_has_partner " );
			query.append("FROM price_list prl ");
			query.append("INNER JOIN channel chn ON chn.chn_id = prl.chn_id ");
			query.append("WHERE end_date BETWEEN :startDate AND :endStart ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND prl.prl_id = :id ");
					params.addValue("id", model.getId());
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND prl.name = :name " );
					params.addValue("name", model.getName());
				}
				
				if( model.getChannel() != null && model.getChannel().getId() != null ) {
					query.append(" AND prl.chn_id = :chnId " );
					params.addValue("chnId", model.getChannel().getId());
				}
				
				if( model.getAllPartners() != null ) {
					query.append(" AND prl.all_partners = :allPartners ");
					params.addValue("allPartners", PortalNumberUtils.booleanToInt( model.getAllPartners() ) );
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "startDate", PortalTimeUtils.localDateTimeFormat( start, "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "endStart", PortalTimeUtils.localDateTimeFormat( end, "yyyy-MM-dd HH:mm:ss" ) );
			
			log.trace( "[QUERY] priceList.findByEndPeriod: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceListMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as listas de preços.", e );
			throw new AppException( "Erro ao buscar as listas de preços.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(PriceList, Pageable)}
	 */
	@Override
	public Optional<PriceList> find(PriceList model) throws AppException {
		List<PriceList> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<PriceList> search(PriceList model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "prl_id");	
			}
			
			Order order = Order.asc( "prl_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  	prl.*, ");
			query.append("			chn.name AS chn_name, ");
			query.append("			chn.active AS chn_active, ");
			query.append("			chn.has_partner AS chn_has_partner " );
			query.append("FROM price_list prl ");
			query.append("INNER JOIN channel chn ON chn.chn_id = prl.chn_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND prl.prl_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND prl.name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
					hasFilter = true;
				}
				
				if( model.getChannel() != null && model.getChannel().getId() != null ) {
					query.append(" AND prl.chn_id = :chnId " );
					params.addValue("chnId", model.getChannel().getId());
					hasFilter = true;
				}
				
				if( model.getStart() != null ) {
					query.append(" AND start_date = :startDate " );
					params.addValue("startDate", PortalTimeUtils.localDateTimeFormat( model.getStart(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				}
				
				if( model.getEnd() != null ) {
					query.append(" AND end_date = :endDate " );
					params.addValue("endDate", PortalTimeUtils.localDateTimeFormat( model.getEnd(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				}
				
				if( model.getAllPartners() != null ) {
					query.append(" AND prl.all_partners = :allPartners ");
					params.addValue("allPartners", PortalNumberUtils.booleanToInt( model.getAllPartners() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] priceList.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceListMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar as listas de preços.", e );
			throw new AppException( "Erro ao procurar as listas de preços.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(PriceList, Pageable)}
	 */
	@Override
	public List<PriceList> search(PriceList model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<PriceList> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT prl.*, " +
								   "chn.name AS chn_name, " +
								   "chn.active AS chn_active, " +
								   "chn.has_partner AS chn_has_partner " +
							"FROM price_list prl " +
							"INNER JOIN channel chn ON chn.chn_id = prl.chn_id " +
							"WHERE prl.prl_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] priceList.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PriceListMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a lista de preço.", e );
			throw new AppException( "Erro ao consultar a lista de preço.", e );
		}
	}

	@Override
	public Optional<PriceList> save( PriceList model ) throws AppException {
		try {
			String query = "INSERT INTO price_list ( name, start_date, end_date, chn_id, all_partners ) VALUES ( :name, :startDate, :endDate, :chnId, :allPartners ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "startDate", PortalTimeUtils.localDateTimeFormat( model.getStart(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "endDate", PortalTimeUtils.localDateTimeFormat( model.getEnd(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "chnId", ( model.getChannel() != null ? model.getChannel().getId() : null ) );
			params.addValue( "allPartners", PortalNumberUtils.booleanToInt( model.getAllPartners() ) );
	
			log.trace( "[QUERY] priceList.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a lista de preço: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a lista de preço.", e);
		}
	}

	@Override
	public Optional<PriceList> update(PriceList model) throws AppException {
		try {
			String query = "UPDATE price_list SET name=:name, start_date=:startDate, end_date=:endDate, chn_id=:chnId, all_partners=:allPartners WHERE prl_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "startDate", PortalTimeUtils.localDateTimeFormat( model.getStart(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "endDate", PortalTimeUtils.localDateTimeFormat( model.getEnd(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "chnId", ( model.getChannel() != null ? model.getChannel().getId() : null ) );
			params.addValue( "allPartners", PortalNumberUtils.booleanToInt( model.getAllPartners() ) );
			params.addValue( "id", model.getId() );
	
			log.trace( "[QUERY] priceList.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a lista de preço: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a lista de preço.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM price_list WHERE prl_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] priceList.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a lista de preço" , e );
			throw new AppException( "Erro ao excluir a lista de preço.", e );
		}

	}

	@Override
	public List<PriceList> listOverlay(PriceList model) throws AppException {
		try {

			StringBuilder query = new StringBuilder(); 
			query.append( "SELECT  	prl.*, ");
			query.append( "			chn.name AS chn_name, ");
			query.append( "			chn.active AS chn_active, ");
			query.append("			chn.has_partner AS chn_has_partner " );
			query.append( "FROM price_list prl " );
			query.append( "INNER JOIN channel chn ON chn.chn_id = prl.chn_id ");
			query.append( "WHERE ( ( start_date >= :startDate AND start_date <= :endDate ) OR ( :startDate >= start_date AND :startDate <= end_date ) ) " );
			
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				params.addValue("startDate", PortalTimeUtils.localDateTimeFormat( model.getStart(), "yyyy-MM-dd HH:mm:ss" ) );
				params.addValue("endDate", PortalTimeUtils.localDateTimeFormat( model.getEnd(), "yyyy-MM-dd HH:mm:ss" ) );
				
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND prl.prl_id = :id ");
					params.addValue("id", model.getId());
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND prl.name = :name " );
					params.addValue("name", model.getName());
				}
				
				if( model.getChannel() != null && model.getChannel().getId() != null ) {
					query.append(" AND prl.chn_id = :chnId " );
					params.addValue("chnId", model.getChannel().getId());
				}
				
				if( model.getAllPartners() != null ) {
					query.append(" AND prl.all_partners = :allPartners ");
					params.addValue("allPartners", PortalNumberUtils.booleanToInt( model.getAllPartners() ) );
				}
			}

			log.trace( "[QUERY] priceList.hasOverlap: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceListMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar a existência de cadastros com sobreposição de vigência.", e );
			throw new AppException( "Erro ao buscar a existência de cadastros com sobreposição de vigência.", e );
		}
	}


}
