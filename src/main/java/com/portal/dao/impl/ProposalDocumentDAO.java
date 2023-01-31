package com.portal.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IProposalDocumentDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.DocumentMapper;
import com.portal.mapper.ProposalMapper;
import com.portal.model.Document;
import com.portal.model.Proposal;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalDocumentDAO extends BaseDAO implements IProposalDocumentDAO {
	
	@Override
	public Optional<Document> getDocument( Integer ppsId, Integer docId ) throws AppException {
		try {
			
			if( ppsId == null || docId == null || ppsId.equals(0) || docId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou documento estão inválidos." );
			}
			
			String query =  "SELECT doc.*, " +
							"c.cla_id, " +
							"c.value as cla_value, " +
							"c.type as cla_type, " +
							"c.label as cla_label, " + 
							"p.name " +
							"FROM proposal pps " +
							"INNER JOIN proposal_document ppd ON ppd.pps_id = pps.pps_id " +
							"INNER JOIN document doc ON doc.doc_id = ppd.doc_id " +
							"INNER JOIN classifier c on doc.type_cla_id = c.cla_id " +
							"INNER JOIN user u on u.usr_id = doc.usr_id " + 
							"INNER JOIN person p on p.per_id = u.per_id " + 
							"WHERE pps.pps_id = :ppsId " +
							"AND doc.doc_id = :docId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("docId", docId);
			params.addValue("ppsId", ppsId);
	
			log.trace( "[QUERY] proposalDocument.getDocument: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new DocumentMapper() ) );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e documento.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre proposta e documento.", e );
		}
	}
	
	@Override
	public Optional<Proposal> getProposal( Integer ppsId, Integer docId ) throws AppException {
		try {
			
			if( ppsId == null || docId == null || ppsId.equals(0) || docId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou documento estão inválidos." );
			}
			
			String query =  "SELECT pps.* " +
							"c.cla_id, " +
							"c.value as cla_value, " +
							"c.type as cla_type, " +
							"c.label as cla_label, " + 
							"FROM proposal pps " +
							"INNER JOIN proposal_document ppd ON ppd.pps_id = pps.pps_id " +
							"INNER JOIN document doc ON doc.doc_id = ppd.doc_id " +
							"WHERE pps.pps_id = :ppsId " +
							"AND doc.doc_id = :docId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("docId", docId);
			params.addValue("ppsId", ppsId);
	
			log.trace( "[QUERY] proposalDocument.getProposal: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new ProposalMapper() ) );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e documento.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre proposta e documento.", e );
		}
	}
	
	@Override
	public List<Document> findByProposal( Integer ppsId ) throws AppException {
		try {
			if( ppsId == null || ppsId.equals(0) ) {
				throw new AppException( "ID da proposta está inválida." );
			}
			
			String query =  "SELECT doc.*, " +
							"c.cla_id, " +
							"c.value as cla_value, " +
							"c.type as cla_type, " +
							"c.label as cla_label, " + 
							"p.name " +
							"FROM proposal pps " +
							"INNER JOIN proposal_document ppd ON ppd.pps_id = pps.pps_id " +
							"INNER JOIN document doc ON doc.doc_id = ppd.doc_id " +
							"INNER JOIN classifier c on doc.type_cla_id = c.cla_id " +
							"INNER JOIN user u on u.usr_id = doc.usr_id " + 
							"INNER JOIN person p on p.per_id = u.per_id " + 
							"WHERE pps.pps_id = :ppsId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ppsId", ppsId);
	
			log.trace( "[QUERY] proposalDocument.findByProposal: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query, params, new DocumentMapper() );
			
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e documento usando a proposta.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre proposta e documento usando a proposta.", e );
		}
	}

	@Override
	public List<Proposal> findByDocument( Integer docId ) throws AppException {
		try {
			
			if( docId == null || docId.equals(0) ) {
				throw new AppException( "O ID da documento está inválido." );
			}
			
			String query =  "SELECT pps.* " +
							"c.cla_id, " +
							"c.value as cla_value, " +
							"c.type as cla_type, " +
							"c.label as cla_label, " + 
							"FROM proposal pps " +
							"INNER JOIN proposal_document ppd ON ppd.pps_id = pps.pps_id " +
							"INNER JOIN document doc ON doc.doc_id = ppd.doc_id " +
							"INNER JOIN classifier c on doc.type_cla_id = c.cla_id " +
							"WHERE doc.doc_id = :docId ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("docId", docId);
	
			log.trace( "[QUERY] proposalDocument.findByDocument: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query, params, new ProposalMapper() );
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao buscar o relacionamento entre proposta e documento usando a documento.", e );
			throw new AppException( "Erro ao buscar o relacionamento entre proposta e documento usando a documento.", e );
		}
	}
	
	@Override
	public void save(Integer ppsId, Integer docId) throws AppException {
		try {
			if( ppsId == null || docId == null || ppsId.equals(0) || docId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou documento estão inválidos." );
			}
			
			String query = "INSERT INTO proposal_document ( pps_id, doc_id ) " +
					 	   "VALUES ( :ppsId, :docId ) " +
						   "ON DUPLICATE KEY UPDATE pps_id=VALUES(pps_id), doc_id=VALUES(doc_id) ";
			
	        MapSqlParameterSource params = new MapSqlParameterSource();
	        params.addValue( "docId", docId );
			params.addValue( "ppsId", ppsId );
			
			log.trace( "[QUERY] proposalDocument.save: {} [PARAMS]: {}", query, params.getValues() );
			
			this.getJdbcTemplatePortal().update( query, params );
		} catch (AppException e) {
			throw e;	
		} catch( Exception e ) {
			log.error( "Erro ao tentar salvar o relacionamento de proposta e documento.", e );
			throw new AppException( "Erro ao tentar salvar o relacionamento de proposta e documento.", e);
		}		
	}

	@Override
	public void delete(Integer ppsId, Integer docId) throws AppException {
		try {
			
			if( ppsId == null || docId == null || ppsId.equals(0) || docId.equals(0) ) {
				throw new AppException( "Os IDs de relacionamento de proposta e/ou documento estão inválidos." );
			}
			
			String query = 	"DELETE FROM proposal_document WHERE pps_id = :ppsId AND doc_id = :docId";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ppsId", ppsId );
			params.addValue( "docId", docId );
	
			log.trace( "[QUERY] proposalDocument.delete: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query, params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre proposta e documento." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre proposta e documento.", e );
		}
	}

	@Override
	public void deleteByProposal(Integer ppsId) throws AppException {
		try {
			if( ppsId == null || ppsId.equals(0)  ) {
				throw new AppException( "O ID da proposta está inválido." );
			}
			
			String query = 	"DELETE FROM proposal_document WHERE pps_id = :ppsId";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ppsId", ppsId );
	
			log.trace( "[QUERY] proposalDocument.deleteByProposal: {} [PARAMS]: {}", query, params.getValues() );
	
			this.getJdbcTemplatePortal().update(query, params);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error( "Erro ao excluir os relacionamento entre proposta e documento." , e );
			throw new AppException( "Erro ao excluir os relacionamento entre proposta e documentos.", e );
		}
	}

	@Override
	public String getDocumentUrl(Integer id) throws AppException {
		try {

			String query = 	"select file_path from document d "
					+ "where d.doc_id = :docId";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "docId", id );

			log.trace( "[QUERY] document.getDocumentUrl: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getString("file_path") );

		} catch (Exception e) {
			log.error( "Erro ao buscar document." , e );
			throw new AppException( "Erro ao buscar document.", e );
		}
	}
}
