package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.SalesOrderState;
import com.portal.model.Classifier;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.SalesOrder;
import com.portal.model.UserModel;
import com.portal.utils.PortalNumberUtils;

public class SalesOrderMapper implements RowMapper<SalesOrder> {
    @Override
    public SalesOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
    	
    	
		Classifier personType = Classifier.builder()
				.id(rs.getInt("per_cla_id"))
				.value(rs.getString("per_cla_value"))
				.type(rs.getString("per_cla_type"))
				.label(rs.getString("per_cla_label"))
				.build();
    	
        Person person = Person.builder()
                .id(rs.getInt("per_id"))
                .name(rs.getString("per_name"))
                .jobTitle(rs.getString("job_title"))
                .cpf(rs.getString("cpf"))
                .cnpj(rs.getString("cnpj"))
                .rg(rs.getString("rg"))
                .rne(rs.getString("rne"))
                .classification(personType)
                .build();

    	
        UserModel user = UserModel.builder()
        		.id(rs.getInt("usr_id"))
        		.username(rs.getString("username"))
        		.person(person)
        		.enabled(PortalNumberUtils.intToBoolean( rs.getInt( "enabled" )))
        		.build();
        
        
		Classifier salesOrderState = Classifier.builder()
				.id(rs.getInt("status_cla_id"))
				.value(rs.getString("status_cla_value"))
				.type(rs.getString("status_cla_type"))
				.label(rs.getString("status_cla_label"))
				.build();

        Proposal proposal = Proposal.builder()
                .id(rs.getInt("pps_id"))
                .num(rs.getLong("pps_num"))
                .cod(rs.getString("pps_cod"))
                .createDate(rs.getTimestamp("pps_create_date").toLocalDateTime())
                .build();

        return SalesOrder.builder()
        		.id(rs.getInt("sor_id"))
                .proposal(proposal)
                .user(user)
                .orderNumber(rs.getInt("order_number"))
                .jiraKey(rs.getString("jira_key"))
                .status(SalesOrderState.getById(rs.getInt("status_cla_id")))
                .statusClassification(salesOrderState)
                .build();
    }
}
