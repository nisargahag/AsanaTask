package com.airtable.driver;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class AsanaController {

	private final AirtableService airtableService;

	@Autowired
	public AsanaController(AirtableService airtableService) {
		this.airtableService = airtableService;
	}

	@PostMapping("/asana")
	public ResponseEntity<String> handleAsanaWebhook(@RequestBody AsanaDto asana) {
		try {
			// Extract task details from the payload
			String taskId = asana.getTaskId();
			String name = asana.getName();
			String assignee = asana.getAssignee();
			LocalDate dueDate = asana.getDueDate();
			String description = asana.getDescription();

			// Make API call to Airtable to create a new record
			boolean success = airtableService.createRecord(taskId, name, assignee, dueDate, description);

			if (success) {
				return ResponseEntity.ok("Task data copied to Airtable successfully.");
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Failed to copy task data to Airtable.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload received from Asana.");
		}

	}
}
