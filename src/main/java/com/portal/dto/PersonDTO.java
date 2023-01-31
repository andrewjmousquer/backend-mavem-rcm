package com.portal.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.portal.model.Classifier;
import com.portal.model.Person;

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
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PersonDTO {

    @EqualsAndHashCode.Include
    private Integer id;

    private String name;
    
    private String corporateName;

    private String jobTitle;

    private String cpf;

    private String cnpj;

    private String rne;

    private String rg;
    
    private String ie;

    private AddressDTO address;

    private Classifier classification;

    @JsonManagedReference
    private List<ContactDTO> contacts;

    @JsonManagedReference
    private List<PersonQualificationDTO> qualifications;

    @JsonManagedReference
    private List<BankAccountDTO> bankAccount;

    @JsonManagedReference
    private List<PersonRelatedDTO> personRelated;

    private LocalDate birthdate;
    
	private Classifier negativeList;

    public static PersonDTO toDTO(Person person) {
        if (person == null) {
            return null;
        }

        return PersonDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .corporateName(person.getCorporateName())
                .jobTitle(person.getJobTitle())
                .cpf(person.getCpf())
                .cnpj(person.getCnpj())
                .rne(person.getRne())
                .rg(person.getRg())
                .ie(person.getIe())
                .address(AddressDTO.toDTO(person.getAddress()))
                .classification(person.getClassification())
                .contacts(ContactDTO.toDTO(person.getContacts()))
                .qualifications(PersonQualificationDTO.toDTO(person.getQualifications()))
                .bankAccount(BankAccountDTO.toDTO(person.getBankAccount()))
                .personRelated(PersonRelatedDTO.toDTO(person.getPersonRelated()))
                .birthdate(person.getBirthdate())
                .negativeList(person.getNegativeList())
                .build();
    }

    public static List<PersonDTO> toDTO(List<Person> persons) {
        if (persons == null) {
            return null;
        }
        return persons.stream().map(PersonDTO::toDTO).collect(Collectors.toList());
    }
}