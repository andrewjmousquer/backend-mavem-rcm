package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Job;
import com.portal.model.ProposalApprovalRule;

public class ProposalApprovalRuleMapper implements RowMapper<ProposalApprovalRule> {

    @Override
    public ProposalApprovalRule mapRow(ResultSet rs, int rowNum) throws SQLException {
        Job job = Job.builder()
                .id(rs.getInt("job_id"))
                .name(rs.getString("job_name"))
                .level(rs.getInt("job_level"))
                .build();

        return ProposalApprovalRule.builder()
                .id(rs.getInt("par_id"))
                .value(rs.getDouble("value"))
                .job(job)
                .build();
    }
}
