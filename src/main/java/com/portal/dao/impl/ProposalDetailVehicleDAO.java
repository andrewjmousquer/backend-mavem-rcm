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
import com.portal.dao.IProposalDetailVehicleDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalDetailVehicleMapper;
import com.portal.model.ProposalDetailVehicle;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalDetailVehicleDAO extends BaseDAO implements IProposalDetailVehicleDAO {

	@Override
	public List<ProposalDetailVehicle> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdv_id");	
			}
			
			Order order = Order.desc( "pdv_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT  pdv.* " +
							"	pl.prl_id, pl.name as price_list " +
							"FROM proposal_detail_vehicle pdv " +
							"inner join price_product as pp on pdv.ppr_id = pp.ppr_id " +
							"inner join price_list as pl on pp.prl_id = pl.prl_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] proposal_detail_vehicle.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ProposalDetailVehicleMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os detalhes do veiculo da proposta.", e );
			throw new AppException( "Erro ao listar os detalhes do veiculo da proposta.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<ProposalDetailVehicle> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<ProposalDetailVehicle> find( ProposalDetailVehicle model, Pageable pageable ) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdv_id");	
			}
			
			Order order = Order.asc( "pdv_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pdv.*, ");
			query.append("	pl.prl_id, pl.name as price_list ");
			query.append("FROM proposal_detail_vehicle pdv ");
			query.append("inner join price_product as pp on pdv.ppr_id = pp.ppr_id ");
			query.append("inner join price_list as pl on pp.prl_id = pl.prl_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND pdv.pdv_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getProposalDetail() != null && model.getProposalDetail().getId() != null) {
					query.append(" AND pdv.ppd_id = :ppdId ");
					params.addValue("ppdId", model.getProposalDetail().getId());
					hasFilter = true;
				}
				
				if(model.getVehicle() != null && model.getVehicle().getId() != null) {
					query.append(" AND pdv.vhe_id = :vheId ");
					params.addValue("vheId", model.getVehicle().getId());
					hasFilter = true;
				}
				
				if(model.getModel() != null && model.getModel().getId() != null) {
					query.append(" AND pdv.mdl_id = :mdlId ");
					params.addValue("mdlId", model.getModel().getId());
					hasFilter = true;
				}
				
				if(model.getPriceProduct() != null && model.getPriceProduct().getId() != null) {
					query.append(" AND pdv.ppr_id = :prpId ");
					params.addValue("prpId", model.getPriceProduct().getId());
					hasFilter = true;
				}
			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] proposal_detail_vehicle.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalDetailVehicleMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os detalhes do veiculo da proposta.", e );
			throw new AppException( "Erro ao buscar os detalhes do veiculo da proposta.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(ProposalDetailVehicle, Pageable)}
	 */
	@Override
	public Optional<ProposalDetailVehicle> find(ProposalDetailVehicle model) throws AppException {
		List<ProposalDetailVehicle> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	/**
	 * @deprecated Usar a função {@link #search(ProposalDetailVehicle, Pageable)}
	 */
	@Override
	public List<ProposalDetailVehicle> search(ProposalDetailVehicle model) throws AppException {
		return this.find(model, null);
	}

	@Override
	public Optional<ProposalDetailVehicle> getById(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pdv.*, ");
			query.append("	pl.prl_id, pl.name as price_list ");
			query.append("FROM proposal_detail_vehicle pdv ");
			query.append("inner join price_product as pp on pdv.ppr_id = pp.ppr_id ");
			query.append("inner join price_list as pl on pp.prl_id = pl.prl_id ");
			query.append("WHERE pdv.pdv_id = :id ");
			query.append("LIMIT 1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposal_detail_vehicle.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalDetailVehicleMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o detalhe do veiculo da proposta.", e );
			throw new AppException( "Erro ao consultar o detalhe do veiculo da proposta.", e );
		}
	}

	@Override
	public Optional<ProposalDetailVehicle> save(ProposalDetailVehicle model) throws AppException {
		try {
			String query = "INSERT INTO proposal_detail_vehicle (ppd_id, ppr_id, vhe_id, mdl_id, model_year, version, " +
																"product_amount_discount, product_percent_discount, product_final_price, " +
																"over_price, over_price_partner_discount_amount, over_price_partner_discount_percent, " +
																"price_discount_amount, price_discount_percent, " +
																"total_amount, total_tax_amount, total_tax_percent," +
																"standard_term_days, agreed_term_days) " +
							"VALUES (:ppdId, :prpId, :vheId, :mdlId, :modelYear, :version, :pad, :ppd, :pfp, :op, :oppda, :oppdp, :pda, :pdp, :ta, :tta, :ttp, :standardTermDays, :agreedTermDays ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ppdId", model.getProposalDetail().getId());
			params.addValue("prpId", model.getPriceProduct().getId());
			params.addValue("vheId", ( model.getVehicle() != null ? model.getVehicle().getId() : null ));
			params.addValue("mdlId", ( model.getModel() != null ? model.getModel().getId() : null ));
			params.addValue("modelYear", model.getModelYear());
			params.addValue("version", model.getVersion());
			params.addValue("pad", model.getProductAmountDiscount() );
			params.addValue("ppd", model.getProductPercentDiscount() );
			params.addValue("pfp", model.getProductFinalPrice() );
			params.addValue("op", model.getOverPrice() );
			params.addValue("oppda", model.getOverPricePartnerDiscountAmount() );
			params.addValue("oppdp", model.getOverPricePartnerDiscountPercent() );
			params.addValue("pda", model.getPriceDiscountAmount() );
			params.addValue("pdp", model.getPriceDiscountPercent() );
			params.addValue("ta", model.getTotalAmount() );
			params.addValue("tta", model.getTotalTaxAmount() );
			params.addValue("ttp", model.getTotalTaxPercent() );
			params.addValue("standardTermDays", model.getStandardTermDays());
			params.addValue("agreedTermDays", model.getAgreedTermDays());
			
			log.trace( "[QUERY] proposal_detail_vehicle.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o detalhe do veículo da proposta: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o detalhe do veículo da proposta.", e);
		}
	}

	@Override
	public Optional<ProposalDetailVehicle> update(ProposalDetailVehicle model) throws AppException {
		try {
			String query = "UPDATE proposal_detail_vehicle SET " +     
										"ppd_id=:ppdId, ppr_id=:prpId, vhe_id=:vheId, mdl_id = :mdlId, model_year = :modelYear, version = :version, " +
										"product_amount_discount=:pad, product_percent_discount=:ppd, product_final_price=:pfp, " +
										"over_price=:op, over_price_partner_discount_amount=:oppda, over_price_partner_discount_percent=:oppdp, " +
										"price_discount_amount=:pda, price_discount_percent=:pdp, " +
										"total_amount=:ta, total_tax_amount=:tta, total_tax_percent=:ttp, " +
										"standard_term_days=:standardTermDays, agreed_term_days=:agreedTermDays " +
					 	   "WHERE pdv_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId() );
			params.addValue("ppdId", model.getProposalDetail().getId());
			params.addValue("vheId", ( model.getVehicle() != null ? model.getVehicle().getId() : null ));
			params.addValue("mdlId", ( model.getModel() != null ? model.getModel().getId() : null ));
			params.addValue("modelYear", model.getModelYear());
			params.addValue("version", model.getVersion());
			params.addValue("prpId", model.getPriceProduct().getId());
			params.addValue("pad", model.getProductAmountDiscount() );
			params.addValue("ppd", model.getProductPercentDiscount() );
			params.addValue("pfp", model.getProductFinalPrice() );
			params.addValue("op", model.getOverPrice() );
			params.addValue("oppda", model.getOverPricePartnerDiscountAmount() );
			params.addValue("oppdp", model.getOverPricePartnerDiscountPercent() );
			params.addValue("pda", model.getPriceDiscountAmount() );
			params.addValue("pdp", model.getPriceDiscountPercent() );
			params.addValue("ta", model.getTotalAmount() );
			params.addValue("tta", model.getTotalTaxAmount() );
			params.addValue("ttp", model.getTotalTaxPercent() );
			params.addValue("standardTermDays", model.getStandardTermDays());
			params.addValue("agreedTermDays", model.getAgreedTermDays());

			
			log.trace( "[QUERY] proposal_detail_vehicle.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o detalhe do veículo da proposta: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o detalhe do veículo da proposta.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM proposal_detail_vehicle WHERE pdv_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposal_detail_vehicle.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o detalhe do veículo da proposta." , e );
			throw new AppException( "Erro ao excluir o detalhe do veículo da proposta.", e );
		}
	}
	
	@Override
	public boolean hasVehicleItemRelationship(Integer pdvId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT pdv_id FROM proposal_detail_vehicle_item WHERE pdv_id = :pdvId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "pdvId", pdvId );

			log.trace( "[QUERY] proposal_detail_vehicle.hasVehicleItemRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com parceiro." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com parceiro.", e );
		}
	}

	@Override
	public Optional<ProposalDetailVehicle> getDetailVehicleByDetail(ProposalDetailVehicle model) throws AppException {
		try {
			boolean hasFilter = false;
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pdv.*, ");
			query.append("	pl.prl_id, pl.name as price_list ");
			query.append("FROM proposal_detail_vehicle pdv ");
			query.append("inner join price_product as pp on pdv.ppr_id = pp.ppr_id ");
			query.append("inner join price_list as pl on pp.prl_id = pl.prl_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND pdv.pdv_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if(model.getProposalDetail() != null && model.getProposalDetail().getId() != null) {
					query.append(" AND pdv.ppd_id = :ppdId ");
					params.addValue("ppdId", model.getProposalDetail().getId());
					hasFilter = true;
				}
				
				if(model.getVehicle() != null && model.getVehicle().getId() != null) {
					query.append(" AND pdv.vhe_id = :vheId ");
					params.addValue("vheId", model.getVehicle().getId());
					hasFilter = true;
				}
				
				if(model.getModel() != null && model.getModel().getId() != null) {
					query.append(" AND pdv.mdl_id = :mdlId ");
					params.addValue("mdlId", model.getModel().getId());
					hasFilter = true;
				}
				
				if(model.getPriceProduct() != null && model.getPriceProduct().getId() != null) {
					query.append(" AND pdv.ppr_id = :prpId ");
					params.addValue("prpId", model.getPriceProduct().getId());
					hasFilter = true;
				}
			}

			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] proposal_detail_vehicle.getDetailVehicleByDetail: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalDetailVehicleMapper() ) );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os detalhes do veiculo da proposta.", e );
			throw new AppException( "Erro ao buscar os detalhes do veiculo da proposta.", e );
		}
	}
}
