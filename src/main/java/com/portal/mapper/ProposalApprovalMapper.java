package com.portal.mapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.ProposalState;
import com.portal.model.AddressModel;
import com.portal.model.Classifier;
import com.portal.model.Lead;
import com.portal.model.Person;
import com.portal.model.Proposal;
import com.portal.model.ProposalApproval;

public class ProposalApprovalMapper implements RowMapper<ProposalApproval> {
    @Override
    public ProposalApproval mapRow(ResultSet rs, int rowNum) throws SQLException {

        Lead lead = Lead.builder().id(rs.getInt("led_id")).build();
        if (lead == null || lead.getId().equals(0)) {
            lead = null;
        }

        LocalDate birthdate = null;
        Date birthdateDB = rs.getDate("birthdate");
        if (birthdateDB != null) {
            birthdate = birthdateDB.toLocalDate();
        }

        Classifier personType = Classifier.builder()
                .id(rs.getInt("classification_cla_id"))
                .build();

        Person person = Person.builder()
                .id(rs.getInt("per_id"))
                .name(rs.getString("name"))
                .jobTitle(rs.getString("job_title"))
                .cpf(rs.getString("cpf"))
                .cnpj(rs.getString("cnpj"))
                .rg(rs.getString("rg"))
                .rne(rs.getString("rne"))
                .classification(personType)
                .address(new AddressModel(rs.getInt("add_id")))
                .birthdate(birthdate)
                .build();

        Proposal proposal = Proposal.builder()
                .id(rs.getInt("pps_id"))
                .num(rs.getLong("pps_num"))
                .cod(rs.getString("pps_cod"))
                .lead(lead)
                .createDate(rs.getTimestamp("pps_create_date").toLocalDateTime())
                .build();

        return ProposalApproval.builder()
                .proposal(proposal)
                .person(person)
                .date(rs.getTimestamp("date").toLocalDateTime())
                .status(ProposalState.getById(rs.getInt("cla_id")))
                .comment(rs.getString("comment"))
                .build();
    }
}
