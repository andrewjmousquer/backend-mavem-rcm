package com.portal.mapper;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Classifier;
import com.portal.model.PaymentMethod;
import com.portal.model.PaymentRule;
import com.portal.model.ProposalDetail;
import com.portal.model.ProposalPayment;
import com.portal.utils.PortalNumberUtils;

public class ProposalPaymentMapper implements RowMapper<ProposalPayment> {

    @Override
    public ProposalPayment mapRow(ResultSet rs, int rowNum) throws SQLException {

        Classifier payer = Classifier.builder()
                .id(rs.getInt("payer_cla_id"))
                .value(rs.getString("payer_value"))
                .type(rs.getString("payer_type"))
                .label(rs.getString("payer_label"))
                .build();

        Classifier event = Classifier.builder()
                .id(rs.getInt("event_cla_id"))
                .value(rs.getString("event_value"))
                .type(rs.getString("event_type"))
                .label(rs.getString("event_label"))
                .build();

        PaymentMethod paymentMethod = PaymentMethod.builder().id(rs.getInt("pym_id")).build();
        PaymentRule paymentRule = PaymentRule.builder().id(rs.getInt("pyr_id")).build();
        ProposalDetail proposalDetail = ProposalDetail.builder().id(rs.getInt("ppd_id")).build();

        return ProposalPayment.builder()
                .id(rs.getInt("ppy_id"))
                .paymentAmount(rs.getDouble("payment_amount"))
                .dueDate(rs.getDate("due_date"))
                .installmentAmount(rs.getDouble("installment_amount"))
                .interest(rs.getDouble("interest"))
                .proposalDetail(proposalDetail)
                .paymentMethod(paymentMethod)
                .paymentRule(paymentRule)
                .preApproved(PortalNumberUtils.intToBoolean(rs.getInt("pre_approved")))
                .antecipatedBilling(PortalNumberUtils.intToBoolean(rs.getInt("antecipated_billing")))
                .payer(payer)
                .event(event)
                .position(rs.getInt("position"))
                .quantityDays(rs.getInt("quantity_days"))
                .carbonBilling(PortalNumberUtils.intToBoolean(rs.getInt("carbon_billing")))
                .build();
    }
}
