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
import com.portal.dao.IProposalDetailDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalDetailMapper;
import com.portal.model.ProposalDetail;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalDetailDAO extends BaseDAO implements IProposalDetailDAO {

	@Override
	public List<ProposalDetail> listAll(Pageable pageable) throws AppException {
		try {
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");	
			}
			
			Order order = Order.desc( "ppd_id" );
			if( pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			String query = 	"SELECT  ppd.*, " +
									"s.sel_id AS seller_id, " +
									"p.name AS seller_name " +
							"FROM proposal_detail ppd " +
							"INNER JOIN seller s ON s.sel_id = ppd.sel_id " +
							"INNER JOIN person p ON s.per_id = p.per_id " +
							"ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " +
							"LIMIT " + pageable.getPageSize() + " " +
							"OFFSET " + pageable.getPageNumber();

			log.trace( "[QUERY] proposalDetail.listAll: {} [PARAMS]: {}", query );

			return this.getJdbcTemplatePortal().query( query, new ProposalDetailMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao listar os detalhes do detalhe da proposta.", e );
			throw new AppException( "Erro ao listar os detalhes do detalhe da proposta.", e );
		}
	}

	/**
	 * @deprecated Usar a função {@link #listAll(Pageable)}
	 */
	@Override
	public List<ProposalDetail> list() throws AppException {
		return this.listAll(null);
	}
	
	@Override
	public List<ProposalDetail> find(ProposalDetail model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "ppd_id");	
			}
			
			Order order = Order.asc( "ppd_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT ppd.*, ");
			query.append("		 s.sel_id AS seller_id, " );
			query.append("		 p.name AS seller_name, " );
			query.append("		 c.chn_id as channel_id, " );
			query.append("		 c.name as channel_name," );
			query.append("		 c.has_partner, " );
			query.append("		 c.has_internal_sale, " );
			query.append("		 par.ptn_id as partner_id, " );
			query.append("		 part.name as per_name " );
			query.append("FROM proposal_detail ppd ");
			query.append("INNER JOIN seller s ON s.sel_id = ppd.sel_id " );
			query.append("INNER JOIN person p ON s.per_id = p.per_id " );
			query.append("INNER JOIN channel c on c.chn_id = ppd.chn_id " );
			query.append("left join partner par on par.ptn_id = ppd.ptn_id " );
			query.append("left join person part on part.per_id = par.entity_per_id " );
			query.append("where 1=1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND ppd.ppd_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if( model.getSeller() != null && model.getSeller().getId() != null ) {
					query.append(" AND ppd.sel_id = :sellerId ");
					params.addValue("sellerId", model.getSeller().getId() );
					hasFilter = true;
				}
				
				if( model.getInternSale() != null && model.getInternSale().getId() != null ) {
					query.append(" AND ppd.intern_sale_sel_id = :internSaleId ");
					params.addValue("internSaleId", model.getInternSale().getId() );
					hasFilter = true;
				}
				
				if(model.getProposal() != null) {
					if(model.getProposal().getId() != null) {
						query.append(" AND ppd.pps_id = :ppsId ");
						params.addValue("ppsId", model.getProposal().getId() );
						hasFilter = true;
					}
				}
			}
						
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] proposalDetail.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalDetailMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os detalhes da proposta.", e );
			throw new AppException( "Erro ao buscar os detalhes da proposta.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(ProposalDetail, Pageable)}
	 */
	@Override
	public Optional<ProposalDetail> find(ProposalDetail model) throws AppException {
		List<ProposalDetail> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	/**
	 * @deprecated Usar a função {@link #search(ProposalDetail, Pageable)}
	 */
	@Override
	public List<ProposalDetail> search(ProposalDetail model) throws AppException {
		return this.find(model, null);
	}

	@Override
	public Optional<ProposalDetail> getById(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT ppd.*, ");
			query.append("		 s.sel_id AS seller_id, " );
			query.append("		 p.name AS seller_name, " );
			query.append("		 c.chn_id as channel_id, " );
			query.append("		 c.name as channel_name," );
			query.append("		 c.has_partner, " );
			query.append("		 c.has_internal_sale, " );
			query.append("		 par.ptn_id as partner_id, " );
			query.append("		 par.additional_term as additional_term, " );
			query.append("		 part.name as per_name " );
			query.append("FROM proposal_detail ppd ");
			query.append("INNER JOIN seller s ON s.sel_id = ppd.sel_id " );
			query.append("INNER JOIN person p ON s.per_id = p.per_id " );
			query.append("INNER JOIN channel c on c.chn_id = ppd.chn_id " );
			query.append("left join partner par on par.ptn_id = ppd.ptn_id " );
			query.append("left join person part on part.per_id = par.entity_per_id " );
			query.append("WHERE ppd.ppd_id = :id ");
			query.append("LIMIT 1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposalDetail.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalDetailMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o detalhe da proposta.", e );
			throw new AppException( "Erro ao consultar o detalhe da proposta.", e );
		}
	}

	@Override
	public Optional<ProposalDetail> save(ProposalDetail model) throws AppException {
		try {
			String query = "INSERT INTO proposal_detail(ppd_id, pps_id, sel_id, intern_sale_sel_id, chn_id, ptn_id, usr_id, purchase_order_service, purchase_order_product, purchase_order_documentation ) " +
						   "VALUES (NULL, :ppsId, :sellerId, :internSaleId, :chnId, :ptnId, :usrId, :purchaseOrderService, :purchaseOrderProduct, :purchaseOrderDocumentation)";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("ppsId", (model.getProposal() == null ? null : model.getProposal().getId() ) );
			params.addValue("sellerId", (model.getSeller() == null ? null : model.getSeller().getId() ) );
			params.addValue("internSaleId", (model.getInternSale() == null ? null : model.getInternSale().getId() ) );
			params.addValue("chnId", (model.getChannel().getId() == null ? null : model.getChannel().getId() ) );
			params.addValue("ptnId", (model.getPartner() == null ? null : model.getPartner().getId() ) );
			params.addValue("usrId", (model.getUser() == null ? null : model.getUser().getId() ) );
			params.addValue("purchaseOrderService", (model.getPurchaseOrderService() == null ? null : model.getPurchaseOrderService() ) );
			params.addValue("purchaseOrderProduct", (model.getPurchaseOrderProduct() == null ? null : model.getPurchaseOrderProduct() ) );
			params.addValue("purchaseOrderDocumentation", (model.getPurchaseOrderDocumentation() == null ? null : model.getPurchaseOrderDocumentation() ) );

	
			log.trace( "[QUERY] proposalDetail.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o detalhe da proposta: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o detalhe da proposta.", e);
		}
	}

	@Override
	public Optional<ProposalDetail> update(ProposalDetail model) throws AppException {
		try {
			String query = "UPDATE proposal_detail SET pps_id=:ppsId, sel_id=:sellerId, intern_sale_sel_id=:internSaleId, chn_id=:chnId, ptn_id=:ptnId, " +
					 	   "purchase_order_service = :purchaseOrderService, "+
					       "purchase_order_product = :purchaseOrderProduct, "+
					       "purchase_order_documentation = :purchaseOrderDocumentation " +
					       "WHERE ppd_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId() );
			params.addValue("ppsId", (model.getProposal() == null ? null : model.getProposal().getId() ) );
			params.addValue("sellerId", (model.getSeller() == null ? null : model.getSeller().getId() ) );
			params.addValue("internSaleId", (model.getInternSale() == null ? null : model.getInternSale().getId() ) );
			params.addValue("chnId", (model.getChannel().getId() == null ? null : model.getChannel().getId() ) );
			params.addValue("ptnId", (model.getPartner() == null ? null : model.getPartner().getId() ) );
			params.addValue("purchaseOrderService", (model.getPurchaseOrderService() == null ? null : model.getPurchaseOrderService() ) );
			params.addValue("purchaseOrderProduct", (model.getPurchaseOrderProduct() == null ? null : model.getPurchaseOrderProduct() ) );
			params.addValue("purchaseOrderDocumentation", (model.getPurchaseOrderDocumentation() == null ? null : model.getPurchaseOrderDocumentation() ) );

			
			log.trace( "[QUERY] proposalDetail.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o detalhe da proposta: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o detalhe da proposta.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM proposal_detail WHERE ppd_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposalDetail.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o detalhe da proposta" , e );
			throw new AppException( "Erro ao excluir o detalhe da proposta.", e );
		}

	}

	@Override
	public Optional<ProposalDetail> getDetailByProposal(ProposalDetail model) throws AppException {
		try {
			boolean hasFilter = false;
			
			StringBuilder query = new StringBuilder();

			query.append("SELECT ppd.*, ");
			query.append("		 s.sel_id AS seller_id, " );
			query.append("		 p.name AS seller_name, " );
			query.append("		 c.chn_id as channel_id, " );
			query.append("		 c.name as channel_name," );
			query.append("		 c.has_partner, " );
			query.append("		 c.has_internal_sale, " );
			query.append("		 par.ptn_id as partner_id, " );
			query.append("		 par.additional_term as additional_term, " );
			query.append("		 part.name as per_name " );
			query.append("FROM proposal_detail ppd ");
			query.append("INNER JOIN seller s ON s.sel_id = ppd.sel_id " );
			query.append("INNER JOIN person p ON s.per_id = p.per_id " );
			query.append("INNER JOIN channel c on c.chn_id = ppd.chn_id " );
			query.append("left join partner par on par.ptn_id = ppd.ptn_id " );
			query.append("left join person part on part.per_id = par.entity_per_id " );
			
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND ppd.ppd_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
		
				if( model.getSeller() != null && model.getSeller().getId() != null ) {
					query.append(" AND ppd.sel_id = :sellerId ");
					params.addValue("sellerId", model.getSeller().getId() );
					hasFilter = true;
				}
				
				if( model.getInternSale() != null && model.getInternSale().getId() != null ) {
					query.append(" AND ppd.intern_sale_sel_id = :internSaleId ");
					params.addValue("internSaleId", model.getInternSale().getId() );
					hasFilter = true;
				}
				
				if(model.getProposal() != null) {
					if(model.getProposal().getId() != null) {
						query.append(" AND ppd.pps_id = :ppsId ");
						params.addValue("ppsId", model.getProposal().getId() );
						hasFilter = true;
					}
				}
			}
						
			query.append( "LIMIT 1 " );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] proposalDetail.getDetailByProposal: {} [PARAMS]: {}", query, params.getValues() );
			
			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalDetailMapper() ) );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os detalhes da proposta.", e );
			throw new AppException( "Erro ao buscar os detalhes da proposta.", e );
		}
	}
}
