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
import com.portal.dao.IProposalCommissionDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalCommisionMapper;
import com.portal.model.ProposalCommission;
import com.portal.model.ProposalDetail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalCommissionDAO extends BaseDAO implements IProposalCommissionDAO {

	@Override
	public List<ProposalCommission> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");	
			}
			
			Order order = Order.desc( "ppd_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
		
			String query = 	"SELECT  pcm.*, " +
							"	 type.cla_id as cla_id, " +
							"	 type.value as _cla_value, " +
							"	 type.type as cla_type, " +
							"	 type.label as cla_label, " +
							"FROM proposal_commission as pcm " +
							"INNER JOIN classifier as type ON pcm.type_cla_id = type.cla_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] proposalCommission.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ProposalCommisionMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar comissao do detalhe da proposta.", e );
			throw new AppException( "Erro ao listar comissao do detalhe da proposta.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<ProposalCommission> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<ProposalCommission> find(ProposalCommission model, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");	
			}
			
			Order order = Order.asc( "ppd_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pcm.*, ");
			query.append("		 type.cla_id as cla_id, " );
			query.append("		 type.value as cla_value, " );
			query.append("		 type.type as cla_type, " );
			query.append("		 type.label as cla_label " );
			query.append("FROM proposal_commission as pcm ");
			query.append("INNER JOIN classifier as type ON pcm.type_cla_id = type.cla_id " );
			query.append("WHERE 1=1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND pcm.pcm_id = :id ");
					params.addValue("id", model.getId());
				}
		
				if( model.getPerson() != null && model.getPerson().getId() != null ) {
					query.append(" AND pcm.per_id = :perId ");
					params.addValue("perId", model.getPerson().getId() );
				}
				
				if( model.getProposalDetail() != null && model.getProposalDetail().getId() != null ) {
					query.append(" AND pcm.ppd_id = :ppdId ");
					params.addValue("ppdId", model.getProposalDetail().getId() );
				}
				
				if( model.getBankAccount() != null && model.getBankAccount().getId() != null ) {
					query.append(" AND pcm.act_id = :actId ");
					params.addValue("actId", model.getBankAccount().getId() );
				}
				
				
				if(model.getCommissionType() != null && model.getCommissionType().getId() != null) {
					query.append(" AND pcm.type_cla_id = :typeClaId ");
					params.addValue("typeClaId", model.getCommissionType().getId() );
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			log.trace( "[QUERY] proposalCommission.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalCommisionMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar ocomissao da proposta.", e );
			throw new AppException( "Erro ao buscar comissao da proposta.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(ProposalDetail, Pageable)}
	 */
	@Override
	public Optional<ProposalCommission> find(ProposalCommission model) throws AppException {
		List<ProposalCommission> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	/**
	 * @deprecated Usar a função {@link #search(ProposalDetail, Pageable)}
	 */
	@Override
	public List<ProposalCommission> search(ProposalCommission model) throws AppException {
		return this.find(model, null);
	}

	@Override
	public Optional<ProposalCommission> getById(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT pcm.*, ");
			query.append("		 type.cla_id as cla_id, " );
			query.append("		 type.value as _cla_value, " );
			query.append("		 type.type as cla_type, " );
			query.append("		 type.label as cla_label " );
			query.append("FROM proposal_commission as pcm ");
			query.append("INNER JOIN classifier as type ON pcm.type_cla_id = type.cla_id " );
			query.append("pcm.pcm_id = :id ");

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposalCommission.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalCommisionMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar comissao da proposta.", e );
			throw new AppException( "Erro ao consultar comissao da proposta.", e );
		}
	}

	@Override
	public Optional<ProposalCommission> save(ProposalCommission model) throws AppException {
		try {
			String query = "INSERT INTO proposal_commission(per_id, due_date, value, notes, type_cla_id, act_id, ppd_id) " +
						   "VALUES (:per_id, :due_date, :value, :notes, :type_cla_id, :act_id, :ppd_id)";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("per_id", (model.getPerson() == null ? null : model.getPerson().getId() ) );
			params.addValue("due_date", (model.getDueDate() == null ? null : model.getDueDate() ) );
			params.addValue("value", (model.getValue() == null ? null : model.getValue() ) );
			params.addValue("notes", (model.getNotes() == null ? null : model.getNotes() ) );
			params.addValue("type_cla_id", (model.getCommissionType() == null ? null : model.getCommissionType().getId() ) );
			params.addValue("act_id", (model.getBankAccount() == null ? null : model.getBankAccount().getId() ) );
			params.addValue("ppd_id", (model.getProposalDetail() == null ? null : model.getProposalDetail().getId() ) );
	
			log.trace( "[QUERY] proposalCommission.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getJdbcTemplatePortal().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar comissao da proposta: {}", model, e );
			throw new AppException( "Erro ao tentar salvar comissao da proposta.", e);
		}
	}

	@Override
	public Optional<ProposalCommission> update(ProposalCommission model) throws AppException {
		try {
			String query = 	"UPDATE proposal_commission SET per_id=:per_id, due_date=:due_date, value=:value, " + 
							"notes=:notes, type_cla_id=:type_cla_id, act_id=:act_id, ppd_id=:ppd_id  " +
					 	   	"WHERE pcm_id = :pcm_id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("per_id", (model.getPerson() == null ? null : model.getPerson().getId() ) );
			params.addValue("due_date", (model.getDueDate() == null ? null : model.getDueDate() ) );
			params.addValue("value", (model.getValue() == null ? null : model.getValue() ) );
			params.addValue("notes", (model.getNotes() == null ? null : model.getNotes() ) );
			params.addValue("type_cla_id", (model.getCommissionType() == null ? null : model.getCommissionType().getId() ) );
			params.addValue("act_id", (model.getBankAccount() == null ? null : model.getBankAccount().getId() ) );
			params.addValue("ppd_id", (model.getProposalDetail() == null ? null : model.getProposalDetail().getId() ) );
			params.addValue("pcm_id", model.getId());
			
			log.trace( "[QUERY] proposalCommission.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar comissao da proposta: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar comissao da proposta.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM proposal_commission WHERE pcm_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposalCommission.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o detalhe da proposta" , e );
			throw new AppException( "Erro ao excluir o detalhe da proposta.", e );
		}
	}
}
