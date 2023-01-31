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
import com.portal.dao.IProposalDetailVehicleItemDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.ProposalDetailVehicleItemMapper;
import com.portal.model.ProposalDetailVehicleItem;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ProposalDetailVehicleItemDAO extends BaseDAO implements IProposalDetailVehicleItemDAO {

	@Override
	public List<ProposalDetailVehicleItem> find(ProposalDetailVehicleItem model, Pageable pageable) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pdvi_id");	
			}
			
			Order order = Order.asc( "pdvi_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pdvi.* ");
			query.append("FROM proposal_detail_vehicle_item pdvi ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				
				if(model.getProposalDetailVehicle() != null) {
					if(model.getProposalDetailVehicle().getId() > 0) {
						query.append(" AND pdvi.pdv_id = :id ");
						params.addValue("id", model.getProposalDetailVehicle().getId());
						hasFilter = true;
					}
				}
			}

			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] proposal_detail_vehicle_item.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalDetailVehicleItemMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os detalhes do item do veiculo da proposta.", e );
			throw new AppException( "Erro ao buscar os detalhes do item do veiculo da proposta.", e );
		}
	}

	@Override
	public Optional<ProposalDetailVehicleItem> getById(Integer id) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pdvi.* ");
			query.append("FROM proposal_detail_vehicle_item pdvi ");
			query.append("WHERE pdvi.pdvi_id = :id ");
			query.append("LIMIT 1 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposal_detail_vehicle.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query.toString(), params, new ProposalDetailVehicleItemMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o detalhe do veiculo da proposta.", e );
			throw new AppException( "Erro ao consultar o detalhe do veiculo da proposta.", e );
		}
	}

	@Override
	public List<ProposalDetailVehicleItem> list() throws AppException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProposalDetailVehicleItem> search(ProposalDetailVehicleItem model) throws AppException {
		try {
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pdvi.* ");
			query.append("FROM proposal_detail_vehicle_item pdvi ");
			query.append("WHERE pdv_id > 0 ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if(model.getProposalDetailVehicle() != null) {
					if(model.getProposalDetailVehicle().getId() > 0) {
						query.append(" AND pdvi.pdv_id = :id ");
						params.addValue("id", model.getProposalDetailVehicle().getId());
					}
				}
			}
			
			log.trace( "[QUERY] proposal_detail_vehicle_item.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new ProposalDetailVehicleItemMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar os detalhes do item do veiculo da proposta.", e );
			throw new AppException( "Erro ao buscar os detalhes do item do veiculo da proposta.", e );
		}
	}

	@Override
	public Optional<ProposalDetailVehicleItem> save(ProposalDetailVehicleItem model) throws AppException {
		try {
			String query = "INSERT INTO proposal_detail_vehicle_item (pdvi_id, amount_discount, percent_discount, final_price, " +
																"for_free, pdv_id, seller_id, pci_id, pim_id) " +
							"VALUES (NULL, :amountdiscount, :percentdiscount, :finalprice, :forfree, :pdvid, :sellerid, :pciid, :pimid) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("amountdiscount", model.getAmountDiscount());
			params.addValue("percentdiscount", model.getPercentDiscount());
			params.addValue("finalprice", model.getFinalPrice());
			params.addValue("forfree", PortalNumberUtils.booleanToInt( model.getForFree() ) );
			params.addValue("pdvid", model.getProposalDetailVehicle().getId() );
			params.addValue("sellerid", model.getSeller().getId() );
			params.addValue("pciid", model.getItemPrice() != null ? model.getItemPrice().getId() : null );
			params.addValue("pimid", model.getItemPriceModel() != null ? model.getItemPriceModel().getId() : null );
	
			log.trace( "[QUERY] proposal_detail_vehicle_item.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o detalhe do item do veículo da proposta: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o detalhe do item do veículo da proposta.", e);
		}
	}

	@Override
	public Optional<ProposalDetailVehicleItem> update(ProposalDetailVehicleItem model) throws AppException {
		try {
			String query = "UPDATE proposal_detail_vehicle_item SET " +     
										"amount_discount=:amountdiscount, percent_discount=:percentdiscount, final_price=:finalprice, " +
										"for_free=:forfree, pdv_id=:pdvid, seller_id=:sellerid, pci_id=:pciid, pim_id=:pimid " +
					 	   "WHERE pdvi_id=:id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("id", model.getId());
			params.addValue("amountdiscount", model.getAmountDiscount());
			params.addValue("percentdiscount", model.getPercentDiscount());
			params.addValue("finalprice", model.getFinalPrice());
			params.addValue("forfree", PortalNumberUtils.booleanToInt( model.getForFree() ) );
			params.addValue("pdvid", model.getProposalDetailVehicle().getId() );
			params.addValue("sellerid", model.getSeller().getId() );
			params.addValue("pciid", model.getItemPrice() != null ? model.getItemPrice().getId() : null );
			params.addValue("pimid", model.getItemPriceModel() != null ? model.getItemPriceModel().getId() : null );
			
			log.trace( "[QUERY] proposal_detail_vehicle_item.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o detalhe do item do veículo da proposta: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o detalhe do item do veículo da proposta.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM proposal_detail_vehicle_item WHERE pdvi_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] proposal_detail_vehicle_item.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir proposal_detail_vehicle_item" , e );
			throw new AppException( "Erro ao excluir proposal_detail_vehicle_item.", e );
		}

	}

	@Override
	public Optional<ProposalDetailVehicleItem> find(ProposalDetailVehicleItem model) throws AppException {
		// TODO Auto-generated method stub
		return null;
	}

}
