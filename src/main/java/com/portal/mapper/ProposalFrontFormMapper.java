package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.portal.model.Classifier;
import com.portal.model.Person;
import com.portal.model.Seller;
import com.portal.utils.PortalNumberUtils;
import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.ProposalState;
import com.portal.model.ProposalFrontForm;

public class ProposalFrontFormMapper implements RowMapper<ProposalFrontForm> {

    @Override
    public ProposalFrontForm mapRow(ResultSet rs, int rowNum) throws SQLException {

//        LocalDateTime createDate = null;
//        Date createDateDB = rs.getDate("create_date");
//        if (createDateDB != null) {
//            createDate = createDateDB.toLocalDate();
//        }

        LocalDate validityDate = null;
        Date validityDB = rs.getDate("validity_date");
        if (validityDB != null) {
            validityDate = validityDB.toLocalDate();
        }

        Seller seller = Seller.builder().id(rs.getInt("seller_id")).person(Person.builder().id(rs.getInt("seller_per_id")).name(rs.getString("seller_name")).build()).build();

        Classifier proposalStatus = Classifier.builder()
                .id(rs.getInt("status_cla_id"))
                .value(rs.getString("status_cla_value"))
                .type(rs.getString("status_cla_type"))
                .label(rs.getString("status_cla_label"))
                .build();

        return ProposalFrontForm.builder()
                .id(rs.getInt("pps_id"))
                .num(rs.getLong("num_proposta"))
                .proposalNumber(rs.getString("proposal_number"))
                .cod(rs.getString("cod"))
                .immediateDelivery(PortalNumberUtils.intToBoolean(rs.getInt("immediate_delivery")))
                .orderNumber(rs.getInt("order_number") > 0 ? rs.getInt("order_number") : null)
                .statusId(rs.getInt("status_cla_id"))
                .status(ProposalState.getById(rs.getInt("status_cla_id")))
                .statusCla(proposalStatus)
                .client(rs.getString("client"))
                .partner(rs.getString("partner"))
                .executive(seller)
                .brandModel(rs.getString("brandModel"))
                .createDate(rs.getTimestamp("create_date").toLocalDateTime())
                .validityDate(validityDate)
                .totalPrice(rs.getDouble("total_price"))
                .build();
    }

}