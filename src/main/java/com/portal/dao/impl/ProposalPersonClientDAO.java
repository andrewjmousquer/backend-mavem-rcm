package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IProposalPersonClientDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PersonMapper;
import com.portal.mapper.ProposalMapper;
import com.portal.mapper.ProposalPersonMapper;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalPerson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalPersonClientDAO extends BaseDAO implements IProposalPersonClientDAO {
	
	@Override
	public Optional<Person> getPerson( Integer ppsId, Integer perId ) throws AppException {
		try {
			
			if( ppsId == null || perId == null || ppsId.equals(0) || perId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou pessoa estão inválidos." );
			}
			
			String query =  "SELECT per.* " +
							"FROM proposal pps " +
							"INNER JOIN proposal_person_client ppc ON ppc.pps_id = pps.pps_id " +
							"INNER JOIN person per ON per.per_id = ppc.per_id " +
							"WHERE pps.pps_id = :ppsId " +
							"AND per.per_id = :perId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("perId", perId);
			params.addValue("ppsId", ppsId);
	
			log.trace( "[QUERY] proposalPerson.getPerson: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PersonMapper() ) );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e pessoa.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre proposta e pessoa.", e );
		}
	}
	
	@Override
	public Optional<Proposal> getProposal( Integer ppsId, Integer perId ) throws AppException {
		try {
			
			if( ppsId == null || perId == null || ppsId.equals(0) || perId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou pessoa estão inválidos." );
			}
			
			String query =  "SELECT pps.* " +
							"FROM proposal pps " +
							"INNER JOIN proposal_person_client ppc ON ppc.pps_id = pps.pps_id " +
							"INNER JOIN person per ON per.per_id = ppc.per_id " +
							"WHERE pps.pps_id = :ppsId " +
							"AND per.per_id = :perId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("perId", perId);
			params.addValue("ppsId", ppsId);
	
			log.trace( "[QUERY] proposalPerson.getProposal: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ProposalMapper() ) );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e pessoa.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre proposta e pessoa.", e );
		}
	}
	
	@Override
	public List<ProposalPerson> findByProposal( Integer ppsId ) throws AppException {
		try {
			if( ppsId == null || ppsId.equals(0) ) {
				throw new AppException( "ID da proposta está inválida." );
			}
			
			String query =  "SELECT ppc.*, "+
							"c.cla_id as pper_cla_id, " +
							"c.value as pper_cla_value, " + 
							"c.type as pper_cla_type, " + 
							"c.label as pper_cla_label " + 
							"FROM proposal_person_client ppc " +
							"inner join classifier c on c.cla_id = ppc.customer_cla_id " +
							"WHERE ppc.pps_id = :ppsId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ppsId", ppsId);
	
			log.trace( "[QUERY] proposalPerson.findByProposal: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query, params, new ProposalPersonMapper() );
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e pessoa usando a proposta.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre proposta e pessoa usando a proposta.", e );
		}
	}

	@Override
	public List<Proposal> findByPerson( Integer perId ) throws AppException {
		try {
			
			if( perId == null || perId.equals(0) ) {
				throw new AppException( "O ID da pessoa está inválida." );
			}
			
			String query =  "SELECT pps.* " +
							"FROM proposal pps " +
							"INNER JOIN proposal_person_client ppc ON ppc.pps_id = pps.pps_id " +
							"INNER JOIN person per ON per.per_id = ppc.per_id " +
							"WHERE per.per_id = :perId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("perId", perId);
	
			log.trace( "[QUERY] proposalPerson.findByPerson: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query, params, new ProposalMapper() );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e pessoa usando a pessoa.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre proposta e pessoa usando a pessoa.", e );
		}
	}
	
	@Override
	public void save(Integer ppsId, ProposalPerson proposalPerson) throws AppException {
		try {
			if( ppsId == null || proposalPerson.getPerson().getId() == null || ppsId.equals(0) || proposalPerson.getPerson().getId().equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou pessoa estão inválidos." );
			}
			
			String query = "INSERT INTO proposal_person_client ( pps_id, per_id, customer_cla_id ) " +
					 	   "VALUES ( :ppsId, :perId, :customer ) " +
						   "ON DUPLICATE KEY UPDATE pps_id=VALUES(pps_id), per_id=VALUES(per_id) ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "perId", proposalPerson.getPerson().getId() );
			params.addValue( "ppsId", ppsId );
			params.addValue( "customer", proposalPerson.getProposalPersonClassification().getId() );
			
			log.trace( "[QUERY] proposalPerson.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento de proposta e pessoa.", e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de proposta e pessoa.", e);
		}		
	}

	@Override
	public void delete(Integer ppsId, Integer perId) throws AppException {
		try {
			
			if( ppsId == null || perId == null || ppsId.equals(0) || perId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou pessoa estão inválidos." );
			}
			
			String query = 	"DELETE FROM proposal_person_client WHERE pps_id = :ppsId AND per_id = :perId";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ppsId", ppsId );
			params.addValue( "perId", perId );
	
			log.trace( "[QUERY] proposalPerson.delete: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query, params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre proposta e pessoa." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre proposta e pessoa.", e );
		}
	}

	@Override
	public void deleteByProposal(Integer ppsId) throws AppException {
		try {
			if( ppsId == null || ppsId.equals(0)  ) {
				throw new AppException( "O ID da proposta está inválido." );
			}
			
			String query = 	"DELETE FROM proposal_person_client WHERE pps_id = :ppsId";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ppsId", ppsId );
	
			log.trace( "[QUERY] proposalPerson.deleteByProposal: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query, params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre proposta e pessoa." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre proposta e pessoas.", e );
		}
	}

	@Override
	public void update(Integer ppsId, ProposalPerson proposalPerson) throws AppException {
		try {
			if( ppsId == null || proposalPerson.getPerson().getId() == null || ppsId.equals(0) || proposalPerson.getPerson().getId().equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou pessoa estão inválidos." );
			}
			
			String query = 	"UPDATE proposal_person_client " + 
							"SET customer_cla_id = :customer " + 
							"WHERE (pps_id = :ppsId) and (per_id = :perId) ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "perId", proposalPerson.getPerson().getId() );
			params.addValue( "ppsId", ppsId );
			params.addValue( "customer", proposalPerson.getProposalPersonClassification().getId() );
			
			log.trace( "[QUERY] proposalPerson.update: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar atualizar o relacionamento de proposta e pessoa.", e );
			throw new AppException( "Erro ao tentar atualizar o relacionamento de proposta e pessoa.", e);
		}	
	}

}
