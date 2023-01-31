package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.PriceItem;
import com.portal.model.PriceItemModel;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.ProposalDetailVehicleItem;
import com.portal.model.Seller;

public class ProposalDetailVehicleItemMapper implements RowMapper<ProposalDetailVehicleItem>{

	@Override
	public ProposalDetailVehicleItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		ProposalDetailVehicle proposalDetailVehicle = new ProposalDetailVehicle();
		proposalDetailVehicle.setId(rs.getInt("pdv_id"));
		
		PriceItemModel itemPriceModel = new PriceItemModel();
		itemPriceModel.setId(rs.getInt("pim_id"));
		
		PriceItem itemPrice = new PriceItem();
		itemPrice.setId(rs.getInt("pci_id"));
		
		Seller seller =  new Seller();
		seller.setId(rs.getInt("seller_id"));
		
		return ProposalDetailVehicleItem.builder()
				.id( rs.getInt("pdvi_id") )
				.proposalDetailVehicle(proposalDetailVehicle)
				.itemPriceModel(itemPriceModel)
				.itemPrice(itemPrice)
				.seller(seller)
				.amountDiscount(rs.getDouble("amount_discount"))
				.percentDiscount(rs.getDouble("percent_discount"))
				.finalPrice(rs.getDouble("final_price"))
				.forFree(rs.getBoolean("for_free"))
				.build();
	}

	
}
