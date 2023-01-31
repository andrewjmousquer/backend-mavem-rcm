package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Job;

public class JobMapper implements RowMapper<Job> {
    @Override
    public Job mapRow(ResultSet rs, int rowNum) throws SQLException {

        return Job.builder()
                .id(rs.getInt("job_id"))
                .name(rs.getString("name"))
                .level(rs.getInt("level"))
                .build();
    }
}
