package org.cryptomator.hub.spi;

import com.radcortez.flyway.test.annotation.DataSource;
import com.radcortez.flyway.test.annotation.FlywayTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;

@QuarkusTest
@FlywayTest(value = @DataSource(url = "jdbc:h2:mem:test"))
@DisplayName("Resource /devices")
public class UsersResourceTest {

	@BeforeAll
	public static void beforeAll() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@Nested
	@DisplayName("As user1")
	@FlywayTest(value = @DataSource(url = "jdbc:h2:mem:test"))
	@TestSecurity(user = "User Name 1", roles = {"user"})
	@OidcSecurity(claims = {
			@Claim(key = "sub", value = "user1")
	})
	public class AsAuthorzedUser1 {

		@Test
		@DisplayName("PUT /users/me returns 201")
		public void testSyncMe() {
			when().put("/users/me")
					.then().statusCode(201);
		}

		@Test
		@DisplayName("GET /users/me returns 200")
		public void testGetMe1() {
			when().get("/users/me")
					.then().statusCode(200)
					.body("id", is("user1"))
					.body("devices.flatten()", empty());
		}

		@Test
		@DisplayName("GET /users/me?withDevices=true returns 200")
		public void testGetMe2() {
			when().get("/users/me?withDevices=true")
					.then().statusCode(200)
					.body("id", is("user1"))
					.body("devices.id", hasItems("device1"))
					.body("devices.accessTo.flatten()", empty());
		}

		@Test
		@DisplayName("GET /users/me?withDevices=true&withAccessibleVaults=true returns 200")
		public void testGetMe3() {
			when().get("/users/me?withDevices=true&withAccessibleVaults=true")
					.then().statusCode(200)
					.body("id", is("user1"))
					.body("devices.id", hasItems("device1"))
					.body("devices.accessTo.id.flatten()", hasItems("vault1"));
		}

		@Test
		@DisplayName("GET /users returns 403")
		public void testGetAll() {
			when().get("/users")
					.then().statusCode(403);
		}

	}

	@Nested
	@DisplayName("As vault owner user1")
	@FlywayTest(value = @DataSource(url = "jdbc:h2:mem:test"))
	@TestSecurity(user = "User Name 1", roles = {"user", "vault-owner"})
	@OidcSecurity(claims = {
			@Claim(key = "sub", value = "user1")
	})
	public class AsVaultOwner {

		@Test
		@DisplayName("GET /users returns 200")
		public void testGetAll() {
			when().get("/users")
					.then().statusCode(200)
					.body("id", hasItems("user1", "user2"));
		}

	}

	@Nested
	@DisplayName("As unauthenticated user")
	public class AsAnonymous {

		@DisplayName("401 Unauthorized")
		@ParameterizedTest(name = "{0} {1}")
		@CsvSource(value = {
				"GET, /users/me",
				"PUT, /users/me",
				"GET, /users"
		})
		public void testGet(String method, String path) {
			when().request(method, path)
					.then().statusCode(401);
		}

	}

}