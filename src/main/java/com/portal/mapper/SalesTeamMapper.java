package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.SalesTeam;

public class SalesTeamMapper implements RowMapper<SalesTeam> {
    @Override
    public SalesTeam mapRow(ResultSet rs, int rowNum) throws SQLException {
        return SalesTeam.builder()
                .id(rs.getInt("slt_id"))
                .name(rs.getString("name"))
                .build();
    }
}
