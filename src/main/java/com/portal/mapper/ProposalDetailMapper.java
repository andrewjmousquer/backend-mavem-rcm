package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Channel;
import com.portal.model.Partner;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalDetail;
import com.portal.model.Seller;
import com.portal.utils.PortalNumberUtils;

public class ProposalDetailMapper implements RowMapper<ProposalDetail> {

	@Override
	public ProposalDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Proposal proposal = Proposal.builder().id( rs.getInt( "pps_id" ) ).build();
		if( proposal == null || proposal.getId().equals( 0 ) ) {
			proposal = null;
		}
		
		Seller internalSale = Seller.builder().id( rs.getInt( "intern_sale_sel_id" ) ).build();
		if( internalSale == null || internalSale.getId().equals( 0 ) ) {
			internalSale = null;
		}

		Person sellerPerson = Person.builder()
									.name( rs.getString( "seller_name" ) )
									.build();
		
		Seller seller = Seller.builder()
							.id(  rs.getInt("seller_id") )
							.person(sellerPerson)
							.build();
		
		Channel channel = Channel.builder()
								.id( rs.getInt("channel_id") )
								.name( rs.getString("channel_name") )
								.hasPartner( PortalNumberUtils.intToBoolean( rs.getInt("has_partner") ) )
								.hasInternalSale( PortalNumberUtils.intToBoolean( rs.getInt("has_internal_sale") ) )
								.build();
		
		Partner partner = null;
		
		if(channel.getHasPartner()) {
			
			Person person = Person.builder()
					.name( rs.getString( "per_name" ) )
					.build();
			
			partner = Partner.builder()
						.id( rs.getInt("partner_id") )
					    .additionalTerm(rs.getInt("additional_term"))
						.channel( channel )
						.person( person )
						.build();
		}
		
		return ProposalDetail.builder()
						.id( rs.getInt( "ppd_id" ) )
						.proposal( proposal )
						.seller( seller )
						.internSale( internalSale )
						.channel(channel)
						.partner(partner)
				        .purchaseOrderService(rs.getString("purchase_order_service"))
						.purchaseOrderProduct(rs.getString("purchase_order_product"))
						.purchaseOrderDocumentation(rs.getString("purchase_order_documentation"))
						.build();
	}
}
