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
import com.portal.dao.IQualificationDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.QualificationMapper;
import com.portal.model.Qualification;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class QualificationDAO extends BaseDAO implements IQualificationDAO {
	
	@Override
	public List<Qualification> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");	
			}
			
			Order order = Order.desc( "qlf_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}

			String query = 	"SELECT qlf.qlf_id, " +
							"		qlf.name, " +
							"		qlf.seq, " +
							"		qlf.required, " +
							"		qlf.active " +
							"FROM qualification qlf " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			
			log.trace( "[QUERY] qualification.listAll: {}", query);

			return this.getJdbcTemplatePortal().query( query, new QualificationMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os qulificações.", e );
			throw new AppException( "Erro ao listar os qulificações.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Qualification> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Qualification> find( Qualification model, Pageable pageable ) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");	
			}
			
			Order order = Order.asc( "qlf_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  qlf.qlf_id,");
			query.append("		  qlf.name, ");
			query.append("		  qlf.seq,  ");
			query.append("		  qlf.required,  ");
			query.append("		  qlf.active  ");
			query.append("FROM qualification qlf ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND qlf.qlf_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name = :name " );
					params.addValue("name", model.getName());
					hasFilter = true;
				}
				
				if(model.getSeq() != null) {
					query.append(" AND seq = :seq " );
					params.addValue("seq", model.getSeq());
					hasFilter = true;
				}

				if(model.getRequired() != null) {
					query.append(" AND required = :required " );
					params.addValue("required", PortalNumberUtils.booleanToInt( model.getRequired() ) );
					hasFilter = true;
				}
				
				if(model.getActive() != null) {
					query.append(" AND active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", hasFilter );
			
			log.trace( "[QUERY] qualification.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new QualificationMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os qulificações.", e );
			throw new AppException( "Erro ao buscar os qulificações.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Qualification, Pageable)}
	 */
	@Override
	public Optional<Qualification> find(Qualification model) throws AppException {
		List<Qualification> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<Qualification> search(Qualification model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "qlf_id");	
			}
			
			Order order = Order.asc( "qlf_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT  qlf.qlf_id,");
			query.append("		  qlf.name, ");
			query.append("		  qlf.seq,  ");
			query.append("		  qlf.required,  ");
			query.append("		  qlf.active  ");
			query.append("FROM qualification qlf ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND qlf.qlf_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
					hasFilter = true;
				}
				
				if(model.getSeq() != null) {
					query.append(" AND seq = :seq " );
					params.addValue("seq", model.getSeq());
					hasFilter = true;
				}
				
				if(model.getRequired() != null) {
					query.append(" AND required = :required " );
					params.addValue("required", PortalNumberUtils.booleanToInt( model.getRequired() ) );
					hasFilter = true;
				}
				
				if(model.getActive() != null) {
					query.append(" AND active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}
			}
			
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", hasFilter);
			
			log.trace( "[QUERY] qualification.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new QualificationMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os qulificações.", e );
			throw new AppException( "Erro ao procurar os qulificações.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Qualification, Pageable)}
	 */
	@Override
	public List<Qualification> search(Qualification model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<Qualification> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT qlf.qlf_id, " +
							"		qlf.name, " +
							"		qlf.seq, " +
							"		qlf.required, " +
							"		qlf.active " +
							"FROM qualification qlf " +
							"WHERE qlf.qlf_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] qualification.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new QualificationMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a qualificação.", e );
			throw new AppException( "Erro ao consultar a qualificação.", e );
		}
	}

	@Override
	public Optional<Qualification> save( Qualification model ) throws AppException {
		try {
			String query = "INSERT INTO qualification ( name, seq, required, active ) VALUES ( :name, :seq, :required, :active ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "seq", model.getSeq() );
			params.addValue( "required", model.getRequired() );
			params.addValue( "active", model.getActive() );
	
			log.trace( "[QUERY] qualification.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();

			this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o qualificação: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o qualificação.", e);
		}
	}

	@Override
	public Optional<Qualification> update(Qualification model) throws AppException {
		try {
			String query = "UPDATE qualification SET name=:name, seq=:seq, required=:required, active=:active WHERE qlf_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "seq", model.getSeq() );
			params.addValue( "required", model.getRequired() );
			params.addValue( "active", model.getActive() );
	
			log.trace( "[QUERY] qualification.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o qualificação: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o qualificação.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM qualification WHERE qlf_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] qualification.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o qualificação" , e );
			throw new AppException( "Erro ao excluir o qualificação.", e );
		}
	}
	
	@Override
	public boolean hasPersonRelationship(Integer qlfId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT qlf_id FROM person_qualification WHERE qlf_id = :qlfId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "qlfId", qlfId );

			log.trace( "[QUERY] qualification.hasPersonRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com pessoa." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com pessoa.", e );
		}
	}
}
