package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IProposalFollowUpDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalFollowUpMapper;
import com.portal.model.ProposalFollowUp;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalFollowUpDAO extends BaseDAO implements IProposalFollowUpDAO {

    @Override
    public Optional<ProposalFollowUp> save(ProposalFollowUp model) throws AppException {
        try {
            String query = "INSERT INTO proposal_fup (pps_id, date, media_cla_id, person, comment) " +
                    "VALUES (:proposal, :date, :media, :person, :comment)";
           
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("proposal", model.getProposal());
            params.addValue("date", PortalTimeUtils.localDateTimeFormat( model.getDate(), "yyyy-MM-dd HH:mm:ss" ) );
            params.addValue("media", model.getMedia().getId());
            params.addValue("person", model.getPerson());
            params.addValue("comment", model.getComment());

            log.trace( "[QUERY] proposal_fup.save: {} [PARAMS]: {}", query, params.getValues() );

            KeyHolder keyHolder = new GeneratedKeyHolder();

            this.getJdbcTemplatePortal().update( query, params, keyHolder );

            model.setId( this.getKey(keyHolder) );

            return Optional.ofNullable(model);

        } catch( Exception e ) {
            log.error( "Erro ao tentar salvar follow up: {}", model, e );
            throw new AppException( "Erro ao tentar salvar follow up.", e);
        }
    }

    @Override
    public Optional<ProposalFollowUp> update(ProposalFollowUp model) throws AppException {
        try {
            String query = "UPDATE proposal_fup SET date = :date, person = :person, comment = :comment, media_cla_id = :media WHERE pfp_id = :pfpId";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("date", PortalTimeUtils.localDateTimeFormat( model.getDate(), "yyyy-MM-dd HH:mm:ss" ) );
            params.addValue("person", model.getPerson());
            params.addValue("comment", model.getComment());
            params.addValue("media", model.getMedia().getId());
            params.addValue("pfpId", model.getId());

            log.trace("[QUERY] proposal_fup.update {} [PARAMS] :{}", query, params.getValues());

            this.getJdbcTemplatePortal().update(query, params);

            return Optional.ofNullable(model);

        } catch (Exception e) {
            log.error("Erro ao tentar atualizar follow up: {}", model, e);
            throw new AppException("Erro ao tentar atualizar follow up.", e);
        }
    }

    @Override
    public Optional<ProposalFollowUp> find(ProposalFollowUp model) throws AppException {
        return Optional.empty();
    }

    @Override
    public Optional<ProposalFollowUp> getById(Integer id) throws AppException {
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT fup.*, ");
            query.append("	medTyp.cla_id as med_cla_id, ");
            query.append("	medTyp.value as med_cla_value, ");
            query.append("	medTyp.label as med_cla_label, ");
            query.append("	medTyp.type as med_cla_type ");
            query.append("FROM " + schemaName + "proposal_fup AS fup ");
    		query.append("INNER JOIN " + schemaName + "classifier AS medTyp on medTyp.cla_id = fup.media_cla_id ");
            query.append("WHERE fup.pfp_id = :id ");
            query.append("LIMIT 1");

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            log.trace("[QUERY] proposal_fup.getById: {} [PARAMS]: {}", query, params.getValues());

            return Optional.ofNullable(this.getJdbcTemplatePortal().queryForObject(query.toString(), params, new ProposalFollowUpMapper()));

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        } catch (Exception e) {
            log.error("Erro ao consultar follow up.", e);
            throw new AppException("Erro ao consultar follow up.", e);
        }
    }

    @Override
    public List<ProposalFollowUp> list() throws AppException {
        return null;
    }

    @Override
    public List<ProposalFollowUp> search(ProposalFollowUp model) throws AppException {
    	try {
    		StringBuilder query = new StringBuilder();
    		query.append("SELECT fup.*, ");
			query.append("	medTyp.cla_id as med_cla_id, ");
			query.append("	medTyp.value as med_cla_value, ");
			query.append("	medTyp.label as med_cla_label, ");
			query.append("	medTyp.type as med_cla_type ");
			query.append("FROM " + schemaName + "proposal_fup AS fup ");
			query.append("INNER JOIN " + schemaName + "classifier AS medTyp on medTyp.cla_id = fup.media_cla_id ");
			query.append("WHERE pfp_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			if(model != null) {
				if(model.getProposal() != null && model.getProposal() > 0) {
					query.append(" AND fup.pps_id = :ppsId ");
					params.addValue("ppsId", model.getProposal());
				}
			}
			
			log.trace("[QUERY] proposal_fup.search: {} [PARAMS]: {}", query, params.getValues());
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalFollowUpMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os follow up de proposta.", e );
			throw new AppException( "Erro ao procurar os follow up de proposta.", e );
		}
	}

    @Override
    public void delete(Integer id) throws AppException {
    	try {
            String query = 	"DELETE FROM proposal_fup WHERE pfp_id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue( "id", id );

            log.trace( "[QUERY] proposalFollowUp.delete: {} [PARAMS]: {}", query, params.getValues() );

            this.getJdbcTemplatePortal().update(query, params);
        } catch (Exception e) {
            log.error( "Erro ao excluir follow up" , e );
            throw new AppException( "Erro ao excluir follow up.", e );
        }
    }

    @Override
    public List<ProposalFollowUp> listAll(Pageable pageable) throws AppException {
        try {
            if( pageable == null ) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "bnk_id");
            }

            Sort.Order order = Sort.Order.desc( "pfp_id" );
            if( pageable.getSort().get().findFirst().isPresent() ) {
                order = pageable.getSort().get().findFirst().orElse( order );
            }
            
            String query = 	"SELECT  fup.*, " +
                    "	medTyp.cla_id as med_cla_id, " +
                    "	medTyp.value as med_cla_value, " + 
                    "	medTyp.label as med_cla_label, " +
                    "	medTyp.type as med_cla_type " +
                    "FROM " + schemaName + "proposal_fup pfp " +
                    "INNER JOIN " + schemaName + "classifier AS medTyp on medTyp.cla_id = fup.media_cla_id " +
                    "ORDER BY " + order.getProperty() + " " +
                    "LIMIT " + pageable.getPageSize() + " " +
                    "OFFSET " + pageable.getPageNumber();

            log.trace( "[QUERY] proposal_fup.listAll: {} [PARAMS]: {}", query );

            return this.getJdbcTemplatePortal().query( query, new ProposalFollowUpMapper() );

        } catch (Exception e) {
            log.error( "Erro ao listar Follow Up.", e );
            throw new AppException( "Erro ao listar Follow Up.", e );
        }
    }
}
