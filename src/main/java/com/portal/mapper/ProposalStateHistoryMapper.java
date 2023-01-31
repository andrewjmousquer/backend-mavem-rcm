package com.portal.mapper;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.Classifier;
import com.portal.model.Proposal;
import com.portal.model.ProposalStateHistory;
import com.portal.model.SalesOrder;
import com.portal.model.UserModel;

public class ProposalStateHistoryMapper implements RowMapper<ProposalStateHistory> {

	@Override
	public ProposalStateHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Classifier oldStatus = Classifier.builder()
							.id(rs.getInt("cla_id_old"))
							.value(rs.getString("value_old"))
							.type(rs.getString("type_old"))
							.label(rs.getString("label_old"))
							.build();
		
		Classifier newStatus = Classifier.builder()
							.id(rs.getInt("cla_id_new"))
							.value(rs.getString("value_new"))
							.type(rs.getString("type_new"))
							.label(rs.getString("label_new"))
							.build();
		
		return ProposalStateHistory.builder()
								.id( rs.getInt( "psh_id" ) )
								.proposal( Proposal.builder().id(rs.getInt("pps_id")).build() )
								.statusNew( newStatus )
								.statusOld( oldStatus )
								.salesOrder( SalesOrder.builder().id(rs.getInt("sor_id")).build() )
								.user( UserModel.builder().id(rs.getInt("usr_id")).build() )
								.statusDate( rs.getTimestamp("status_date").toLocalDateTime() )
								.build();
	}
}
