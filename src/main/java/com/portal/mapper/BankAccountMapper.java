package com.portal.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.portal.enums.AccountType;
import com.portal.model.Bank;
import com.portal.model.BankAccount;
import com.portal.model.Person;
import com.portal.utils.PortalNumberUtils;

public class BankAccountMapper implements RowMapper<BankAccount> {

	@Override
	public BankAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Bank bank = Bank.builder()
							.id( rs.getInt( "bnk_id" ) )
							.name( rs.getString( "name" ) )
							.code( rs.getString( "code" ) )
							.active( PortalNumberUtils.intToBoolean( rs.getInt( "active" ) ) )
							.build();
		
		return BankAccount.builder()
								.id( rs.getInt( "act_id" ) )
								.agency( rs.getString( "agency" ) )
								.accountNumber( rs.getString( "account_number" ) )
								.pixKey( rs.getString( "pix_key" ) )
								.type( AccountType.getById( rs.getInt( "type_cla_id" ) ) )
								.bank( bank )
								.person( Person.builder().id( rs.getInt( "per_id" ) ).build() )
								.build();
	}
}
