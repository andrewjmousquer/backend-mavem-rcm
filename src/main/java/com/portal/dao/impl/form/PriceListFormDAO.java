package com.portal.dao.impl.form;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import com.portal.config.BaseDAO;
import com.portal.dao.IPriceListFormDAO;
import com.portal.dto.ChannelDTO;
import com.portal.dto.PriceListDTO;
import com.portal.dto.form.PriceListDuplicateItemDTO;
import com.portal.exceptions.AppException;
import com.portal.utils.PortalNumberUtils;
import com.portal.utils.PortalTimeUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PriceListFormDAO extends BaseDAO implements IPriceListFormDAO {

	@Override
	public Optional<List<PriceListDuplicateItemDTO>> findProductsOverlay( LocalDateTime start, LocalDateTime end ) throws AppException {
		
		try {
			
			String query = 	"SELECT  prl.prl_id AS priceListId, " +
							"		 prl.name, " +
							"        prl.start_date, " +
							"        prl.end_date, " +
							"        prl.chn_id AS channelId, " +
							"        plp.ptn_id AS partnerId, " +
							"        prm.prd_id AS productId, " +
							"        prm.prm_id AS productModelId, " +
							"        prm.mdl_id AS modelId " +
							"FROM price_list prl " +
							"INNER JOIN price_list_partner plp ON plp.prl_id = prl.prl_id " +
							"INNER JOIN price_product ppr ON ppr.prl_id = prl.prl_id " +
							"INNER JOIN product_model prm ON prm.prm_id = ppr.prm_id " +
							"WHERE ( ( start_date >= :startDate AND start_date <= :endDate ) OR ( :startDate >= start_date AND :startDate <= end_date ) ) " +
							"AND prl.all_partners = 0 ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("startDate", PortalTimeUtils.localDateTimeFormat( start, "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue("endDate", PortalTimeUtils.localDateTimeFormat( end, "yyyy-MM-dd HH:mm:ss" ) );

			log.trace( "[QUERY] priceListForm.findProductsOverlay: {} [PARAMS]: {}", query, params.getValues() );

			List<PriceListDuplicateItemDTO> list = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {
				
				List<PriceListDuplicateItemDTO> qList = new ArrayList<>();
				
				while( rs.next() ) {
					ChannelDTO channel = ChannelDTO.builder()
														.id( rs.getInt( "channelId" ) )
														.build();
					
					PriceListDTO priceList = PriceListDTO.builder()
															.id( rs.getInt( "priceListId" ) )
															.name( rs.getString( "name" ) )
															.channel( channel )
															.build();

					PriceListDuplicateItemDTO duplicateItem = PriceListDuplicateItemDTO.builder()
																					.priceList( priceList )
																					.channelId( channel.getId() )
																					.partnerId( rs.getInt( "partnerId" ) )
																					.productModelId( rs.getInt( "productModelId" ) )
																					.build();
					
					qList.add(duplicateItem);
				}
				
				return qList;
			});
			
			return Optional.ofNullable( list );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar as sobreposições da lista de preços de produtos.", e );
			throw new AppException( "Erro ao consultar as sobreposições da lista de preços de produtos.", e );
		}
	}
	
	@Override
	public Optional<List<PriceListDuplicateItemDTO>> findItemOverlay( LocalDateTime start, LocalDateTime end ) throws AppException {
		
		try {
			
			String query = 	"SELECT  prl.prl_id AS priceListId, " +
							"		 prl.name, " +
							"        prl.start_date, " +
							"        prl.end_date, " +
							"        prl.chn_id AS channelId, " +
							"        plp.ptn_id AS partnerId, " +
							"        pci.itm_id AS itemId " +
							"FROM price_list prl " +
							"INNER JOIN price_list_partner plp ON plp.prl_id = prl.prl_id " +
							"INNER JOIN price_item pci ON pci.prl_id = prl.prl_id " +
							"WHERE ( ( start_date >= :startDate AND start_date <= :endDate ) OR ( :startDate >= start_date AND :startDate <= end_date ) ) " +
							"AND prl.all_partners = 0 ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("startDate", PortalTimeUtils.localDateTimeFormat( start, "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue("endDate", PortalTimeUtils.localDateTimeFormat( end, "yyyy-MM-dd HH:mm:ss" ) );

			log.trace( "[QUERY] priceListForm.findItemOverlay: {} [PARAMS]: {}", query, params.getValues() );

			List<PriceListDuplicateItemDTO> list = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {
				
				List<PriceListDuplicateItemDTO> qList = new ArrayList<>();
				
				while( rs.next() ) {
					ChannelDTO channel = ChannelDTO.builder()
														.id( rs.getInt( "channelId" ) )
														.build();
					
					PriceListDTO priceList = PriceListDTO.builder()
															.id( rs.getInt( "priceListId" ) )
															.name( rs.getString( "name" ) )
															.channel( channel )
															.build();

					PriceListDuplicateItemDTO duplicateItem = PriceListDuplicateItemDTO.builder()
																					.priceList( priceList )
																					.channelId( channel.getId() )
																					.partnerId( rs.getInt( "partnerId" ) )
																					.itemId( rs.getInt( "itemId" ) )
																					.build();
					
					qList.add(duplicateItem);
				}
				
				return qList;
			});
			
			return Optional.ofNullable( list );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar as sobreposições da lista de preços de item.", e );
			throw new AppException( "Erro ao consultar as sobreposições da lista de preços de item.", e );
		}
	}
	
	@Override
	public Optional<List<PriceListDuplicateItemDTO>> findItemModelOverlay( LocalDateTime start, LocalDateTime end ) throws AppException {
		
		try {
			
			String query = 	"SELECT  prl.prl_id AS priceListId, " +
							"		 prl.name, " +
							"        prl.start_date, " +
							"        prl.end_date, " +
							"        prl.chn_id AS channelId, " +
							"        plp.ptn_id AS partnerId, " +
							"        pim.itm_id AS itemId, " +
							"        pim.brd_id AS brandId, " +
							"        pim.imd_id AS itemModelId, " +
							"        pim.all_brands, " + 
							"        pim.all_models " + 
							"FROM price_list prl " +
							"INNER JOIN price_list_partner plp ON plp.prl_id = prl.prl_id " +
							"INNER JOIN price_item_model pim ON pim.prl_id = prl.prl_id " +
							"WHERE ( ( start_date >= :startDate AND start_date <= :endDate ) OR ( :startDate >= start_date AND :startDate <= end_date ) ) " +
							"AND prl.all_partners = 0 ";
			
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("startDate", PortalTimeUtils.localDateTimeFormat( start, "yyyy-MM-dd HH:mm:ss" ) );
			params.addValue("endDate", PortalTimeUtils.localDateTimeFormat( end, "yyyy-MM-dd HH:mm:ss" ) );

			log.trace( "[QUERY] priceListForm.findItemModelOverlay: {} [PARAMS]: {}", query, params.getValues() );

			List<PriceListDuplicateItemDTO> list = this.getJdbcTemplatePortal().query(query, params, (ResultSet rs) -> {
				
				List<PriceListDuplicateItemDTO> qList = new ArrayList<>();
				
				while( rs.next() ) {
					ChannelDTO channel = ChannelDTO.builder()
														.id( rs.getInt( "channelId" ) )
														.build();
					
					PriceListDTO priceList = PriceListDTO.builder()
															.id( rs.getInt( "priceListId" ) )
															.name( rs.getString( "name" ) )
															.channel( channel )
															.build();

					PriceListDuplicateItemDTO duplicateItem = PriceListDuplicateItemDTO.builder()
																					.priceList( priceList )
																					.channelId( channel.getId() )
																					.partnerId( rs.getInt( "partnerId" ) )
																					.itemId( rs.getInt( "itemId" ) )
																					.itemModelId( rs.getInt( "itemModelId" ) )
																					.brandId( rs.getInt( "brandId" ) )
																					.allBrands( PortalNumberUtils.intToBoolean( rs.getInt( "all_brands" ) ) )
																					.allModels( PortalNumberUtils.intToBoolean( rs.getInt( "all_models" ) ) )
																					.build();
					
					qList.add(duplicateItem);
				}
				
				return qList;
			});
			
			return Optional.ofNullable( list );
		
		} catch( EmptyResultDataAccessException e ) {
			return Optional.empty();
			
		} catch (Exception e) {
			log.error( "Erro ao consultar as sobreposições da lista de preços de item.", e );
			throw new AppException( "Erro ao consultar as sobreposições da lista de preços de item.", e );
		}
	}

}
