package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Classifier;
import com.portal.model.ProposalFollowUp;

public class ProposalFollowUpMapper implements RowMapper<ProposalFollowUp> {

    @Override
    public ProposalFollowUp mapRow(ResultSet rs, int rowNum) throws SQLException {
		Classifier mediaType = Classifier.builder()
				.id(rs.getInt("med_cla_id"))
				.value(rs.getString("med_cla_value"))
				.type(rs.getString("med_cla_type"))
				.label(rs.getString("med_cla_label"))
				.build();
    	
        return ProposalFollowUp.builder()
                .id( rs.getInt("pfp_id"))
                .proposal( rs.getInt( "pps_id" ) )
                .date( rs.getTimestamp( "date" ).toLocalDateTime() )
                .media( mediaType )
                .person( rs.getString("person"))
                .comment( rs.getString("comment") )
                .build();
    }
}
