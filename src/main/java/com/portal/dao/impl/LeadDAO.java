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
import com.portal.dao.ILeadDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.LeadMapper;
import com.portal.model.Lead;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class LeadDAO extends BaseDAO implements ILeadDAO {

	@Override
	public List<Lead> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id");	
			}
			
			Order order = Order.desc( "led_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT  led.*, " +
							"		 src.name AS source_name, " +
							"		 src.active AS source_active, " +
							"        mdl.mdl_id AS mdl_id, " +
							"        mdl.name AS model_name, " +
							"        brd.brd_id AS brand_id, " +
							"        brd.name AS brand_name, " +
							"        per.per_id AS seller_per_id, " +
							"        per.name AS seller_name " +
							"FROM " + schemaName + "lead led " +
							"INNER JOIN " + schemaName + "seller sel ON sel.sel_id = led.seller_id " +
							"INNER JOIN " + schemaName + "source src ON src.src_id = led.src_id " +
							"INNER JOIN " + schemaName + "model mdl ON mdl.mdl_id = led.mdl_id " +
							"INNER JOIN " + schemaName + "brand brd ON brd.brd_id = mdl.brd_id " +
							"INNER JOIN " + schemaName + "person per ON per.per_id = sel.per_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] lead.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new LeadMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os leads.", e );
			throw new AppException( "Erro ao listar os leads.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Lead> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Lead> find(Lead model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "led_id");	
			}
			
			Order order = Order.asc( "led_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT led.*, ");
			query.append("		 src.name AS source_name, ");
			query.append("		 src.active AS source_active, ");
			query.append("		 mdl.mdl_id AS mdl_id, ");
			query.append("		 mdl.name AS model_name, ");
			query.append("       brd.brd_id AS brand_id, ");
			query.append("       brd.name AS brand_name, ");
			query.append("       per.per_id AS seller_per_id, ");
			query.append("       per.name AS seller_name ");
			query.append("FROM " + schemaName + "lead led ");
			query.append("INNER JOIN " + schemaName + "seller sel ON sel.sel_id = led.seller_id ");
			query.append("INNER JOIN " + schemaName + "source src ON src.src_id = led.src_id ");
			query.append("INNER JOIN " + schemaName + "model mdl ON mdl.mdl_id = led.mdl_id ");
			query.append("INNER JOIN " + schemaName + "brand brd ON brd.brd_id = mdl.brd_id ");
			query.append("INNER JOIN " + schemaName + "person per ON per.per_id = sel.per_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {				
				if( hasTextSearch(model) )  {
					query.append(" AND (");
					query.append("	led.name LIKE CONCAT('%', :name, '%')");
					query.append("	OR led.email LIKE CONCAT('%', :email, '%')");
					query.append("	OR led.phone LIKE CONCAT('%', :phone, '%') ");
					query.append(") ");
					
					params.addValue("name", model.getName());
					params.addValue("email", model.getEmail());
					params.addValue("phone", model.getPhone());
					hasFilter = true;
				}
				
				if(model.getDescription() != null && !model.getDescription().isEmpty()) {
					query.append(" AND led.description LIKE CONCAT('%', :description, '%') ");
					params.addValue("description", model.getDescription());
					hasFilter = true;
				}
				
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND led.led_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if( model.getCreateDate() != null ) {
					query.append(" AND led.create_date = :createDate ");
					params.addValue("createDate", PortalTimeUtils.localDateTimeFormat( model.getCreateDate(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				}
				
				if( model.getClient() != null && model.getClient().getId() != null ) {
					query.append(" AND led.client_per_id = :clientId ");
					params.addValue("clientId", model.getClient().getId());
					hasFilter = true;
				}

				if( model.getSeller() != null && model.getSeller().getId() != null ) {
					query.append(" AND led.seller_id = :sellerId ");
					params.addValue("sellerId", model.getSeller().getId());
					hasFilter = true;
				}
				
				if( model.getSource() != null && model.getSource().getId() != null ) {
					query.append(" AND led.src_id = :sourceId ");
					params.addValue("sourceId", model.getSource().getId());
					hasFilter = true;
				}
				
				if( model.getStatus() != null ) {
					query.append(" AND led.status_cla_id = :status ");
					params.addValue("status", model.getStatus().getId());
					hasFilter = true;
				}
				
				if( model.getModel() != null && model.getModel().getId() != null ) {
					query.append(" AND led.mdl_id = :modelId ");
					params.addValue("modelId", model.getModel().getId());
					hasFilter = true;
				}
				
				if( model.getSaleProbabilty() != null ) {
					query.append(" AND led.sale_probability_cla_id = :saleProbability ");
					params.addValue("saleProbability", model.getSaleProbabilty().getId());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] lead.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new LeadMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os leads.", e );
			throw new AppException( "Erro ao buscar os leads.", e );
		}
	}

	private boolean hasTextSearch(Lead model) {
		return model.getName() != null && !model.getName().isEmpty() 
				&& model.getEmail() != null && !model.getEmail().isEmpty()
				&& model.getPhone() != null && !model.getPhone().isEmpty()
				&& model.getName().equals(model.getEmail()) && model.getEmail().equals(model.getPhone());
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Lead, Pageable)}
	 */
	@Override
	public Optional<Lead> find(Lead model) throws AppException {
		List<Lead> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	/**
	 * @deprecated Usar a função
	 */
	@Override
	public List<Lead> search(Lead model) throws AppException {
		return this.find(model, null);
	}

	@Override
	public Optional<Lead> getById(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT led.*, ");
			query.append("		 src.name AS source_name, ");
			query.append("		 src.active AS source_active, ");
			query.append("		 mdl.mdl_id AS mdl_id, ");
			query.append("		 mdl.name AS model_name, ");
			query.append("       brd.brd_id AS brand_id, ");
			query.append("       brd.name AS brand_name, ");
			query.append("       per.per_id AS seller_per_id, ");
			query.append("       per.name AS seller_name ");
			query.append("FROM " + schemaName + "lead led ");
			query.append("INNER JOIN " + schemaName + "seller sel ON sel.sel_id = led.seller_id ");
			query.append("INNER JOIN " + schemaName + "source src ON src.src_id = led.src_id ");
			query.append("INNER JOIN " + schemaName + "model mdl ON mdl.mdl_id = led.mdl_id ");
			query.append("INNER JOIN " + schemaName + "brand brd ON brd.brd_id = mdl.brd_id ");
			query.append("INNER JOIN " + schemaName + "person per ON per.per_id = sel.per_id ");
			query.append("WHERE led.led_id = :id ");
			query.append("LIMIT 1");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] lead.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new LeadMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a lead.", e );
			throw new AppException( "Erro ao consultar a lead.", e );
		}
	}

	@Override
	public Optional<Lead> save(Lead model) throws AppException {
		try {
			String query = "INSERT INTO " + schemaName + "lead (led_id, name, create_date, email, phone, seller_id, client_per_id, "
					+ "src_id, status_cla_id, sale_probability_cla_id, mdl_id, subject, description) " +
					"VALUES (NULL, :name, :createDate, :email, :phone, :sellerId, :clientId, "
					+ ":srcId, :statusId, :saleProbability, :mdlId, :subject, :description)";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("name", model.getName());
			params.addValue("createDate", PortalTimeUtils.localDateTimeFormat(model.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
			params.addValue("email", model.getEmail());
			params.addValue("phone", model.getPhone());
			params.addValue("sellerId", (model.getSeller() != null ? model.getSeller().getId() : null));
			params.addValue("clientId", (model.getClient() != null ? model.getClient().getId() : null));			
			params.addValue("srcId", (model.getSource() != null ? model.getSource().getId() : null));
			params.addValue("statusId", (model.getStatus() != null ? model.getStatus().getId() : null));				
			params.addValue("saleProbability", ( model.getSaleProbabilty() != null ? model.getSaleProbabilty().getId() : null ));
			params.addValue("mdlId", (model.getModel() != null ? model.getModel().getId() : null));					
			params.addValue("subject", ( model.getSubject() != null ? model.getSubject() : null ));
			params.addValue("description", model.getDescription());
	
			log.trace( "[QUERY] lead.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a lead: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a lead.", e);
		}
	}

	@Override
	public Optional<Lead> update(Lead model) throws AppException {
		try {
			String query = "UPDATE " + schemaName + "lead SET "
					+ "name=:name, "
					+ "create_date=:createDate, "
					+ "email=:email, "
					+ "phone=:phone, "
					+ "seller_id=:sellerId, "
					+ "client_per_id=:clientId, "					
					+ "src_id=:sourceId, "
					+ "status_cla_id=:status, "
					+ "sale_probability_cla_id=:saleProbability, "
					+ "mdl_id=:modelId, "
					+ "subject=:subject, "
					+ "description=:description "
					+ "WHERE led_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId() );
			params.addValue("name", model.getName());
			params.addValue("createDate", PortalTimeUtils.localDateTimeFormat( model.getCreateDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue("email", model.getEmail());
			params.addValue("phone", model.getPhone());			
			params.addValue("sellerId", ( model.getSeller() != null ? model.getSeller().getId() : null ));
			params.addValue("clientId", ( model.getClient() != null ? model.getClient().getId() : null ));			
			params.addValue("sourceId", ( model.getSource() != null ? model.getSource().getId() : null ));
			params.addValue("status", ( model.getStatus() != null ? model.getStatus().getId() : null ));
			params.addValue("modelId", ( model.getModel() != null ? model.getModel().getId() : null ));
			params.addValue("saleProbability", ( model.getSaleProbabilty() != null ? model.getSaleProbabilty().getId() : null ));
			params.addValue("subject", ( model.getSubject() != null ? model.getSubject() : null ));
			params.addValue("description", model.getDescription());
			
			log.trace( "[QUERY] lead.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a lead: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a lead.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM " + schemaName + "lead WHERE led_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] lead.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a lead" , e );
			throw new AppException( "Erro ao excluir a lead.", e );
		}

	}

	@Override
	public boolean hasProposalRelationship(Integer ledId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT led_id FROM proposal WHERE led_id = :ledId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ledId", ledId );

			log.trace( "[QUERY] lead.hasProposalRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com proposta." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com proposta.", e );
		}
	}
}
