package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.model.BankAccount;
import com.portal.model.Classifier;
import com.portal.model.Person;
import com.portal.model.ProposalCommission;
import com.portal.model.ProposalDetail;

public class ProposalCommisionMapper implements RowMapper<ProposalCommission> {

	@Override
	public ProposalCommission mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Classifier commissionType = Classifier.builder()
				.id(rs.getInt("cla_id"))
				.value(rs.getString("cla_value"))
				.type(rs.getString("cla_type"))
				.label(rs.getString("cla_label"))
				.build();
			
		return ProposalCommission.builder()
						.id( rs.getInt( "pcm_id" ) )
				        .person(Person.builder()
				        		.id( rs.getInt( "per_id" ))
				        		.build())
				        .dueDate(rs.getDate("due_date"))
				        .value(rs.getDouble("value"))
				        .notes(rs.getString("notes"))
				        .commissionType(commissionType)
				        .bankAccount(BankAccount.builder()
				        		.id(rs.getInt("act_id"))
				        		.build())
				        .proposalDetail(ProposalDetail.builder()
				        		.id(rs.getInt("ppd_id"))
				        		.build())
						.build();
	}
}
