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
import com.portal.dao.IPaymentRuleDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PaymentRuleMapper;
import com.portal.model.PaymentRule;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PaymentRuleDAO extends BaseDAO implements IPaymentRuleDAO {

	@Override
	public List<PaymentRule> listAll( Pageable pageble ) throws AppException {
		try {
			if( pageble == null ) {
				pageble = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "name");	
			}
			
			Order order = Order.desc( "name" );
			if( pageble.getSort().get().findFirst().isPresent() ) {
				order = pageble.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT pyr.*, pym.name as pym_name, pym.active as pym_active  " +
							"FROM payment_rule pyr " +
							"INNER JOIN payment_method pym on pyr.pym_id = pym.pym_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageble.getPageSize() + " " +
							"OFFSET " + pageble.getPageNumber();

			log.trace( "[QUERY] payment_rule.listAll: {}", query);

			return this.getJdbcTemplatePortal().query( query, new PaymentRuleMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar as regras de pagamento.", e );
			throw new AppException( "Erro ao listar as regras de pagamento.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<PaymentRule> list() throws AppException {
		return this.listAll(null);
	}

	@Override
	public List<PaymentRule> find(PaymentRule model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pyr_id");	
			}
			
			Order order = Order.asc( "pyr_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pyr.*, pym.name as pym_name, pym.active as pym_active  ");
			query.append("FROM payment_rule pyr ");
			query.append("INNER JOIN payment_method pym on pyr.pym_id = pym.pym_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND pyr.pyr_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND pyr.name = :name " );
					params.addValue("name", model.getName());
					hasFilter = true;
				}
				
				if( model.getActive() != null ) {
					query.append(" AND pyr.active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}

				if( model.getPreApproved() != null ) {
					query.append(" AND pyr.pre_approved = :pre_approved " );
					params.addValue("pre_approved", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}
				
				if( model.getPaymentMethod() != null && model.getPaymentMethod().getId() != null && model.getPaymentMethod().getId() > 0) {
					query.append(" AND pyr.pym_id = :pymId " );
					params.addValue("pymId", model.getPaymentMethod().getId() );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] payment_rule.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PaymentRuleMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as regras de pagamento.", e );
			throw new AppException( "Erro ao buscar as regras de pagamento.", e );
		}
	}
	
	@Override
	public Optional<PaymentRule> find(PaymentRule model) throws AppException {
		List<PaymentRule> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<PaymentRule> search(PaymentRule model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pyr_id");	
			}
			
			Order order = Order.asc( "pyr_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pyr.*, pym.name as pym_name, pym.active as pym_active ");
			query.append("FROM payment_rule pyr ");
			query.append("INNER JOIN payment_method pym on pyr.pym_id = pym.pym_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND pyr.pyr_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getName() != null && !model.getName().equals("")) {
					query.append(" AND pyr.name like :name " );
					params.addValue("name", this.mapLike(model.getName()));
					hasFilter = true;
				}
				
				if( model.getActive() != null ) {
					query.append(" AND pyr.active = :active " );
					params.addValue("active", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}

				if( model.getPreApproved() != null ) {
					query.append(" AND pyr.pre_approved = :pre_approved " );
					params.addValue("pre_approved", PortalNumberUtils.booleanToInt( model.getActive() ) );
					hasFilter = true;
				}
				
				if( model.getPaymentMethod() != null && model.getPaymentMethod().getId() != null && model.getPaymentMethod().getId() > 0) {
					query.append(" AND pyr.pym_id = :pymId " );
					params.addValue("pymId", model.getPaymentMethod().getId() );
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] payment_rule.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PaymentRuleMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as regras de pagamento.", e );
			throw new AppException( "Erro ao buscar as regras de pagamento.", e );
		}
	}
	
	@Override
	public List<PaymentRule> search(PaymentRule model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public List<PaymentRule> listToPaymentMethod(Integer id) throws AppException {
		try{
			String query = "SELECT pyr.*, pym.name as pym_name, pym.active as pym_active " +
					"FROM payment_rule pyr " +
					"INNER JOIN payment_method pym on pyr.pym_id = pym.pym_id " +
					"WHERE pyr.pym_id = :id ";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );
			
			log.trace( "[QUERY] payment_rule.listAll: {}", query);

			return this.getJdbcTemplatePortal().query( query, params, new PaymentRuleMapper() );
		}catch (Exception e){
			log.error( "Erro ao buscar as regras de pagamento.", e );
			throw new AppException( "Erro ao buscar as regras de pagamento.", e );
		}
	}

	@Override
	public Optional<PaymentRule> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT pyr.*, pym.name as pym_name, pym.active as pym_active " +
							"FROM payment_rule pyr " +
							"INNER JOIN payment_method pym on pyr.pym_id = pym.pym_id " +
							"WHERE pyr.pyr_id = :id " +
							"LIMIT 1";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] payment_rule.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PaymentRuleMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a regra de método de pagamento.", e );
			throw new AppException( "Erro ao consultar a regra de método de pagamento.", e );
		}
	}
	
	@Override
	public Optional<PaymentRule> save(PaymentRule model) throws AppException {
		try {
			String query = "INSERT INTO payment_rule ( name, installments, tax, active, pre_approved, pym_id ) " +
						   "VALUES ( :name, :installments, :tax, :active, :pre_approved, :pymId ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "installments", model.getInstallments() );
			params.addValue( "tax", model.getTax() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			params.addValue( "pre_approved", PortalNumberUtils.booleanToInt( model.getPreApproved() ) );
			params.addValue( "pymId", ( model.getPaymentMethod() == null ? null : model.getPaymentMethod().getId() ) );
	
			log.trace( "[QUERY] payment_rule.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a regra de método de pagamento: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a regra de método de pagamento.", e);
		}
	}

	@Override
	public Optional<PaymentRule> update(PaymentRule model) throws AppException {
		try {
			String query = "UPDATE payment_rule SET installments=:installments, tax=:tax, pym_id=:pymId, name=:name, active=:active, pre_approved=:pre_approved WHERE pyr_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "installments", model.getInstallments() );
			params.addValue( "tax", model.getTax() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
			params.addValue( "pre_approved", PortalNumberUtils.booleanToInt( model.getPreApproved() ) );
			params.addValue( "pymId", ( model.getPaymentMethod() == null ? null : model.getPaymentMethod().getId() ) );
	
			log.trace( "[QUERY] payment_rule.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a regra de método de pagamento: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a regra de método de pagamento.", e);
		}
	}
	
	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM payment_rule WHERE pyr_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] payment_rule.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a regra de método de pagamento.", e );
			throw new AppException( "Erro ao excluir a regra de método de pagamento.", e );
		}
	}
}
