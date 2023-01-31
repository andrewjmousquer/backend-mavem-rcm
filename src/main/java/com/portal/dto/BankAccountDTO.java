package com.portal.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.enums.AccountType;
import com.portal.model.BankAccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankAccountDTO {

	private Integer id;

	private String agency;
	
	private String accountNumber;
	
	private String pixKey;
	
	private AccountType type;
	
	private String label;
	
	private BankDTO bank;
	
	@JsonBackReference
	private PersonDTO person;
	
	public static BankAccountDTO toDTO(BankAccount bankAccount) {
	        if (bankAccount == null) {
	            return null;
	        }

	        return BankAccountDTO.builder()
	                .id(bankAccount.getId())
	                .agency(bankAccount.getAgency())
	                .accountNumber(bankAccount.getAccountNumber())
	                .pixKey(bankAccount.getPixKey())
	                .type(bankAccount.getType())
	                .bank(BankDTO.toDTO(bankAccount.getBank()))
	               // .person(PersonDTO.toDTO(bankAccount.getPerson()))
	                .build();
	}

	public static List<BankAccountDTO> toDTO(List<BankAccount> contacts) {
	        if (contacts == null) {
	            return null;
	        }

	        return contacts.stream().map(BankAccountDTO::toDTO).collect(Collectors.toList());
	}
}
