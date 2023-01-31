package com.portal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HttpClient {

	private static Logger logger = LoggerFactory.getLogger(HttpClient.class);

	@Autowired
	private RestTemplateBuilder builder;

	public HttpClient() {
		super();
	}

	public <T> T restGet(Class<T> clazz, String url) {
		RestTemplate restTemplate = this.builder.build();
		T response = restTemplate.getForObject(url, clazz);
		return response;
	}

	public <T> T restExchange(Object requestBody, Class<T> clazz, HttpMethod method, String url, HttpHeaders header) throws HttpClientErrorException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = requestBody != null && !requestBody.getClass().equals(String.class) ? mapper.writeValueAsString(requestBody) : requestBody != null ? requestBody.toString() : null;
			HttpEntity<String> request = new HttpEntity<String>(jsonString, header);
			RestTemplate restTemplate = this.builder.build();
			ResponseEntity<T> exchange = restTemplate.exchange(url, method, request, clazz);
			return exchange.getBody();
		} catch (HttpClientErrorException e) {
			throw e;
		} catch (Exception e) {
			logger.info("Erro ao processar requisição rest: " + e.getMessage());
		}

		return null;
	}     
}
