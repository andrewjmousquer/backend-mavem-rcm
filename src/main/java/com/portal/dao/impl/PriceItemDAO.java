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
import com.portal.dao.IPriceItemDAO;
import com.portal.exceptions.AppException;
import com.portal.mapper.PriceItemMapper;
import com.portal.model.PriceItem;
import com.portal.model.ProductModel;
import com.portal.utils.PortalNumberUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PriceItemDAO extends BaseDAO implements IPriceItemDAO {

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceItem> list() throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}

	/**
	 * @deprecated Essa operação não existe nessa entidade. Use o {@link #find(ProductModel, Pageable)}
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public List<PriceItem> search(PriceItem model) throws AppException {
		throw new UnsupportedOperationException( "Essa operação não existe nessa entidade. Use o find" );
	}
	
	@Override
	public List<PriceItem> find( PriceItem model, Pageable pageable ) throws AppException {
		try {
			boolean hasFilter = false;
			
			if( pageable == null ) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.fromString( "DESC" ), "pci_id");	
			}
			
			Order order = Order.asc( "pci_id" );
			if( pageable.getSort() != null && pageable.getSort().get().findFirst().isPresent() ) {
				order = pageable.getSort().get().findFirst().orElse( order );
			}
			
			StringBuilder query = new StringBuilder();
			
			query.append("SELECT pci.* ");
			query.append("FROM price_item pci ");
			query.append("INNER JOIN price_list prl ON prl.prl_id = pci.prl_id ");
			query.append("INNER JOIN item itm ON itm.itm_id = pci.itm_id ");
			query.append("WHERE :hasFilter ");
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			
			if(model != null) {
				if( model.getId() != null && model.getId() > 0) {
					query.append(" AND pci.pci_id = :id ");
					params.addValue("id", model.getId());
					hasFilter = true;
				}
				
				if( model.getItem() != null && model.getItem().getId() != null) {
					query.append(" AND pci.itm_id = :itmId ");
					params.addValue("itmId",model.getItem().getId());
					hasFilter = true;
				}

				if( model.getPriceList() != null && model.getPriceList().getId() != null) {
					query.append(" AND pci.prl_id = :prlId ");
					params.addValue("prlId",model.getPriceList().getId());
					hasFilter = true;
				}
			}
			
			query.append( "ORDER BY " + order.getProperty() + " " + order.getDirection().name() + " " );
			query.append( "LIMIT " + pageable.getPageSize() + " " );
			query.append( "OFFSET " + pageable.getPageNumber() );
			
			params.addValue( "hasFilter", PortalNumberUtils.booleanToInt( hasFilter ) );
			
			log.trace( "[QUERY] priceItem.find: {} [PARAMS]: {}", query, params.getValues() );
			
			return this.getJdbcTemplatePortal().query( query.toString(), params, new PriceItemMapper() );
			
		} catch (Exception e) {
			log.error( "Erro ao buscar o preço dos itens.", e );
			throw new AppException( "Erro ao buscar o preço dos itens.", e );
		}
	}
	
	/**
	 * @deprecated Usar a função {@link #find(PriceItem, Pageable)}
	 */
	@Override
	public Optional<PriceItem> find(PriceItem model) throws AppException {
		List<PriceItem> models = this.find( model, null );
		return Optional.ofNullable( ( models != null ? models.get(0) : null ) );
	}
	
	@Override
	public Optional<PriceItem> getById(Integer id) throws AppException {
		try {
			
			String query = 	"SELECT * " +
							"FROM price_item pci " +
							"INNER JOIN price_list prl ON prl.prl_id = pci.prl_id " +
							"INNER JOIN item itm ON itm.itm_id = pci.itm_id " +
							"WHERE pci.pci_id = :id " +
							"LIMIT 1";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] priceItem.getById: {} [PARAMS]: {}", query, params.getValues() );

			return Optional.ofNullable( this.getJdbcTemplatePortal().queryForObject( query, params, new PriceItemMapper() ) );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar o preço do item.", e );
			throw new AppException( "Erro ao consultar o preço do item.", e );
		}
	}

	@Override
	public Optional<PriceItem> save(PriceItem model) throws AppException {
		try {
			String query = "INSERT INTO price_item ( pci_id, price, itm_id, prl_id ) VALUES ( NULL, :price, :itmId, :prlId ) ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "price", model.getPrice() );
			params.addValue( "itmId", ( model.getItem() == null ? null : model.getItem().getId() ) );
			params.addValue( "prlId", ( model.getPriceList() == null ? null : model.getPriceList().getId() ) );
	
			log.trace( "[QUERY] priceItem.save: {} [PARAMS]: {}", query, params.getValues() );
			
			KeyHolder keyHolder = new GeneratedKeyHolder();
	
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params, keyHolder);
	
	        model.setId( this.getKey(keyHolder) );
	        
	        return Optional.ofNullable( model );
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar salvar o preço do item: {}", model, e );
			throw new AppException( "Erro ao tentar salvar o preço do item.", e);
		}
	}

	@Override
	public Optional<PriceItem> update(PriceItem model) throws AppException {
		try {
			String query = "UPDATE price_item SET price=:price, itm_id=:itmId, prl_id=:prlId WHERE pci_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", model.getId() );
			params.addValue( "price", model.getPrice() );
			params.addValue( "itmId", ( model.getItem() == null ? null : model.getItem().getId() ) );
			params.addValue( "prlId", ( model.getPriceList() == null ? null : model.getPriceList().getId() ) );
	
			log.trace( "[QUERY] priceItem.update: {} [PARAMS]: {}", query, params.getValues() );
	        this.getNamedParameterJdbcTemplate().update(query.toString(), params);
	        
	        return Optional.ofNullable(model);
	        
		} catch (Exception e) {
			log.error( "Erro ao tentar atualizar o preço do item: {}", model, e );
			throw new AppException( "Erro ao tentar atualizar o preço do item.", e);
		}
	}

	@Override
	public void delete(Integer id) throws AppException {
		try {
			String query = 	"DELETE FROM price_item WHERE pci_id = :id";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "id", id );

			log.trace( "[QUERY] priceItem.delete: {} [PARAMS]: {}", query, params.getValues() );

			this.getJdbcTemplatePortal().update(query, params);
			
		} catch (Exception e) {
			log.error( "Erro ao excluir o preço do item" , e );
			throw new AppException( "Erro ao excluir o preço do item.", e );
		}

	}
	
	@Override
	public boolean hasProposalDetailRelationship(Integer ipcId) throws AppException {
		try {
			String query = 	"SELECT CASE WHEN EXISTS ( " +
								"SELECT pci_id FROM proposal_detail_vehicle_item WHERE pci_id = :ipcId LIMIT 1 " +
							") " +
							"THEN TRUE " +
							"ELSE FALSE " +
							"END AS `exists` ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue( "ipcId", ipcId );

			log.trace( "[QUERY] priceItem.hasProposalDetailRelationship: {} [PARAMS]: {}", query, params.getValues() );

			return this.getJdbcTemplatePortal().queryForObject( query, params, (rs, rowNum) -> rs.getBoolean( "exists" ) );
			
		} catch (Exception e) {
			log.error( "Erro ao verificar a existência de relacionamento com proposta." , e );
			throw new AppException( "Erro ao verificar a existência de relacionamento com proposta.", e );
		}
	}
}
