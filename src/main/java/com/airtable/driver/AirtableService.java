package com.airtable.driver;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AirtableService {

	private final RestTemplate restTemplate;
	private final String airtableApiKey = "YOUR_AIRTABLE_API_KEY";
	private final String airtableBaseUrl = "https://api.airtable.com/v0/YOUR_BASE_ID/Asana%20Tasks";

	@Autowired
	public AirtableService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public boolean createRecord(String taskId, String name, String assignee, LocalDate dueDate, String description) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(airtableApiKey);
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, Object> taskData = new HashMap<>();
			taskData.put("fields", Map.of("Task ID", taskId, "Name", name, "Assignee", assignee, "Due Date",
					dueDate.toString(), "Description", description));

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(taskData, headers);
			ResponseEntity<String> response = restTemplate.exchange(airtableBaseUrl, HttpMethod.POST, requestEntity,
					String.class);

			return response.getStatusCode() == HttpStatus.CREATED;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
