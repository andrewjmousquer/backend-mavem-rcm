package com.portal.service.imp;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.portal.dto.UserProfileDTO;
import com.portal.enums.AuditOperationType;
import com.portal.exceptions.AppException;
import com.portal.exceptions.BusException;
import com.portal.model.ParameterModel;
import com.portal.service.IAuditService;
import com.portal.service.IJiraIntegrationService;
import com.portal.service.IParameterService;
import com.portal.utils.HttpClient;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class JiraIntegrationService implements IJiraIntegrationService {
	
	private static final String jiraIntegrationParameterKey = "JIRA_TASK_GENERATION_INTEGRATION_";

	@Autowired
	private IAuditService auditService;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private IParameterService parameterService;
	
	@Autowired
	private HttpClient httpClient;
	
	/**
	 * Cria uma nova terefa no JIRA.
	 * ATENÇÂO: Os dados enviados no param ("valuesToCreateTransiction") deverão ser previamente cadastrados nos parametros(ParameterModel) de integração 'JIRA_INTEGRATION_BASE_URI'
	 * 
	 * @param valuesToCreateTransiction Mapa de dados que serão utilizados para criar a tarefas.
	 * @param issueName Nome da tarefa a ser criada.
	 * @param userRequester dados do usuário logado.
	 * @author Osmar
	 */
	@Override
	public String createIssue(String issueName , Map<String, Object> valuesToCreateTransiction , UserProfileDTO userRequester) throws NoSuchMessageException, AppException, JsonProcessingException, BusException {

		String baseUri = this.parameterService.getValueOf("JIRA_INTEGRATION_BASE_URI");
		
		// Cria URI
		URI uri = URI.create(new StringBuilder().append(baseUri).append("/issue").toString());
		
		String bodyJSON = createBodyRequestNewSalesOrder( issueName, valuesToCreateTransiction);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", this.makeAuthorizationHeader());
		headers.add("Content-Type", "application/json");
		
		

		try {

			String response = httpClient.restExchange(bodyJSON, String.class, HttpMethod.POST, uri.toString(), headers);
			
				final ObjectNode responseObject = new ObjectMapper().readValue(response, ObjectNode.class);
				final String jiraKey = responseObject.get("key").toString().replaceAll("\"", "");
				
				return jiraKey;

		} catch (Exception e) {
			e.getMessage();
			throw new AppException(e.getMessage());
		}

	}

	private String makeAuthorizationHeader() {

		String password = this.parameterService.getValueOf("JIRA_INTEGRATION_PASSWORD");

		String userName = this.parameterService.getValueOf("JIRA_INTEGRATION_USERNAME");

		// Concatena Username e Password
		String plainCredentials = userName + ":" + password;
		// Codifica para Base 64
		String base64Credentials = new String(Base64.getEncoder().encode(plainCredentials.getBytes()));
		// Cria o Header codificado
		String authorizationHeader = "Basic " + base64Credentials;

		return authorizationHeader;
	}

	private String createBodyRequestNewSalesOrder(String issueName, Map<String, Object> valuesToCreateTransiction) throws JsonProcessingException, AppException, BusException {

		String issuetype = this.parameterService.getValueOf("JIRA_INTEGRATION_ISSUE_TYPE");

		String project = this.parameterService.getValueOf("JIRA_INTEGRATION_PROJECT");
		
		List<ParameterModel> objectsToAdd = this.parameterService.search(new ParameterModel(jiraIntegrationParameterKey));

		// Body do JSON
		ObjectNode bodyContent = mapper.createObjectNode();

		// Update
		ObjectNode updateContent = mapper.createObjectNode();
		bodyContent.set("update", updateContent);

		// Fields
		ObjectNode fieldsContent = mapper.createObjectNode();
		fieldsContent.put("summary", issueName);

		// IssueType
		ObjectNode issueTypeContent = mapper.createObjectNode();
		issueTypeContent.put("id", issuetype);
		fieldsContent.set("issuetype", issueTypeContent);

		// Project
		ObjectNode projectContent = mapper.createObjectNode();
		projectContent.put("id", project);
		fieldsContent.set("project", projectContent);
		
		for (ParameterModel parameter : objectsToAdd) {
			
			String keyOfField = parameter.getValue().toString();
			Object fieldFinded = valuesToCreateTransiction.get(parameter.getName().replace(jiraIntegrationParameterKey , ""));
			
			if(fieldFinded == null)
				continue;
			
			String field = fieldFinded.toString();
			fieldsContent.put(keyOfField, field );
			
		}
		
		bodyContent.set("fields", fieldsContent);

		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyContent);

	}

	public void audit(String salesOrder, AuditOperationType operationType, UserProfileDTO userProfile)
			throws AppException, BusException {

		this.auditService.save(salesOrder, operationType, userProfile);

	}
	
}