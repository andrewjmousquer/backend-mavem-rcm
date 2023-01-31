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
import com.portal.dao.IPaymentMethodDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PaymentMethodMapper;
import com.portal.model.PaymentMethod;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PaymentMethodDAO extends BaseDAO implements IPaymentMethodDAO {

	@Override
	public List<PaymentMethod> listAll( Pageable pageable ) throws AppException {
		try {
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "name");	
			}
			
			Order order = Order.desc( "name" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT * " +
							"FROM payment_method pym " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] payment_method.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new PaymentMethodMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os métodos de pagamento.", e );
			throw new AppException( "Erro ao listar os métodos de pagamento.", e );
		}
	}

	@Override
	public Optional<PaymentMethod> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM payment_method pym " +
							"WHERE pym.pym_id = :id " +
							"LIMIT 1";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] payment_method.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PaymentMethodMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o método de pagamento.", e );
			throw new AppException( "Erro ao consultar o método de pagamento.", e );
		}
	}
	
	@Override
	public Optional<PaymentMethod> save(PaymentMethod model) throws AppException {
		try {
			String query = "INSERT INTO payment_method ( name, active ) VALUES ( :name, :active ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
	
			log.trace( "[QUERY] payment_method.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o método de pagamento: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o método de pagamento.", e);
		}
	}

	@Override
	public Optional<PaymentMethod> update(PaymentMethod model) throws AppException {
		try {
			String query = "UPDATE payment_method SET name=:name, active=:active WHERE pym_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "name", model.getName().toUpperCase() );
			params.addValue( "active", PortalNumberUtils.booleanToInt( model.getActive() ) );
	
			log.trace( "[QUERY] payment_method.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o método de pagamento: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o método de pagamento.", e);
		}
	}
	
	@Override
	public void delete(Integer id) throws AppException {

		try {
			String query = 	"DELETE FROM payment_method WHERE pym_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] payment_method.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o método de pagamento.", e );
			throw new AppException( "Erro ao excluir o método de pagamento.", e );
		}
		
	}
	
	@Override
	public List<PaymentMethod> find(PaymentMethod model, Pageable pageable) throws AppException {
		
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pym_id");	
			}
			
			Order order = Order.asc( "pym_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM payment_method pym ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND pym.pym_id = :id ");
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
			
			log.trace( "[QUERY] payment_method.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PaymentMethodMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os métodos de pagamento.", e );
			throw new AppException( "Erro ao buscar os métodos de pagamento.", e );
		}
	}
	
	@Override
	public List<PaymentMethod> search(PaymentMethod model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pym_id");	
			}
			
			Order order = Order.asc( "pym_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM payment_method pym ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if(model.getId() != null && model.getId() > 0) {
					query.append(" AND pym.pym_id = :id ");
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
			
			log.trace( "[QUERY] payment_method.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PaymentMethodMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os métodos de pagamento.", e );
			throw new AppException( "Erro ao procurar os métodos de pagamento.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(PaymentMethod, Pageable)}
	 */
	@Override
	public Optional<PaymentMethod> find( PaymentMethod model ) throws AppException {
		List<PaymentMethod> brands = this.find( model, null );
		return Optional.ofNullable( ( brands != null ? brands.get(0) : null ) ); 
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	@Deprecated
	public List<PaymentMethod> list() throws AppException {
		return this.listAll(null);
	}

	/**
	 * @deprecated Usar a função {@link #search(PaymentMethod, Pageable)}
	 */
	@Override
	public List<PaymentMethod> search(PaymentMethod model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public boolean hasProposalRelationship(Integer pymId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT pym_id FROM proposal_payment WHERE pym_id = :pymId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "pymId", pymId );

			log.trace( "[QUERY] payment_method.hasProposalRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com a proposta." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com a proposta.", e );
		}
	}
}
