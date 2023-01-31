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
import com.portal.dao.IProposalStateHistoryDAO;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalStateHistoryMapper;
import com.portal.model.ProposalStateHistory;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalStateHistoryDAO extends BaseDAO implements IProposalStateHistoryDAO {

	@Override
	public Optional<ProposalStateHistory> getLastProposalHistory(Integer ppsId) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT psh.*, ");
			query.append("	cla_old.cla_id as cla_id_old, ");
			query.append("	cla_old.value as value_old, ");
			query.append("	cla_old.type as type_old, ");
			query.append("	cla_old.label as label_old, ");
			query.append("	cla_new.cla_id as cla_id_new, ");
			query.append("	cla_new.value as value_new, ");
			query.append("	cla_new.type as type_new, ");
			query.append("	cla_new.label as label_new ");
			query.append("FROM proposal_state_history psh ");
			query.append("LEFT JOIN classifier cla_old on cla_old.cla_id = psh.cla_id_old ");
			query.append("INNER JOIN classifier cla_new on cla_new.cla_id = psh.cla_id_new ");
			query.append("WHERE pps_id = :pps_id ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("pps_id", ppsId);
						
			query.append( "ORDER BY status_date DESC LIMIT 1");
			
			log.trace( "[QUERY] getLastProposalHistory: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalStateHistoryMapper() ) );

		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao buscar o detalhe de pagamento.", e );
			throw new AppException( "Erro ao buscar o detalhe de pagamento.", e );
		}
	}
	
	@Override
	public List<ProposalStateHistory> find(ProposalStateHistory model, Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "ASC" ), "position");
			}
			
			Order order = Order.asc( "ppy_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT .*, ");
			query.append("	cla_old.cla_id as cla_id_old, ");
			query.append("	cla_old.value as value_old, ");
			query.append("	cla_old.type as type_old, ");
			query.append("	cla_old.label as label_old, ");
			query.append("	cla_new.cla_id as cla_id_new, ");
			query.append("	cla_new.value as value_new, ");
			query.append("	cla_new.type as type_new, ");
			query.append("	cla_new.label as label_new ");
			query.append("FROM proposal_state_history psh ");
			query.append("LEFT JOIN classifier cla_old on cla_old.cla_id = psh.cla_id_old ");
			query.append("INNER JOIN classifier cla_new on cla_new.cla_id = psh.cla_id_new ");
			query.append("WHERE psh_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND psh.psh_id = :id ");
					params.addValue("id", model.getId());
				}

				if( model.getProposal() != null && model.getProposal().getId() != null && model.getProposal().getId() > 0) {
					query.append(" AND psh.pps_id = :pps_id ");
					params.addValue("pps_id", model.getProposal().getId() );
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			log.trace( "[QUERY] proposalPaymentfind: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalStateHistoryMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar o detalhe de pagamento.", e );
			throw new AppException( "Erro ao buscar o detalhe de pagamento.", e );
		}
	}
	
    @Override
    public Optional<ProposalStateHistory> save(ProposalStateHistory proposalStateHistory, UserProfileDTO userProfile) throws AppException {
        try {
            String query = "INSERT INTO proposal_state_history " +
                    "(pps_id, cla_id_old, cla_id_new, sor_id, usr_id, status_date) " +
                    "VALUES ( :pps_id,  :cla_id_old,  :cla_id_new,  :sor_id,  :usr_id, :status_date)";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("pps_id", proposalStateHistory.getProposal().getId());
            params.addValue("cla_id_old", proposalStateHistory.getStatusOld() != null ? proposalStateHistory.getStatusOld().getId() : null);
            params.addValue("cla_id_new", proposalStateHistory.getStatusNew() != null ? proposalStateHistory.getStatusNew().getId(): null);
            params.addValue("sor_id", proposalStateHistory.getSalesOrder() != null ? proposalStateHistory.getSalesOrder().getId() : null);
            params.addValue("usr_id", proposalStateHistory.getUser().getId());
            params.addValue("status_date", PortalTimeUtils.localDateTimeFormat(proposalStateHistory.getStatusDate(), "yyyy-MM-dd HH:mm:ss"));

            log.trace("[QUERY] proposalStateHistory.save: {} [PARAMS]: {}", query, params.getValues());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);

            proposalStateHistory.setId(this.getKey(keyHolder));

            return Optional.ofNullable(proposalStateHistory);

        } catch (Exception e) {
            log.error("Erro ao tentar salvar a histórico do status da proposta: {}", proposalStateHistory, e);
            throw new AppException("Erro ao tentar salvar a histórico do status da proposta.", e);
        }
    }
}
