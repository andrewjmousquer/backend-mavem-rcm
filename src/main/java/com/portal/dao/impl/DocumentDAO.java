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
import com.portal.dao.IDocumentDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.DocumentMapper;
import com.portal.model.Document;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class DocumentDAO extends BaseDAO implements IDocumentDAO {

	@Override
	public List<Document> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "doc_id");	
			}
			
			Order order = Order.desc( "doc_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT doc.*, " +
							"c.cla_id, " +
							"c.value as cla_value, " +
							"c.type as cla_type, " +
							"c.label as cla_label, " + 
							"p.name " + 
							"FROM document doc " +
							"INNER JOIN classifier c on doc.type_cla_id = c.cla_id " +
							"INNER JOIN user u on u.usr_id = doc.usr_id " + 
							"INNER JOIN person p on p.per_id = u.per_id " + 
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			
			log.trace( "[QUERY] document.listAll: {}", query );

			return this.getJdbcTemplatePortal().query( query, new DocumentMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os documentos.", e );
			throw new AppException( "Erro ao listar os documentos.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<Document> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<Document> find(Document model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "doc_id");	
			}
			
			Order order = Order.asc( "doc_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT doc.*, p.name, ");
			query.append("c.cla_id, ");
			query.append("c.value as cla_value, ");
			query.append("c.type as cla_type, ");
			query.append("c.label as cla_label ");
			query.append("FROM document doc ");
			query.append("INNER JOIN classifier c on doc.type_cla_id = c.cla_id ");
			query.append("INNER JOIN user u on u.usr_id = doc.usr_id " ); 
			query.append("INNER JOIN person p on p.per_id = u.per_id " );
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND doc.doc_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getFileName() != null && !model.getFileName().equals("")) {
					query.append(" AND file_name = :fileName " );
					params.addValue("fileName", model.getFileName());
					hasFilter = true;
				}
				
				if(model.getContentType() != null && !model.getContentType().equals("")) {
					query.append(" AND content_type = :contentType " );
					params.addValue("contentType", model.getContentType());
					hasFilter = true;
				}
				
				if(model.getDescription() != null && !model.getDescription().equals("")) {
					query.append(" AND description = :description " );
					params.addValue("description", model.getDescription());
					hasFilter = true;
				}
				
				if( model.getCreateDate() != null ) {
					query.append(" AND create_date = :createDate " );
					params.addValue("createDate", PortalTimeUtils.localDateTimeFormat( model.getCreateDate(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				}
				
				if(model.getFilePath() != null && !model.getFilePath().equals("")) {
					query.append(" AND file_path = :filePath " );
					params.addValue("filePath", model.getFilePath());
					hasFilter = true;
				}
				
				if( model.getType() != null ) {
					query.append(" AND type_cla_id = :typeId " );
					params.addValue("typeId", model.getType().getId());
					hasFilter = true;
				}
				
				if( model.getUser() != null ) {
					query.append(" AND usr_id = :usrId " );
					params.addValue("usrId", model.getUser().getId());
					hasFilter = true;
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] document.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new DocumentMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os documentos.", e );
			throw new AppException( "Erro ao buscar os documentos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Document, Pageable)}
	 */
	@Override
	public Optional<Document> find(Document model) throws AppException {
		List<Document> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public List<Document> search(Document model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "doc_id");	
			}
			
			Order order = Order.asc( "doc_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT doc.*, p.name, ");
			query.append("c.cla_id, ");
			query.append("c.value as cla_value, ");
			query.append("c.type as cla_type, ");
			query.append("c.label as cla_label ");
			query.append("FROM document doc ");
			query.append("INNER JOIN classifier c on doc.type_cla_id = c.cla_id ");
			query.append("INNER JOIN user u on u.usr_id = doc.usr_id " ); 
			query.append("INNER JOIN person p on p.per_id = u.per_id " );
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND doc.doc_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getFileName() != null && !model.getFileName().equals("")) {
					query.append(" AND file_name LIKE :fileName " );
					params.addValue("fileName", this.mapLike( model.getFileName() ) );
					hasFilter = true;
				}
				
				if(model.getContentType() != null && !model.getContentType().equals("")) {
					query.append(" AND content_type LIKE :contentType " );
					params.addValue("contentType", this.mapLike( model.getContentType() ) );
					hasFilter = true;
				}
				
				if(model.getDescription() != null && !model.getDescription().equals("")) {
					query.append(" AND description LIKE :description " );
					params.addValue("description", this.mapLike( model.getDescription() ) );
					hasFilter = true;
				}
				
				if( model.getCreateDate() != null ) {
					query.append(" AND create_date = :createDate " );
					params.addValue("createDate", PortalTimeUtils.localDateTimeFormat( model.getCreateDate(), "yyyy-MM-dd HH:mm:ss" ) );
					hasFilter = true;
				}
				
				if(model.getFilePath() != null && !model.getFilePath().equals("")) {
					query.append(" AND file_path = :filePath " );
					params.addValue("filePath", model.getFilePath());
					hasFilter = true;
				}
				
				if( model.getType() != null ) {
					query.append(" AND type_cla_id = :typeId " );
					params.addValue("typeId", model.getType().getId());
					hasFilter = true;
				}
				
				if( model.getUser() != null ) {
					query.append(" AND usr_id = :usrId " );
					params.addValue("usrId", model.getUser().getId());
					hasFilter = true;
				}
			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] document.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new DocumentMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os documentos.", e );
			throw new AppException( "Erro ao buscar os documentos.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #search(Document, Pageable)}
	 */
	@Override
	public List<Document> search(Document model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public Optional<Document> getById(Integer id) throws AppException {
		try {
			
			String query = "SELECT doc.*, " +
							"c.cla_id, " +
							"c.value as cla_value, " +
							"c.type as cla_type, " +
							"c.label as cla_label, " + 
							"p.name " + 
							"FROM document doc " +
							"INNER JOIN classifier c on doc.type_cla_id = c.cla_id " +
							"INNER JOIN user u on u.usr_id = doc.usr_id " + 
							"INNER JOIN person p on p.per_id = u.per_id " + 
							"WHERE doc.doc_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] document.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new DocumentMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o documento.", e );
			throw new AppException( "Erro ao consultar o documento.", e );
		}
	}

	@Override
	public Optional<Document> save(Document model) throws AppException {
		try {
			String query = "INSERT INTO document (doc_id, file_name, content_type, description, file_path, create_date, usr_id, type_cla_id) " +
					 	   "VALUES ( NULL, :fileName, :contentType, :description, :filePath, :createDate, :usrId, :typeId ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "fileName", model.getFileName() );
			params.addValue( "contentType", model.getContentType() );
			params.addValue( "description", model.getDescription() );
			params.addValue( "filePath", model.getFilePath() );
			params.addValue( "createDate", PortalTimeUtils.localDateTimeFormat( model.getCreateDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "usrId", ( model.getUser() == null ? null : model.getUser().getId() ) );
			params.addValue( "typeId", model.getType().getId() );
	
			log.trace( "[QUERY] document.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o documento: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o documento.", e);
		}
	}

	@Override
	public Optional<Document> update(Document model) throws AppException {
		try {
			String query = "UPDATE document SET file_name=:fileName, content_type=:contentType, description=:description, " +
						   "file_path=:filePath, create_date=:createDate, usr_id=:usrId, type_cla_id=:typeId " +
					 	   "WHERE doc_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "fileName", model.getFileName() );
			params.addValue( "contentType", model.getContentType() );
			params.addValue( "description", model.getDescription() );
			params.addValue( "filePath", model.getFilePath() );
			params.addValue( "createDate", PortalTimeUtils.localDateTimeFormat( model.getCreateDate(), "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue( "usrId", ( model.getUser() == null ? null : model.getUser().getId() ) );
			params.addValue( "typeId", model.getType().getId() );
	
			log.trace( "[QUERY] document.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o documento: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o documento.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM document WHERE doc_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] document.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o documento" , e );
			throw new AppException( "Erro ao excluir o documento.", e );
		}

	}

	@Override
	public boolean hasProposalRelationship(Integer docId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT doc_id FROM proposal_document WHERE doc_id = :docId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "docId", docId );

			log.trace( "[QUERY] document.hasProposalRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com a proposta." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com a proposta.", e );
		}
	}
}
