package com.devsuperior.dsmovie.controllers;

import com.devsuperior.dsmovie.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ScoreControllerRA {

	private Long existingMovieId, nonExistingMovieId, dependentMovieId;
	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;
	private Map<String, Object> postScoreInstance;


	@BeforeEach
	public void setUp() throws JSONException {
		baseURI = "http://localhost:8080";

		existingMovieId = 1L;
		nonExistingMovieId = 100L;
		dependentMovieId = 2L;

		clientUsername = "alex@gmail.com";
		clientPassword = "123456";
		adminUsername = "maria@gmail.com";
		adminPassword = "123456";

		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		invalidToken = "invalidToken";

		postScoreInstance = new HashMap<>();
		postScoreInstance.put("movieId", 100);
		postScoreInstance.put("score", 4.0f);
	}
	
	@Test
	public void saveScoreShouldReturnNotFoundWhenMovieIdDoesNotExist() throws Exception {
		JSONObject newScore = new JSONObject(postScoreInstance);

		given().header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + adminToken)
				.body(newScore.toString())
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when().put("/scores")
				.then().statusCode(404)
				.body("error", equalTo("Recurso não encontrado"))
				.body("status", equalTo(404));

	}
	
	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenMissingMovieId() throws Exception {
		postScoreInstance.put("movieId", null);

		JSONObject newScore = new JSONObject(postScoreInstance);

		given().header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + adminToken)
				.body(newScore.toString())
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when().put("/scores")
				.then().statusCode(422)
				.body("errors.message[0]", equalTo("Required field"))
				.body("status", equalTo(422));
	}
	
	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenScoreIsLessThanZero() throws Exception {
		JSONObject newScore = new JSONObject(postScoreInstance);
		newScore.put("score", -1);

		given().header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + adminToken)
				.body(newScore.toString())
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when().put("/scores")
				.then().statusCode(422)
				.body("errors.message[0]", equalTo("Score should be greater than or equal to zero"))
				.body("status", equalTo(422));
	}
}
