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
import com.portal.dao.IPersonRelatedDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PersonRelatedMapper;
import com.portal.model.PersonRelated;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PersonRelatedDAO extends BaseDAO implements IPersonRelatedDAO {

	@Override
	public List<PersonRelated> listAll( Pageable pageable ) throws AppException {
		try {
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "psr_id");	
			}
			
			Order order = Order.desc( "psr_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT psr.*, " +
							"	 typ.cla_id as per_cla_id, " +
							"	 typ.value as per_cla_value, " +
							"	 typ.type as per_cla_type, " +
							"	 typ.label as per_cla_label " +
							"FROM person_related psr " +
				            "INNER JOIN classifier typ ON psr.type_cla_id = typ.cla_id " + 
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] person_related.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new PersonRelatedMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os relacionamentos da pessoa.", e );
			throw new AppException( "Erro ao listar os relacionamentos da pessoa.", e );
		}
	}

	@Override
	public Optional<PersonRelated> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT psr.*, " +
							"	 typ.cla_id as per_cla_id, " +
							"	 typ.value as per_cla_value, " +
							"	 typ.type as per_cla_type, " +
							"	 typ.label as per_cla_label " +
							"FROM person_related psr " +
				            "INNER JOIN classifier typ ON psr.type_cla_id = typ.cla_id " + 
							"WHERE psr.psr_id = :id " +
							"LIMIT 1";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] person_related.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PersonRelatedMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o relacionamento com a pessoa.", e );
			throw new AppException( "Erro ao consultar o relacionamento com a pessoa.", e );
		}
	}
	
	@Override
	public Optional<PersonRelated> save(PersonRelated model) throws AppException {
		try {
			String query = "INSERT INTO person_related ( name, birthdate, type_cla_id, per_id ) VALUES ( :name, :birthdate, :relatedType, :perId ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName() );
			params.addValue( "birthdate", PortalTimeUtils.localDateFormat( model.getBirthdate(), "yyyy-MM-dd" ) );
			params.addValue( "relatedType", model.getRelatedType().getId() );
			params.addValue( "perId" , ( model.getPerson() != null ? model.getPerson().getId() : null ) );
	
			log.trace( "[QUERY] person_related.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o relacionamento com a pessoa: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o relacionamento com a pessoa.", e);
		}
	}

	@Override
	public Optional<PersonRelated> update(PersonRelated model) throws AppException {
		try {
			String query = "UPDATE person_related SET name=:name, birthdate=:birthdate, type_cla_id=:relatedType, per_id=:perId WHERE psr_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName() );
			params.addValue( "birthdate", PortalTimeUtils.localDateFormat( model.getBirthdate(), "yyyy-MM-dd" ) );
			params.addValue( "relatedType", model.getRelatedType().getId() );
			params.addValue( "perId" , ( model.getPerson() != null ? model.getPerson().getId() : null ) );
	
			log.trace( "[QUERY] person_related.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o relacionamento com a pessoa: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o relacionamento com a pessoa.", e);
		}
	}
	
	@Override
	public void delete(Integer id) throws AppException {

		try {
			String query = "DELETE FROM person_related WHERE psr_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] person_related.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o relacionamento com a pessoa.", e );
			throw new AppException( "Erro ao excluir o relacionamento com a pessoa.", e );
		}
		
	}
	
	@Override
	public List<PersonRelated> find(PersonRelated model, Pageable pageable) throws AppException {
		
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "psr_id");	
			}
			
			Order order = Order.asc( "psr_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT psr.*, ");
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label " );
			query.append("FROM person_related psr ");
			query.append("INNER JOIN classifier typ ON psr.type_cla_id = typ.cla_id "); 
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND psr.psr_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND psr.name = :name " );
					params.addValue("name", model.getName());
					hasFilter = true;
				}
				
				if( model.getRelatedType() != null ) {
					query.append(" AND psr.type_cla_id = :relatedType ");
					params.addValue("relatedType", model.getRelatedType().getId());
					hasFilter = true;
				}

				if (model.getBirthdate() != null) {
					query.append(" AND psr.birthdate = :birthdate ");
					params.addValue("birthdate", PortalTimeUtils.localDateFormat(model.getBirthdate(), "yyyy-MM-dd"));
					hasFilter = true;
				}

				if (model.getPerson() != null) {
					query.append(" AND psr.per_id = :per_id ");
					params.addValue("per_id", model.getPerson().getId());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] person_related.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PersonRelatedMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os relacionamentos da pessoa.", e );
			throw new AppException( "Erro ao buscar os relacionamentos da pessoa.", e );
		}
	}
	
	@Override
	public List<PersonRelated> search(PersonRelated model, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "psr_id");	
			}
			
			Order order = Order.asc( "psr_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT psr.*, ");
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label " );
			query.append("FROM person_related psr ");
			query.append("INNER JOIN classifier typ ON psr.type_cla_id = typ.cla_id ");
			query.append("WHERE psr.psr_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND psr.psr_id = :id ");
					params.addValue("id", model.getId());
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND psr.name LIKE :name " );
					params.addValue("name", this.mapLike(model.getName()));
				}
				
				if( model.getRelatedType() != null ) {
					query.append(" AND psr.type_cla_id = :relatedType " );
					params.addValue("relatedType", model.getRelatedType().getId() );
				}

				if( model.getBirthdate() != null ) {
					query.append(" AND psr.birthdate = :birthdate " );
					params.addValue( "birthdate", PortalTimeUtils.localDateFormat( model.getBirthdate(), "yyyy-MM-dd" ) );
				}
				
				if( model.getPerson() != null && model.getPerson().getId() != null && model.getPerson().getId() > 0) {
					query.append(" AND psr.per_id = :perId " );
					params.addValue("perId", model.getPerson().getId() );
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			log.trace( "[QUERY] person_related.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PersonRelatedMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os relacionamentos da pessoa.", e );
			throw new AppException( "Erro ao procurar os relacionamentos da pessoa.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(PersonRelated, Pageable)}
	 */
	@Override
	public Optional<PersonRelated> find( PersonRelated model ) throws AppException {
		List<PersonRelated> brands = this.find( model, null );
		return Optional.ofNullable( ( brands != null ? brands.get(0) : null ) ); 
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	@Deprecated
	public List<PersonRelated> list() throws AppException {
		return this.listAll(null);
	}

	/**
	 * @deprecated Usar a função {@link #search(PersonRelated, Pageable)}
	 */
	@Override
	public List<PersonRelated> search(PersonRelated model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public List<PersonRelated> findByPerson(Integer id) throws AppException {
		List<PersonRelated> personRelatedList = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT psr.*, ");
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label " );
			query.append(" FROM person_related as psr ");
			query.append(" INNER JOIN classifier typ ON psr.type_cla_id = typ.cla_id ");
			query.append(" WHERE psr.per_id = :id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", id);

			personRelatedList = this.getJdbcTemplatePortal().query(query.toString(), params, new PersonRelatedMapper());
		} catch (Exception e) {
			log.error("Erro ao procurar os relacionamentos da pessoa.", e);
			throw new AppException("Erro ao procurar os relacionamentos da pessoa.", e);
		}
		return personRelatedList;
	}
}
