package com.portal.service;

import java.util.Map;

import org.springframework.context.NoSuchMessageException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.portal.dto.UserProfileDTO;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;


public interface IJiraIntegrationService {
	
	/**
	 * Cria uma nova terefa no JIRA.
	 * <p>ATENÇÂO: Os dados enviados no param ("valuesToCreateTransiction") deverão ser previamente cadastrados nos parametros(ParameterModel) de integração 'JIRA_INTEGRATION_BASE_URI'
	 * 
	 * @param valuesToCreateTransiction Mapa de dados que serão utilizados para criar a tarefas.
	 * @param issueName Nome da tarefa a ser criada.
	 * @param userRequester dados do usuário logado.
	 * @author Osmar
	 */
	String createIssue(String issueName, Map<String, Object> valuesToCreateTransiction, UserProfileDTO userRequester)
			throws NoSuchMessageException, AppException, JsonProcessingException, BusException; 

}
