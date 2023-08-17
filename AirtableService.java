package com.airtable.driver;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AirtableService {

	@Autowired
	private RestTemplate restTemplate;
	private final String airtableApiKey = "patJqymxNF3zA4s82.c486c5a3821a4829fe134215fe78b79ede2bcfb5224cc4f090fb2bc376706ded";
	private final String airtableBaseUrl = "https://api.airtable.com/1.0/{baseID}/{tbldtBTl09MCHLsuk}";

	public boolean createRecord(String taskId, String name, String assignee, String type, LocalDate dueDate,
			String description) {
		try {

			URI uri = UriComponentsBuilder.fromUriString(airtableBaseUrl)
					.buildAndExpand("app5Rto1dn7sMvVEm", "tbldtBTl09MCHLsuk").toUri();

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(airtableApiKey);
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, Object> taskFields = new HashMap<>();
			taskFields.put("Task ID", taskId);
			taskFields.put("Name", name);
			taskFields.put("Assignee", assignee);
			taskFields.put("Description", description);
			taskFields.put("Due DATE", dueDate);

			if (dueDate != null) {
				taskFields.put("Due Date", dueDate.toString());
			}

			Map<String, Object> taskData = new HashMap<>();
			taskData.put("fields", taskFields);

			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(taskData, headers);
			ResponseEntity<String> response = restTemplate.exchange(uri.toString(), HttpMethod.POST, requestEntity,
					String.class);

			return response.getStatusCode() == HttpStatus.CREATED;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void processAsanaWebhookPayload(String payload) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			AsanaWebhookEvent event = objectMapper.readValue(payload, AsanaWebhookEvent.class);

	
				if ("task.created".equals(event.getType())) {
					String type = event.getType();
					String taskId = event.getTaskId();
					String taskName = event.getName();
					String assignee = event.getAssignee();
					LocalDate dueDate = event.getDueDate();
					String description = event.getDescription();

					boolean success = createRecord(type, taskId, taskName, assignee, dueDate, description);

					if (success) {
						System.out.println("Record created in Airtable: Task ID " + taskId);
					} else {
						System.out.println("Failed to create record in Airtable: Task ID " + taskId);
					}
				}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ResponseEntity<String> handleAsanaWebhook(String requestBody, String hookSecret, String hookSignature) {
		if (hookSecret != null) {
			System.out.println("This is a new webhook");
			AsanaController.secret = hookSecret;
			return ResponseEntity.status(HttpStatus.OK).header("X-Hook-Secret", AsanaController.secret).build();
		} else if (hookSignature != null) {
			try {
				String computedSignature = computeSignature(requestBody);

				if (!constantTimeComparison(hookSignature, computedSignature)) {

					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
				} else {

					System.out.println("Events on " + new java.util.Date() + ":");
					System.out.println(requestBody);

					return ResponseEntity.status(HttpStatus.OK).build();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			System.err.println("Something went wrong!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	private String computeSignature(String requestBody) throws NoSuchAlgorithmException, InvalidKeyException {
		Mac sha256Hmac = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKey = new SecretKeySpec(AsanaController.secret.getBytes(StandardCharsets.UTF_8),
				"HmacSHA256");
		sha256Hmac.init(secretKey);
		byte[] hashBytes = sha256Hmac.doFinal(requestBody.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(hashBytes);
	}

	private boolean constantTimeComparison(String a, String b) {
		byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
		byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

		if (aBytes.length != bBytes.length) {
			return false;
		}

		int result = 0;
		for (int i = 0; i < aBytes.length; i++) {
			result |= aBytes[i] ^ bBytes[i];
		}
		return result == 0;
	}

}
