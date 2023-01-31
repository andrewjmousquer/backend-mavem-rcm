package com.portal.model;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portal.dto.BankAccountDTO;
import com.portal.enums.AccountType;
import com.portal.validators.NotNullNotZero;
import com.portal.validators.ValidationHelper.OnSave;
import com.portal.validators.ValidationHelper.OnUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankAccount {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;

	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String agency;
	
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String accountNumber;
	
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String pixKey;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private AccountType type;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	private Bank bank;
	
	@NotNull(groups = {OnUpdate.class, OnSave.class})
	@JsonBackReference
	private Person person;
	
	public static BankAccount toEntity(BankAccountDTO bankAccountDTO) {
		if (bankAccountDTO == null) {
			return null;
		}

		return BankAccount.builder()
                .id(bankAccountDTO.getId())
                .agency(bankAccountDTO.getAgency())
                .accountNumber(bankAccountDTO.getAccountNumber())
                .pixKey(bankAccountDTO.getPixKey())
                .type(bankAccountDTO.getType())
                .bank(Bank.toEntity(bankAccountDTO.getBank()))
                //.person(Person.toEntity(bankAccountDTO.getPerson()))
                .build();
	}

	public static List<BankAccount> toEntity(List<BankAccountDTO> list) {
		if (list == null) {
			return null;
		}
		return list.stream().map(BankAccount::toEntity).collect(Collectors.toList());
	}
	
}