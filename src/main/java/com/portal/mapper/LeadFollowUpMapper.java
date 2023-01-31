package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.MediaContact;
import com.portal.model.Lead;
import com.portal.model.LeadFollowUp;

public class LeadFollowUpMapper implements RowMapper<LeadFollowUp> {

	@Override
	public LeadFollowUp mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		return LeadFollowUp.builder()
				.id( rs.getInt("lfp_id") )
				.lead( Lead.builder().id(rs.getInt("led_id")).build() )
				.date(  rs.getTimestamp( "date" ).toLocalDateTime() )
				.person( rs.getString("person") )
				.media( MediaContact.getById(rs.getInt("media_cla_id")).getType() )
				.comment( rs.getString("comment") )
				.build();
	}

}
