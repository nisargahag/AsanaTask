package com.airtable.driver;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/webhook")
public class AsanaController {

	@Autowired
	private AirtableService airtableService;

	public static String secret;

	@PostMapping("/asana")
	public ResponseEntity<String> handleAsanaWebhook(HttpServletRequest request, @RequestBody String payload,
			HttpServletResponse response) {

		if (request.getHeader("x-hook-secret") != null) {
			secret = request.getHeader("x-hook-secret");
			response.setHeader("x-hook-secret", secret);
			response.setStatus(200);
			System.out.println(secret);
			System.out.println(payload);
			return ResponseEntity.status(HttpStatus.OK).body(secret);
		} else if (request.getHeader("x-hook-signature") != null) {
			System.out.println(secret);
			System.out.println(payload);
			return ResponseEntity.status(HttpStatus.OK).body("webhook processed");
		} else {
			response.setStatus(404);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid request.");
		}
	}
}
