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
import static org.hamcrest.Matchers.*;

public class MovieControllerRA {

	private String movieName, image;
	private Long existingMovieId, nonExistingMovieId, dependentMovieId;
	private Map<String, Object> postMovieInstance;
	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;

	@BeforeEach
	public void setUp() throws JSONException {
		baseURI = "http://localhost:8080";
		movieName = "Titanic";
		image = "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/yDI6D5ZQh67YU4r2ms8qcSbAviZ.jpg";

		existingMovieId = 1L;
		nonExistingMovieId = 100L;
		dependentMovieId = 2L;

		postMovieInstance = new HashMap<>();

		postMovieInstance.put("title", "Test Movie");
		postMovieInstance.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");
		postMovieInstance.put("score", 0.0f);
		postMovieInstance.put("count", 0);

		clientUsername = "alex@gmail.com";
		clientPassword = "123456";
		adminUsername = "maria@gmail.com";
		adminPassword = "123456";

		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		invalidToken = "invalidToken";
	}
	
	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {

		given().get("/movies")
				.then().statusCode(200)
				.body("content.title", hasItems("Matrix Resurrections", "The Witcher"));
	}
	
	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {

		given().get("/movies?title={movieName}", movieName)
				.then().statusCode(200)
				.body("content.id[0]", is(7))
				.body("content.image[0]", equalTo(image))
				.body("content.title[0]", equalTo("Titanic"));
	}
	
	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		image = "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg";

		given().get("/movies/{id}", existingMovieId)
				.then().statusCode(200)
				.body("id", is(1))
				.body("count", is(2))
				.body("image", equalTo(image))
				.body("score", is(4.5f))
				.body("title", equalTo("The Witcher"));
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		given().get("/movies/{id}", nonExistingMovieId)
				.then().statusCode(404)
				.body("error", equalTo("Recurso não encontrado"))
				.body("status", equalTo(404));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {
		postMovieInstance.put("title", "");

		JSONObject newMovie = new JSONObject(postMovieInstance);


		given().header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + adminToken)
				.body(newMovie.toString())
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when().post("/movies")
				.then().statusCode(422)
				.body("errors.message", hasItems("Tamanho deve ser entre 5 e 80 caracteres", "Campo requerido"));
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieInstance);


		given().header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + clientToken)
				.body(newMovie.toString())
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when().post("/movies")
				.then().statusCode(403);
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieInstance);


		given().header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + invalidToken)
				.body(newMovie.toString())
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when().post("/movies")
				.then().statusCode(401);
	}
}
