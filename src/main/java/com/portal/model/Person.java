package com.portal.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import com.portal.dto.PersonDTO;
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
public class Person {

	@EqualsAndHashCode.Include
	@Null(groups = {OnSave.class})
	@NotNullNotZero(groups = {OnUpdate.class})
	private Integer id;

	@Size(max = 255, groups = {OnUpdate.class, OnSave.class})
	@NotBlank(groups = {OnUpdate.class, OnSave.class})
	private String name;
	
	@Size(max = 255, groups = {OnUpdate.class, OnSave.class})
	private String corporateName;
	
	@Size(max = 255, groups = {OnUpdate.class, OnSave.class})
	private String jobTitle;
	
	@Size(max = 14, groups = {OnUpdate.class, OnSave.class})
	private String cpf;
	
	@Size(max = 14, groups = {OnUpdate.class, OnSave.class})
	private String cnpj;
	
	@Size(max = 45, groups = {OnUpdate.class, OnSave.class})
	private String rne;
	
	@Size(max = 20, groups = {OnUpdate.class, OnSave.class})
	private String rg;
	
	@Size(max = 20, groups = {OnUpdate.class, OnSave.class})
	private String ie;

	private AddressModel address;
	
	private Classifier classification;
	
	private List<Contact> contacts;
	
	private List<PersonQualification> qualifications;
	
	private List<BankAccount> bankAccount;
	
	private List<PersonRelated> personRelated;
	
	private LocalDate birthdate;
	
	private Classifier negativeList;

	private boolean isUser;
	
	public Person( Integer id) {
		this.id = id;
	}
		
	public Person( Integer id, String name,Classifier classification) {
		this.id = id;
		this.name = name;
		this.classification = classification;
	}
	
	public Person( Integer id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Person( String name,Classifier classification) {
		this.name = name;
		this.classification = classification;
	}

	public Person(Integer id, String name, String jobTitle, String cpf, String cnpj, String rne, String rg, String ie, Classifier classification) {
		this.id = id;
		this.name = name;
		this.jobTitle = jobTitle;
		this.cpf = cpf;
		this.cnpj = cnpj;
		this.rne = rne;
		this.rg = rg;
		this.ie = ie;
		this.classification = classification;
	}

	public static Person toEntity( PersonDTO personDTO ) {
		if (personDTO == null) {
			return null;
		}
		
		return Person.builder()
						.id(personDTO.getId())
						.name(personDTO.getName())
						.corporateName(personDTO.getCorporateName())
						.jobTitle(personDTO.getJobTitle())
						.cpf(personDTO.getCpf())
						.cnpj(personDTO.getCnpj())
						.rne(personDTO.getRne())
						.rg(personDTO.getRg())
						.ie(personDTO.getIe())
						.address( AddressModel.toEntity(personDTO.getAddress()) )
						.classification( personDTO.getClassification() )
						.contacts( Contact.toEntity( personDTO.getContacts() ) )
						.qualifications( PersonQualification.toEntity(personDTO.getQualifications()) )
						.bankAccount( BankAccount.toEntity(personDTO.getBankAccount()) )
						.personRelated( PersonRelated.toEntity(personDTO.getPersonRelated()) )
						.birthdate( personDTO.getBirthdate() )
						.negativeList( personDTO.getNegativeList())
						.build();
	}

	public static List<Person> toEntity(List<PersonDTO> list) {
		if (list == null) {
			return null;
		}
		return list.stream().map(Person::toEntity).collect(Collectors.toList());
	}

}