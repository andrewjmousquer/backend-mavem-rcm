package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
import com.portal.dao.ISalesOrderDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.SalesOrderMapper;
import com.portal.model.SalesOrder;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SalesOrderDAO extends BaseDAO implements ISalesOrderDAO {
	
	private static final String TABLE_NAME =  "sales_order"; 

	@Override
	public List<SalesOrder> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "sor_id");	
			}
			
			Order order = Order.desc( "sor_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT  so.*, " +
							"statusCla.value as status_cla_value, " +
							"statusCla.type as status_cla_type, " +
							"statusCla.label as status_cla_label, " +
							"riskCla.cla_id as risk_cla_id, " +
							"riskCla.value as risk_cla_value, " +
							"riskCla.type as risk_cla_type, " +
							"riskCla.label as risk_cla_label " +
							"FROM " + TABLE_NAME +  " so " +
							"inner join classifier statusCla on statusCla.cla_id = pps.status_cla_id " +
							"inner join classifier riskCla on riskCla.cla_id = pps.risk_cla_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();
			
			

			log.trace( "[QUERY] SalesOrder.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new SalesOrderMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar as Pedido de venda.", e );
			throw new AppException( "Erro ao listar as Pedido de venda.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<SalesOrder> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<SalesOrder> find(SalesOrder model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "sor_id");	
			}
			
			Order order = Order.asc( "sor_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT so.*, ");
			query.append("	 u.usr_id, " );
			query.append("	 u.username, " );
			query.append("	 u.enabled, " );
			
			query.append("	 p.per_id, " );
			query.append("	 p.name as per_name, " );
			query.append("	 p.job_title, " );
			query.append("	 p.cpf, " );
			query.append("	 p.cnpj, " );
			query.append("	 p.rg, " );
			query.append("	 p.rne, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );
			
			query.append("	 pps.num as pps_num, " );
			query.append("	 pps.cod as pps_cod, " );
			query.append("	 pps.create_date as pps_create_date, " );			
			
			query.append("statusCla.cla_id as status_cla_id, ");
			query.append("statusCla.value as status_cla_value, ");
			query.append("statusCla.type as status_cla_type, ");
			query.append("statusCla.label as status_cla_label ");
			

			query.append("FROM " + TABLE_NAME +  " so ");
			query.append("INNER JOIN " + schemaName + "classifier statusCla on statusCla.cla_id = so.status_cla_id ");
			query.append("INNER JOIN " + schemaName + "proposal pps ON pps.pps_id = so.pps_id ");
			query.append("INNER JOIN " + schemaName + "user u ON u.usr_id = so.usr_id ");
			query.append("INNER JOIN " + schemaName + "person as p ON p.per_id = u.per_id ");
			query.append("INNER JOIN " + schemaName + "classifier typ ON p.classification_cla_id = typ.cla_id " );
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND so.sor_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
				
				if( model.getStatus() != null ) {
					query.append(" AND so.status_cla_id = :status ");
					params.addValue("status", model.getStatus().getType().getId());
					hasFilter = true;
				}
				
				if( model.getUser() != null && model.getUser().getId() > 0 ) {
					query.append(" AND so.usr_id = :userID ");
					params.addValue("userID", model.getUser().getId());
					hasFilter = true;
				}
				
				if( model.getProposal() != null && model.getProposal().getId() > 0 ) {
					query.append(" AND so.pps_id = :proposalID ");
					params.addValue("proposalID", model.getProposal().getId());
					hasFilter = true;
				}

				if( StringUtils.isNotBlank(model.getJiraKey()) ) {
					query.append(" AND so.jira_key = :jiraKEY ");
					params.addValue("jiraKEY", model.getJiraKey());
					hasFilter = true;
				}
				
				if(model.getOrderNumber() != null && model.getOrderNumber() > 0) {
					query.append(" AND so.order_number = :orderNumber ");
					params.addValue("orderNumber", model.getOrderNumber());
					hasFilter = true;
				}
				

			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] SalesOrder.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new SalesOrderMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as Pedido de venda.", e );
			throw new AppException( "Erro ao buscar as Pedido de venda.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(SalesOrder, Pageable)}
	 */
	@Override
	public Optional<SalesOrder> find(SalesOrder model) throws AppException {
		List<SalesOrder> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	/**
	 * @deprecated Usar a função {@link #search(SalesOrder, Pageable)}
	 */
	@Override
	public List<SalesOrder> search(SalesOrder model) throws AppException {
		return this.find(model, null);
	}

	@Override
	public Optional<SalesOrder> findByProposal(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();

			query.append("SELECT so.*, ");
			query.append("	 u.usr_id, " );
			query.append("	 u.username, " );
			query.append("	 u.enabled, " );

			query.append("	 p.per_id, " );
			query.append("	 p.name as per_name, " );
			query.append("	 p.job_title, " );
			query.append("	 p.cpf, " );
			query.append("	 p.cnpj, " );
			query.append("	 p.rg, " );
			query.append("	 p.rne, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );

			query.append("	 pps.num as pps_num, " );
			query.append("	 pps.cod as pps_cod, " );
			query.append("	 pps.create_date as pps_create_date, " );

			query.append("statusCla.cla_id as status_cla_id, ");
			query.append("statusCla.value as status_cla_value, ");
			query.append("statusCla.type as status_cla_type, ");
			query.append("statusCla.label as status_cla_label ");


			query.append("FROM " + TABLE_NAME +  " so ");
			query.append("INNER JOIN " + schemaName + "classifier statusCla on statusCla.cla_id = so.status_cla_id ");
			query.append("INNER JOIN " + schemaName + "proposal pps ON pps.pps_id = so.pps_id ");
			query.append("INNER JOIN " + schemaName + "user u ON u.usr_id = so.usr_id ");
			query.append("INNER JOIN " + schemaName + "person as p ON p.per_id = u.per_id ");
			query.append("INNER JOIN " + schemaName + "classifier typ ON p.classification_cla_id = typ.cla_id " );
			query.append("WHERE so.pps_id = :id ");
			query.append("LIMIT 1 ");

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] SalesOrder.findByProposal: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new SalesOrderMapper() ) );

		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();

		} catch (Exception e) {
			log.error( "Erro ao consultar a Pedido de compra.", e );
			throw new AppException( "Erro ao consultar a Pedido de compra.", e );
		}
	}

	@Override
	public Optional<SalesOrder> getById(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT so.*, ");
			query.append("	 u.usr_id, " );
			query.append("	 u.username, " );
			query.append("	 u.enabled, " );
			
			query.append("	 p.per_id, " );
			query.append("	 p.name as per_name, " );
			query.append("	 p.job_title, " );
			query.append("	 p.cpf, " );
			query.append("	 p.cnpj, " );
			query.append("	 p.rg, " );
			query.append("	 p.rne, " );
			query.append("	 typ.cla_id as per_cla_id, " );
			query.append("	 typ.value as per_cla_value, " );
			query.append("	 typ.type as per_cla_type, " );
			query.append("	 typ.label as per_cla_label, " );
			
			query.append("	 pps.num as pps_num, " );
			query.append("	 pps.cod as pps_cod, " );
			query.append("	 pps.create_date as pps_create_date, " );			
			
			query.append("statusCla.cla_id as status_cla_id, ");
			query.append("statusCla.value as status_cla_value, ");
			query.append("statusCla.type as status_cla_type, ");
			query.append("statusCla.label as status_cla_label ");
			

			query.append("FROM " + TABLE_NAME +  " so ");
			query.append("INNER JOIN " + schemaName + "classifier statusCla on statusCla.cla_id = so.status_cla_id ");
			query.append("INNER JOIN " + schemaName + "proposal pps ON pps.pps_id = so.pps_id ");
			query.append("INNER JOIN " + schemaName + "user u ON u.usr_id = so.usr_id ");
			query.append("INNER JOIN " + schemaName + "person as p ON p.per_id = u.per_id ");
			query.append("INNER JOIN " + schemaName + "classifier typ ON p.classification_cla_id = typ.cla_id " );
			query.append("WHERE so.sor_id = :id ");
			query.append("LIMIT 1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] SalesOrder.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new SalesOrderMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a Pedido de compra.", e );
			throw new AppException( "Erro ao consultar a Pedido de compra.", e );
		}
	}

	@Override
	public Optional<SalesOrder> save(SalesOrder model) throws AppException {
		try {
			
			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO "+ schemaName + TABLE_NAME + "(pps_id,order_number,jira_key,status_cla_id,usr_id)");
			query.append("VALUES (:proposalID, fnGetOrderNumberSalesOrder() , :jiraKey ,:status ,:userID)");
			
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			params.addValue("proposalID", model.getProposal().getId());
			params.addValue("jiraKey", StringUtils.isBlank(model.getJiraKey()) ? "AGUARDANDO" : model.getJiraKey());
			params.addValue("status", model.getStatus().getType().getId());
			params.addValue("userID", model.getUser().getId());
	
			log.trace( "[QUERY] SalesOrder.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a Pedido de compra: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a Pedido de compra.", e);
		}
	}

	@Override
	public Optional<SalesOrder> update(SalesOrder model) throws AppException {
		try {
			
			StringBuilder query = new StringBuilder();
			query.append("UPDATE " + TABLE_NAME +" SET ");
			query.append(" pps_id = :proposalID ");
			query.append(",jira_key = :jiraKey ");
			query.append(",status_cla_id = :status ");
			query.append(",usr_id = :userID ");			
			query.append("WHERE sor_id = :id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId() );
			params.addValue("proposalID", model.getProposal().getId());
			params.addValue("jiraKey", model.getJiraKey());
			params.addValue("status", model.getStatus().getType().getId());
			params.addValue("userID", model.getUser().getId());
	
			log.trace( "[QUERY] SalesOrder.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a Pedido de compra: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a Pedido de compra.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM " + TABLE_NAME + " WHERE sor_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] SalesOrder.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a Pedido de compra" , e );
			throw new AppException( "Erro ao excluir a Pedido de compra.", e );
		}
	}
}
