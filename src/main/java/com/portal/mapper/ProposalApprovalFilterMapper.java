package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;

import com.portal.dto.ProposalApprovalListDTO;
import com.portal.model.Person;
import com.portal.model.ProposalDetailVehicle;
import com.portal.model.Seller;

public class ProposalApprovalFilterMapper implements RowMapper<ProposalApprovalListDTO> {

    @Override
    public ProposalApprovalListDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

        LocalDate createDate = null;
        Date createDateDB = rs.getDate("create_date" );
        if( createDateDB != null){
            createDate = createDateDB.toLocalDate();
        }

        LocalDate validityDate = null;
        Date validityDateDB = rs.getDate("validity_date");
        if(validityDateDB != null){
            validityDate = validityDateDB.toLocalDate();
        }
        
        ProposalDetailVehicle proposalDetailVehicle = ProposalDetailVehicle.builder()
        		.id(rs.getInt("pdv_id"))
        		.productAmountDiscount(rs.getDouble("product_amount_discount"))
        		.priceDiscountAmount(rs.getDouble("price_discount_amount"))
        		.build();
        		

        Seller seller = Seller.builder().id(rs.getInt("seller_id")).person(Person.builder().id(rs.getInt("seller_per_id")).name(rs.getString("seller_name")).build()).build();

        return ProposalApprovalListDTO.builder()
                .id( rs.getInt("pps_id"))
                .num( rs.getLong("num_proposta") )
                .proposalNumber( rs.getString("proposal_number") )
                .proposalDetailVehicle(proposalDetailVehicle)
                .orderNumber( rs.getInt("order_number") )
                .client( rs.getString("client") )
                .partner( rs.getString("partner") )
                .executive(seller)
                .brandModel( rs.getString("brandModel") )
                .createDate(createDate)
                .validityDate(validityDate)
                .totalPrice( rs.getDouble("total_price"))
                .build();
    }
}
