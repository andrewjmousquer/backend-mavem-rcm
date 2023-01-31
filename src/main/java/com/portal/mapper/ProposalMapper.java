package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.ProposalRisk;
import com.portal.enums.ProposalState;
import com.portal.model.Classifier;
import com.portal.model.Lead;
import com.portal.model.Proposal;
import com.portal.model.SalesOrder;
import com.portal.utils.PortalNumberUtils;

public class ProposalMapper implements RowMapper<Proposal> {

    @Override
    public Proposal mapRow(ResultSet rs, int rowNum) throws SQLException {

        Lead lead = new Lead();
		/*
		Lead lead = Lead.builder().id( rs.getInt( "led_id" ) ).build();
		if( lead == null || lead.getId().equals( 0 ) ) {
			lead = null;
		}
		 */

        Classifier proposalState = Classifier.builder()
                .id(rs.getInt("status_cla_id"))
                .value(rs.getString("status_cla_value"))
                .type(rs.getString("status_cla_type"))
                .label(rs.getString("status_cla_label"))
                .build();

        Classifier proposalRisk = Classifier.builder()
                .id(rs.getInt("risk_cla_id"))
                .value(rs.getString("risk_cla_value"))
                .type(rs.getString("risk_cla_type"))
                .label(rs.getString("risk_cla_label"))
                .build();

        SalesOrder salesOrder = SalesOrder.builder()
                .orderNumber(rs.getInt("order_number"))
                .build();

        LocalDate validityDate= null;
        Date validityDateDB = rs.getDate("validity_date");
        if(validityDateDB != null){
            validityDate = validityDateDB.toLocalDate();
        }

        return Proposal.builder()
                .id(rs.getInt("pps_id"))
                .createDate(rs.getTimestamp("create_date").toLocalDateTime())
                .validityDate(validityDate.atStartOfDay())
                .proposalNumber(rs.getString("proposal_number"))
                .num(rs.getLong("num"))
                .cod(rs.getString("cod"))
                .status(ProposalState.getById(rs.getInt("status_cla_id")))
                .statusClassification(proposalState)
                .finantialContact(rs.getBoolean("finantial_contact"))
                .finantialContactEmail(rs.getString("finantial_contact_email"))
                .finantialContactName(rs.getString("finantial_contact_name"))
                .finantialContactPhone(rs.getString("finantial_contact_phone"))
                .documentContact(rs.getBoolean("document_contact"))
                .documentContactEmail(rs.getString("document_contact_email"))
                .documentContactName(rs.getString("document_contact_name"))
                .documentContactPhone(rs.getString("document_contact_phone"))
                .risk(ProposalRisk.getById(rs.getInt("risk_cla_id")))
                .riskClassification(proposalRisk)
                .immediateDelivery(PortalNumberUtils.intToBoolean(rs.getInt("immediate_delivery")))
                .contract(rs.getString("contract"))
                .salesOrder(salesOrder)
                .lead(lead)
                .commercialContactName(rs.getString("commercial_contact_name"))
                .commercialContactEmail(rs.getString("commercial_contact_email"))
                .commercialContactPhone(rs.getString("commercial_contact_phone"))
                .build();
    }
}
