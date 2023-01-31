package com.portal.service;

import javax.validation.Validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.dao.impl.PartnerPersonDAO;
import com.portal.service.imp.ClassifierService;
import com.portal.service.imp.PartnerPersonService;
import com.portal.service.imp.PartnerService;
import com.portal.service.imp.PersonService;

@ExtendWith(SpringExtension.class)	
public class PartnerPersonServiceTest {

	@Mock
	PartnerPersonDAO dao;
	
	@Mock
	PersonService personService;
	
	@Mock
	PartnerService partnerService;
	
	@Mock
	ClassifierService classifierService;
	
	@Mock
	ObjectMapper objectMapper;
	
	@Mock
	Validator validator;
	
	@Mock
	MessageSource messageSource;
	
	@InjectMocks
	PartnerPersonService service;
	
	@Nested
	class Find {
		@Test
		@DisplayName( "Dado o ID válido de um parceiro quando procurado retorna a lista de pessoas" )
		void givenValidPartnerId_whenFind_thenReturnPersonList() throws Exception { 
			
		}
		
		void givenValidPersonId_whenFind_thenReturnPartner() throws Exception { 
		
		}
		
		@Test
		@DisplayName( "Dado NULO quando procurado retorna erro" )
		void givenNull_whenFind_thenReturnError() throws Exception { 
		
		}
	}
	
	@Nested
	class Get {
		@Test
		@DisplayName( "Dado o ID válido de um parceiro e uma pessoa quando procurado retorna a pessoas relacionada" )
		void givenValidPartnerPersonId_whenGetById_thenReturnPerson() throws Exception {
			
		}
		
		@Test
		@DisplayName( "Dado o ID zero do parceiro quando busca por uma pessoa deve dar erro" )
		void givenZeroPartnerId_whenGetPerson_thenReturnError() throws Exception {
		
		}
		
		@Test
		@DisplayName( "Dado o ID nulo do parceiro quando busca por uma pessoa deve dar erro" )
		void givenNullPartnerId_whenGetPerson_thenReturnTypeList() throws Exception {
				}
		
		@Test
		@DisplayName( "Dado o ID zero da pessoa quando busca por uma pessoa deve dar erro" )
		void givenZeroPersonId_whenGetPerson_thenReturnTypeList() throws Exception {

		}
		
		@Test
		@DisplayName( "Dado o ID nulo da pessoa quando busca por uma pessoa deve dar erro" )
		void givenNullPersonId_whenGetPerson_thenReturnTypeList() throws Exception {
		
		}
	}
	
	@Nested
	class Save {
		@Test
		@DisplayName("Dado um relacionamento quando salvo não deve dar erro")
		void givenPartnerPersonRelatioship_whenSave_thenReturnId() throws Exception {
		}
		
		@Test
		@DisplayName("Dado uma pessoa inexistente quando salvo deve ocorre erro")
		void givenNoExistPerson_whenSave_thenReturnError() throws Exception {
			
		}
		
		@Test
		@DisplayName("Dado um parceiro inexistente quando salvo deve ocorre erro")
		void givenNoExistType_whenSave_thenReturnError() throws Exception {
			
		}
	}
	
	@Nested
	class Delete {
		
	}
}
