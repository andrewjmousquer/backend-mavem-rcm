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
import com.portal.dao.IPartnerDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PartnerMapper;
import com.portal.model.Partner;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PartnerDAO extends BaseDAO implements IPartnerDAO {
	
	@Override
	public List<Partner> listAll( Pageable pageable ) throws AppException {
		try {
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id");	
			}
			
			Order order = Order.desc( "ptn_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT  ptn.*, " +
									"sit.cla_id as sit_cla_id," +
									"sit.value as sit_cla_value," +
									"sit.type as sit_cla_type," +
									"sit.label as sit_cla_label," +
									"chn.name AS chn_name, " +
									"chn.active AS chn_active, " +
									"per.name AS per_name, " +
									"per.job_title AS per_job_title, " +
									"per.cpf AS per_cpf, " +
									"per.cnpj AS per_cnpj, " +
									"per.rg AS per_rg, " +
									"per.rne AS per_rne, " +
									"per.add_id AS per_add_id, " +
									"typ.cla_id as per_cla_id," +
									"typ.value as per_cla_value," +
									"typ.type as per_cla_type," +
									"typ.label as per_cla_label," +
									"IFNULL(ptg.ptg_id, 0) AS ptg_id, " +
									"ptg.name AS ptg_name " +
							"FROM partner ptn " +
							"INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " +
							"INNER JOIN person per ON per.per_id = ptn.entity_per_id " +
							"INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " +
							"INNER JOIN channel chn ON chn.chn_id = ptn.chn_id " +
							"LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();
			

			log.trace( "[QUERY] partner.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new PartnerMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os parceiros.", e );
			throw new AppException( "Erro ao listar os parceiros.", e );
		}
	}

	@Override
	public Optional<Partner> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT  ptn.*, " +
									"sit.cla_id as sit_cla_id," +
									"sit.value as sit_cla_value," +
									"sit.type as sit_cla_type," +
									"sit.label as sit_cla_label," +
									"chn.name AS chn_name, " +
									"chn.active AS chn_active, " +
									"per.name AS per_name, " +
									"per.job_title AS per_job_title, " +
									"per.cpf AS per_cpf, " +
									"per.cnpj AS per_cnpj, " +
									"per.rg AS per_rg, " +
									"per.rne AS per_rne, " +
									"per.add_id AS per_add_id, " +
									"typ.cla_id as per_cla_id," +
									"typ.value as per_cla_value," +
									"typ.type as per_cla_type," +
									"typ.label as per_cla_label," +
									"IFNULL(ptg.ptg_id, 0) AS ptg_id, " +
									"ptg.name AS ptg_name " +
							"FROM partner ptn " +
							"INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " +
							"INNER JOIN person per ON per.per_id = ptn.entity_per_id " +
							"INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " +
							"INNER JOIN channel chn ON chn.chn_id = ptn.chn_id " +
							"LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id " +
							"WHERE ptn.ptn_id = :id " +
							"LIMIT 1";

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] partner.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PartnerMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o parceiro.", e );
			throw new AppException( "Erro ao consultar o parceiro.", e );
		}
	}
	
	@Override
	public Optional<Partner> save(Partner partner) throws AppException {
		try {
			
			if( partner == null || 
					partner.getChannel() == null || partner.getChannel().getId() == null || partner.getChannel().getId().equals( 0 ) ||
					partner.getPerson() == null || partner.getPerson().getId() == null || partner.getPerson().getId().equals( 0 )) {
				
				throw new AppException( "O objeto parceiro é inválido para salvar." ); 
				
			}
			
			String query = "INSERT INTO partner ( situation_cla, entity_per_id, ptg_id, chn_id, additional_term, is_assistance ) VALUES ( :situation_cla, :perId, :ptgId, :chnId, :additionalTerm, :isAssistance ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			params.addValue( "situation_cla", partner.getSituation().getId()  );
			params.addValue( "perId", partner.getPerson().getId() );
			params.addValue( "ptgId", ( partner.getPartnerGroup() != null ? partner.getPartnerGroup().getId() : null ) );
			params.addValue( "chnId", partner.getChannel().getId() );
			params.addValue( "additionalTerm", partner.getAdditionalTerm() );
			params.addValue( "isAssistance", PortalNumberUtils.booleanToInt(partner.isAssistance()));
	
			log.trace( "[QUERY] partner.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        partner.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( partner );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o parceiro: {}", partner, e );
			throw new AppException( "Erro ao tentar salvar o parceiro.", e);
		}
	}

	@Override
	public Optional<Partner> update(Partner partner) throws AppException {
		try {
			
			if( partner == null || 
					partner.getChannel() == null || partner.getChannel().getId() == null || partner.getChannel().getId().equals( 0 ) ||
					partner.getPerson() == null || partner.getPerson().getId() == null || partner.getPerson().getId().equals( 0 )) {
				
				throw new AppException( "O objeto parceiro é inválido para atualizar." ); 
			}
			
			String query = "UPDATE partner SET situation_cla=:situation_cla, entity_per_id=:perId, ptg_id=:ptgId, chn_id=:chnId, additional_term = :additionalTerm, is_assistance = :isAssistance WHERE ptn_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "situation_cla", partner.getSituation().getId() );
			params.addValue( "perId", partner.getPerson().getId() );
			params.addValue( "ptgId", ( partner.getPartnerGroup() != null ? partner.getPartnerGroup().getId() : null ) );
			params.addValue( "chnId", partner.getChannel().getId() );
			params.addValue( "additionalTerm", partner.getAdditionalTerm() );
			params.addValue( "id", partner.getId() );
			params.addValue( "isAssistance", PortalNumberUtils.booleanToInt(partner.isAssistance()));

			log.trace( "[QUERY] partner.update: {} [PARAMS]: {}", query, params.getValues() );
	        
			this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(partner);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o parceiro: {}", partner, e );
			throw new AppException( "Erro ao tentar atualizar o parceiro.", e);
		}
	}
	
	@Override
	public void delete(Integer id) throws AppException {

		try {
			String query = 	"DELETE FROM partner WHERE ptn_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] partner.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o parceiro.", e );
			throw new AppException( "Erro ao excluir o parceiro.", e );
		}
		
	}
	
	@Override
	public List<Partner> find(Partner partner, Pageable pageable) throws AppException {
		
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id");	
			}
			
			Order order = Order.asc( "ptn_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append( "SELECT  	ptn.*, " );
			query.append( "			sit.cla_id as sit_cla_id," );
			query.append( "			sit.value as sit_cla_value, " );
			query.append( "			sit.type as sit_cla_type, " );
			query.append( "			sit.label as sit_cla_label, " );
			query.append( "			chn.name AS chn_name, " );
			query.append( "			chn.active AS chn_active, " );
			query.append( "			per.name AS per_name, " );
			query.append( "			per.job_title AS per_job_title, " );
			query.append( "			per.cpf AS per_cpf, " );
			query.append( "			per.cnpj AS per_cnpj, " );
			query.append( "			per.rg AS per_rg, " );
			query.append( "			per.rne AS per_rne, " );
			query.append( "			per.add_id AS per_add_id, " );
			query.append( "			typ.cla_id as per_cla_id, " );
			query.append( "			typ.value as per_cla_value, " );
			query.append( "			typ.type as per_cla_type, " );
			query.append( "			typ.label as per_cla_label, " );
			query.append( "			IFNULL(ptg.ptg_id, 0) AS ptg_id, " );
			query.append( "			ptg.name AS ptg_name " );
			query.append( "FROM partner ptn " );
			query.append( "INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " );
			query.append( "INNER JOIN person per ON per.per_id = ptn.entity_per_id " );
			query.append( "INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " );
			query.append( "INNER JOIN channel chn ON chn.chn_id = ptn.chn_id  " );
			query.append( "LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id " );
			query.append( "WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(partner != null) {
				if(partner.getId() != null && partner.getId() > 0) {
					query.append(" AND ptn.ptn_id = :id ");
					params.addValue("id", partner.getId());
					hasFilter = true;
				}
				
				if( partner.getPartnerGroup() != null ) {
					query.append(" AND ptn.ptg_id = :ptgId ");
					params.addValue("ptgId", partner.getPartnerGroup().getId());
					hasFilter = true;
				}
				
				if( partner.getSituation() != null && partner.getSituation().getId() != null && partner.getSituation().getId() > 0) {
					query.append(" AND ptn.situation_cla = :situation_cla " );
					params.addValue("situation_cla", partner.getSituation().getId() );
					hasFilter = true;
				}
				
				if(partner.getChannel() != null ) {
					if( partner.getChannel().getId() != null ) {
						query.append(" AND ptn.chn_id = :chnId ");
						params.addValue("chnId", partner.getChannel().getId());
						hasFilter = true;
					}
					
					if( partner.getChannel().getName() != null ) {
						query.append(" AND chn.name = :chnName ");
						params.addValue("chnName", partner.getChannel().getName());
						hasFilter = true;
					}
				}
				
				if (partner.isAssistance()) {
					query.append(" AND ptn.assistance = :isAssistance");
					params.addValue( "isAssistance", PortalNumberUtils.booleanToInt(partner.isAssistance()));
					hasFilter = true;
				}
				
				if(partner.getPerson() != null ) {
					if( partner.getPerson().getId() != null ) {
						query.append(" AND ptn.entity_per_id = :perId ");
						params.addValue("perId", partner.getPerson().getId());
						hasFilter = true;
					}
					
					if( partner.getPerson().getName() != null ) {
						query.append(" AND per.name = :perName ");
						params.addValue("perName", partner.getPerson().getName());
						hasFilter = true;
					}
					
					if( partner.getPerson().getCnpj() != null ) {
						query.append(" AND per.cnpj = :perCnpj ");
						params.addValue("perCnpj", partner.getPerson().getCnpj());
						hasFilter = true;
					}
					
					if( partner.getPerson().getCpf() != null ) {
						query.append(" AND per.cpf = :perCpf ");
						params.addValue("perCpf", partner.getPerson().getCpf());
						hasFilter = true;
					}
					
					if( partner.getPerson().getRne() != null ) {
						query.append(" AND per.rne = :perRne ");
						params.addValue("perRne", partner.getPerson().getRne());
						hasFilter = true;
					}
					
					if( partner.getPerson().getRg() != null ) {
						query.append(" AND per.rg = :perRg ");
						params.addValue("perRg", partner.getPerson().getRg());
						hasFilter = true;
					}
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] partner.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PartnerMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os parceiros.", e );
			throw new AppException( "Erro ao buscar os parceiros.", e );
		}
	}
	
	@Override
	public List<Partner> search(Partner partner, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id");	
			}
			
			Order order = Order.asc( "ptn_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append( "SELECT  	ptn.*, " );
			query.append( "			sit.cla_id as sit_cla_id," );
			query.append( "			sit.value as sit_cla_value, " );
			query.append( "			sit.type as sit_cla_type, " );
			query.append( "			sit.label as sit_cla_label, " );
			query.append( "			chn.name AS chn_name, " );
			query.append( "			chn.active AS chn_active, " );
			query.append( "			per.name AS per_name, " );
			query.append( "			per.job_title AS per_job_title, " );
			query.append( "			per.cpf AS per_cpf, " );
			query.append( "			per.cnpj AS per_cnpj, " );
			query.append( "			per.rg AS per_rg, " );
			query.append( "			per.rne AS per_rne, " );
			query.append( "			per.add_id AS per_add_id, " );
			query.append( "			typ.cla_id as per_cla_id, " );
			query.append( "			typ.value as per_cla_value, " );
			query.append( "			typ.type as per_cla_type, " );
			query.append( "			typ.label as per_cla_label, " );
			query.append( "			IFNULL(ptg.ptg_id, 0) AS ptg_id, " );
			query.append( "			ptg.name AS ptg_name " );
			query.append("FROM partner ptn ");
			query.append("INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " );
			query.append("INNER JOIN person per ON per.per_id = ptn.entity_per_id " );
			query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN channel chn ON chn.chn_id = ptn.chn_id  " );
			query.append("LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id " );
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(partner != null) {
				if(partner.getId() != null && partner.getId() > 0) {
					query.append(" AND ptn.ptn_id = :id ");
					params.addValue("id", partner.getId());
					hasFilter = true;
				}
				
				if( partner.getPartnerGroup() != null ) {
					query.append(" AND ptn.ptg_id = :ptgId ");
					params.addValue("ptgId", partner.getPartnerGroup().getId());
					hasFilter = true;
				}
				
				if( partner.getSituation() != null && partner.getSituation().getId() != null && partner.getSituation().getId() > 0) {
					query.append(" AND ptn.situation_cla = :situation_cla " );
					params.addValue("situation_cla", partner.getSituation().getId() );
					hasFilter = true;
				}
				
				if(partner.getChannel() != null ) {
					if( partner.getChannel().getId() != null ) {
						query.append(" AND ptn.chn_id = :chnId ");
						params.addValue("chnId", partner.getChannel().getId());
						hasFilter = true;
					}
					
					if( partner.getChannel().getName() != null ) {
						query.append(" AND chn.name like :chnName ");
						params.addValue("chnName", this.mapLike( partner.getChannel().getName() ) );
						hasFilter = true;
					}
				}
				
				if (partner.isAssistance()) {
					query.append(" AND ptn.assistance = :isAssistance");
					params.addValue( "isAssistance", PortalNumberUtils.booleanToInt(partner.isAssistance()));
					hasFilter = true;
				}
				
				if(partner.getPerson() != null ) {
					if( partner.getPerson().getId() != null ) {
						query.append(" AND ptn.entity_per_id = :perId ");
						params.addValue("perId", partner.getPerson().getId());
						hasFilter = true;
					}
					
					if( partner.getPerson().getName() != null ) {
						query.append(" AND per.name like :perName ");
						params.addValue("perName", this.mapLike( partner.getPerson().getName() ) );
						hasFilter = true;
					}
					
					if( partner.getPerson().getCnpj() != null ) {
						query.append(" AND per.cnpj like :perCnpj ");
						params.addValue("perCnpj", this.mapLike( partner.getPerson().getCnpj()) );
						hasFilter = true;
					}
					
					if( partner.getPerson().getCpf() != null ) {
						query.append(" AND per.cpf like :perCpf ");
						params.addValue("perCpf", this.mapLike(partner.getPerson().getCpf()) ); 
						hasFilter = true;
					}
					
					if( partner.getPerson().getRne() != null ) {
						query.append(" AND per.rne like :perRne ");
						params.addValue("perRne", this.mapLike( partner.getPerson().getRne()) );
						hasFilter = true;
					}
					
					if( partner.getPerson().getRg() != null ) {
						query.append(" AND per.rg like :perRg ");
						params.addValue("perRg", this.mapLike( partner.getPerson().getRg()) );
						hasFilter = true;
					}
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] partner.search: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PartnerMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao procurar os parceiros.", e );
			throw new AppException( "Erro ao procurar os parceiros.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(Partner, Pageable)}
	 */
	@Override
	public Optional<Partner> find( Partner partner ) throws AppException {
		List<Partner> brands = this.find( partner, null );
		return Optional.ofNullable( ( brands != null ? brands.get(0) : null ) ); 
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	@Deprecated
	public List<Partner> list() throws AppException {
		return this.listAll(null);
	}

	/**
	 * @deprecated Usar a função {@link #search(Partner, Pageable)}
	 */
	@Override
	public List<Partner> search(Partner model) throws AppException {
		return this.search(model, null);
	}

	@Override
	public List<Partner> searchForm(String searchText, Pageable pageable) throws AppException {

		try {
			boolean hasFilter = false;

			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ptn_id");
			}

			Order order = Order.asc( "ptn_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}

			StringBuilder query = new StringBuilder();

			query.append( "SELECT  	ptn.*, " );
			query.append( "			sit.cla_id as sit_cla_id," );
			query.append( "			sit.value as sit_cla_value, " );
			query.append( "			sit.type as sit_cla_type, " );
			query.append( "			sit.label as sit_cla_label, " );
			query.append( "			chn.name AS chn_name, " );
			query.append( "			chn.active AS chn_active, " );
			query.append( "			per.name AS per_name, " );
			query.append( "			per.job_title AS per_job_title, " );
			query.append( "			per.cpf AS per_cpf, " );
			query.append( "			per.cnpj AS per_cnpj, " );
			query.append( "			per.rg AS per_rg, " );
			query.append( "			per.rne AS per_rne, " );
			query.append( "			per.add_id AS per_add_id, " );
			query.append( "			typ.cla_id as per_cla_id, " );
			query.append( "			typ.value as per_cla_value, " );
			query.append( "			typ.type as per_cla_type, " );
			query.append( "			typ.label as per_cla_label, " );
			query.append( "			IFNULL(ptg.ptg_id, 0) AS ptg_id, " );
			query.append( "			ptg.name AS ptg_name " );
			query.append("FROM partner ptn ");
			query.append("INNER JOIN classifier sit ON ptn.situation_cla = sit.cla_id " );
			query.append("INNER JOIN person per ON per.per_id = ptn.entity_per_id " );
			query.append("INNER JOIN classifier typ ON per.classification_cla_id = typ.cla_id " );
			query.append("INNER JOIN channel chn ON chn.chn_id = ptn.chn_id  " );
			query.append("LEFT JOIN partner_group ptg ON ptn.ptg_id = ptg.ptg_id " );
			query.append("WHERE :hasFilter ");

			MapSqlParameterSource params = new MapSqlParameterSource();

			if(searchText != null) {

				query.append(" AND (per.name like :text ");
				query.append(" or chn.name like :text ");
				query.append(" or ptg.name like :text ");
				query.append(" or sit.label like :text )");
				hasFilter=true;
				params.addValue("text", this.mapLike( searchText ) );
			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );

			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );

			log.trace( "[QUERY] partner.find: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().query( query.toString(), params, new PartnerMapper() );

		} catch (Exception e) {
			log.error( "Erro ao buscar os parceiros.", e );
			throw new AppException( "Erro ao buscar os parceiros.", e );
		}
	}

	@Override
	public boolean hasPriceListRelationship(Integer partnerId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT ptn_id FROM price_list_partner WHERE ptn_id = :ptnId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ptnId", partnerId );

			log.trace( "[QUERY] partner.hasPriceListRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com lista de preço." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com lista de preço.", e );
		}
	}
}
