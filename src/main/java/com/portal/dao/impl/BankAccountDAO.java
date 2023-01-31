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
import com.portal.dao.IBankAccountDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.BankAccountMapper;
import com.portal.model.BankAccount;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class BankAccountDAO extends BaseDAO implements IBankAccountDAO {

	@Override
	public List<BankAccount> listAll(Pageable pageable) throws AppException {
		try {
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id");	
			}
			
			Order order = Order.desc( "act_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT * " +
							"FROM bank_account act " +
							"INNER JOIN bank bnk ON bnk.bnk_id = act.bnk_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] bankAccount.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new BankAccountMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar as contas bancárias.", e );
			throw new AppException( "Erro ao listar as contas bancárias.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<BankAccount> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<BankAccount> find(BankAccount model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id");	
			}
			
			Order order = Order.asc( "act_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM bank_account act ");
			query.append("INNER JOIN bank bnk ON bnk.bnk_id = act.bnk_id " );
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND act.act_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getAgency() != null && !model.getAgency().equals("")) {
					query.append(" AND act.agency = :agency " );
					params.addValue("agency", model.getAgency());
					hasFilter = true;
				}
				
				if(model.getAccountNumber() != null && !model.getAccountNumber().equals("")) {
					query.append(" AND act.account_number = :accountNumber " );
					params.addValue("accountNumber", model.getAccountNumber());
					hasFilter = true;
				}

				if(model.getPixKey() != null && !model.getPixKey().equals("")) {
					query.append(" AND act.pix_key = :pixKey " );
					params.addValue("pixKey", model.getPixKey());
					hasFilter = true;
				}
				
				if(model.getBank() != null && model.getBank().getId() != null ) {
					query.append(" AND act.bnk_id = :bankId " );
					params.addValue("bankId", model.getBank().getId());
					hasFilter = true;
				}
				
				if( model.getType() != null ) {
					query.append(" AND act.type_cla_id = :type " );
					params.addValue("type", model.getType().getType().getId() );
					hasFilter = true;
				}
				
				if(model.getPerson() != null && model.getPerson().getId() != null ) {
					query.append(" AND act.per_id = :perId " );
					params.addValue("perId", model.getPerson().getId());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] bankAccount.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new BankAccountMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as contas bancárias.", e );
			throw new AppException( "Erro ao buscar as contas bancárias.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(BankAccount, Pageable)}
	 */
	@Override
	public Optional<BankAccount> find(BankAccount model) throws AppException {
		List<BankAccount> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<BankAccount> search(BankAccount model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "act_id");	
			}
			
			Order order = Order.asc( "act_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT * ");
			query.append("FROM bank_account act ");
			query.append("INNER JOIN bank bnk ON bnk.bnk_id = act.bnk_id " );
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND act.act_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getAgency() != null && !model.getAgency().equals("")) {
					query.append(" AND act.agency LIKE :agency " );
					params.addValue("agency", this.mapLike(model.getAgency()));
					hasFilter = true;
				}
				
				if(model.getAccountNumber() != null && !model.getAccountNumber().equals("")) {
					query.append(" AND act.account_number LIKE :accountNumber " );
					params.addValue("accountNumber", this.mapLike(model.getAccountNumber()));
					hasFilter = true;
				}

				if(model.getPixKey() != null && !model.getPixKey().equals("")) {
					query.append(" AND act.pix_key LIKE :pixKey " );
					params.addValue("pixKey", this.mapLike(model.getPixKey()));
					hasFilter = true;
				}
				
				if(model.getBank() != null && model.getBank().getId() != null ) {
					query.append(" AND act.bnk_id = :bankId " );
					params.addValue("bankId", model.getBank().getId());
					hasFilter = true;
				}
				
				if( model.getType() != null ) {
					query.append(" AND act.type_cla_id = :type " );
					params.addValue("type", model.getType().getType().getId() );
					hasFilter = true;
				}
				
				if(model.getPerson() != null && model.getPerson().getId() != null ) {
					query.append(" AND act.per_id = :perId " );
					params.addValue("perId", model.getPerson().getId());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] bankAccount.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new BankAccountMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar as contas bancárias.", e );
			throw new AppException( "Erro ao buscar as contas bancárias.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(BankAccount, Pageable)}
	 */
	@Override
	public List<BankAccount> search(BankAccount model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<BankAccount> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM bank_account act " +
							"INNER JOIN bank bnk ON bnk.bnk_id = act.bnk_id " +
							"WHERE act_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] bankAccount.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new BankAccountMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar a conta bancária.", e );
			throw new AppException( "Erro ao consultar a conta bancária.", e );
		}
	}

	@Override
	public Optional<BankAccount> save(BankAccount model) throws AppException {
		try {
			String query = "INSERT INTO bank_account ( agency, account_number, pix_key, type_cla_id, bnk_id, per_id ) " +
					 	   "VALUES ( :agency, :accountNumber, :pixKey, :typeClaId, :bnkId, :perId ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "agency", model.getAgency() );
			params.addValue( "accountNumber", model.getAccountNumber() );
			params.addValue( "pixKey", model.getPixKey() );
			params.addValue( "typeClaId", model.getType().getType().getId() );
			params.addValue( "bnkId", model.getBank().getId() );
			params.addValue( "perId", model.getPerson().getId() );
	
			log.trace( "[QUERY] bankAccount.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar a conta bancária: {}", model, e );
			throw new AppException( "Erro ao tentar salvar a conta bancária.", e);
		}
	}

	@Override
	public Optional<BankAccount> update(BankAccount model) throws AppException {
		try {
			String query = "UPDATE bank_account " +
					 	   "SET agency=:agency, account_number=:accountNumber, pix_key=:pixKey, type_cla_id=:type, bnk_id=:bnkId, per_id=:perId " +
					 	   "WHERE act_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "agency", model.getAgency() );
			params.addValue( "accountNumber", model.getAccountNumber() );
			params.addValue( "pixKey", model.getPixKey() );
			params.addValue( "type", model.getType().getType().getId() );
			params.addValue( "bnkId", model.getBank().getId() );
			params.addValue( "perId", model.getPerson().getId() );
			
			log.trace( "[QUERY] bankAccount.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar a conta bancária: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar a conta bancária.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM bank_account WHERE act_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] bankAccount.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir a conta bancária" , e );
			throw new AppException( "Erro ao excluir a conta bancária.", e );
		}
	}

}
