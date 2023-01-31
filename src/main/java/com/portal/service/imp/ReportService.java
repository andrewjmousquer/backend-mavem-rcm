package com.portal.service.imp;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.model.ReportModel;
import com.portal.service.IReportService;

@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ReportService implements IReportService {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Value("${jasperserver.host}")
	public String jasperServerHost;
	
	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

	@Override
	public Boolean check() {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));

			HttpEntity<String> entity = new HttpEntity<String>(headers);

			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			ResponseEntity<String> response = restTemplate.exchange(
					jasperServerHost, HttpMethod.GET,
					entity, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				return true;
			}

			return false;
		} catch (HttpClientErrorException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}
	
	@Override
	public List<ReportModel> listAll(String token) {
		List<ReportModel> list = new ArrayList<ReportModel>();
		try {
			token = token.replace("Bearer", "").trim();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));

			HttpEntity<String> entity = new HttpEntity<String>(headers);

			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			ResponseEntity<String> response = restTemplate.exchange(
					jasperServerHost + "/rest_v2/resources?type=reportUnit&sortBy=uri&pp=" + token, HttpMethod.GET,
					entity, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				String stringResponse = response.getBody();
				Map<String, ReportModel[]> reports = objectMapper.readValue(stringResponse,
						new TypeReference<Map<String, ReportModel[]>>() {
						});
				ReportModel[] reportsModel = reports.get("resourceLookup");

				list = Arrays.asList(reportsModel);
			}

			return list;
		} catch (HttpClientErrorException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return list;
	}

	@Override
	public List<ReportModel> listAllFolders(String token) {
		List<ReportModel> list = new ArrayList<ReportModel>();
		try {
			token = token.replace("Bearer", "").trim();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));

			HttpEntity<String> entity = new HttpEntity<String>(headers);

			ResponseEntity<String> response = restTemplate.exchange(
					jasperServerHost + "/rest_v2/resources?type=folder&sortBy=uri&pp=" + token, HttpMethod.GET, entity,
					String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				String stringResponse = response.getBody();
				Map<String, ReportModel[]> reports = objectMapper.readValue(stringResponse,
						new TypeReference<Map<String, ReportModel[]>>() {
						});
				ReportModel[] reportsModel = reports.get("resourceLookup");

				list = Arrays.asList(reportsModel);
			}

			return list;
		} catch (HttpClientErrorException e) {
			logger.error(e.getMessage());

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return list;
	}
	
	public byte[] getReport(String jasperUrl, String reportPath, String jasperUser, String jasperPassword, String params) {
		StringBuilder url = new StringBuilder();
		url.append(jasperUrl);
		url.append("/rest_v2/reports" + reportPath);
		url.append("?j_username=" + jasperUser);
		url.append("&j_password=" + jasperPassword);
		url.append("&" + params);
		
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_PDF, MediaType.APPLICATION_OCTET_STREAM));
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<byte[]> result = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, byte[].class);
	
		return result.getBody();
	}
}
