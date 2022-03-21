package org.cryptomator.hub.api;

import com.radcortez.flyway.test.annotation.DataSource;
import com.radcortez.flyway.test.annotation.FlywayTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Timestamp;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

@QuarkusTest
@FlywayTest(value = @DataSource(url = "jdbc:h2:mem:test"))
@DisplayName("Resource /vaults")
public class VaultResourceTest {

	@BeforeAll
	public static void beforeAll() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

	}

	@Nested
	@DisplayName("As user1")
	@TestSecurity(user = "User Name 1", roles = {"user"})
	@OidcSecurity(claims = {
			@Claim(key = "sub", value = "user1")
	})
	public class AsAuthorizedUser1 {

		@Test
		@DisplayName("GET /vaults returns 200")
		public void testGetSharedOrOwned() {
			when().get("/vaults")
					.then().statusCode(200)
					.body("size()", is(2))
					.body("id", hasItems("vault1", "vault2"));
		}

		@Test
		@DisplayName("GET /vaults/vault1 returns 200")
		public void testGetVault1() {
			when().get("/vaults/{vaultId}", "vault1")
					.then().statusCode(200)
					.body("id", is("vault1"));
		}

		@Test
		@DisplayName("GET /vaults/nonExistingVault returns 404")
		public void testGetVault2() {
			when().get("/vaults/{vaultId}", "nonExistingVault")
					.then().statusCode(404);
		}

		@Test
		@DisplayName("GET /vaults/vault1/members returns 403")
		public void testGetMembers() {
			when().get("/vaults/{vaultId}/members", "vault1")
					.then().statusCode(403);
		}

		@Test
		@DisplayName("GET /vaults/vault1/keys/device1 returns 200")
		public void testUnlock1() {
			when().get("/vaults/{vaultId}/keys/{deviceId}", "vault1", "device1")
					.then().statusCode(200)
					.body(is("jwe1"));
		}

		@Test
		@DisplayName("GET /vaults/vault1/keys/noSuchDevice returns 404")
		public void testUnlock2() {
			when().get("/vaults/{vaultId}/keys/{deviceId}", "vault1", "noSuchDevice")
					.then().statusCode(404);
		}

		@Test
		@DisplayName("GET /vaults/vault1/keys/device2 returns 403")
		public void testUnlock3() {
			when().get("/vaults/{vaultId}/keys/{deviceId}", "vault1", "device2")
					.then().statusCode(403);
		}

	}

	@Nested
	@DisplayName("As vault owner user1")
	@TestSecurity(user = "User Name 1", roles = {"user", "vault-owner"})
	@OidcSecurity(claims = {
			@Claim(key = "sub", value = "user1")
	})
	public class AsVaultOwner {

		@Test
		@DisplayName("GET /vaults/vault1/members returns 200")
		public void testGetMembers() {
			when().get("/vaults/{vaultId}/members", "vault1")
					.then().statusCode(200)
					.body("id", hasItems("user1", "user2"));
		}

		@Test
		@DisplayName("PUT /vaults/vault1 returns 409")
		public void testCreateVault1() {
			var vaultDto = new VaultResource.VaultDto("vault1", "My Vault","Test vault 1", Timestamp.valueOf("1999-11-19 19:19:19"), null, "masterkey3", "42", "NaCl");

			given().contentType(ContentType.JSON).body(vaultDto)
					.when().put("/vaults/{vaultId}", "vault1")
					.then().statusCode(409);
		}

		@Test
		@DisplayName("PUT /vaults/vaultX returns 409")
		public void testCreateVault2() {
			var vaultDto = new VaultResource.VaultDto("vaultX", "Vault 1","This is a testvault.", Timestamp.valueOf("2020-02-20 20:20:20"), null, "masterkey1", "iterations1", "salt1");

			given().contentType(ContentType.JSON).body(vaultDto)
					.when().put("/vaults/{vaultId}", "vaultX")
					.then().statusCode(409);
		}

		@Test
		@DisplayName("PUT /vaults/vault3 returns 201")
		public void testCreateVault3() {
			var vaultDto = new VaultResource.VaultDto("vault3", "My Vault","Test vault 3", Timestamp.valueOf("2112-12-21 21:12:21"), null, "masterkey3", "42", "NaCl");

			given().contentType(ContentType.JSON).body(vaultDto)
					.when().put("/vaults/{vaultId}", "vault3")
					.then().statusCode(201);
		}

		@Test
		@DisplayName("PUT /vaults/vault3 returns 400")
		public void testCreateVault4() {
			given().contentType(ContentType.JSON)
					.when().put("/vaults/{vaultId}", "vault3")
					.then().statusCode(400);
		}

		@Test
		@DisplayName("PUT /vaults/vault1/keys/device3 returns 201")
		public void testGrantAccess1() {
			given().contentType(ContentType.TEXT).body("jwe4")
					.when().put("/vaults/{vaultId}/keys/{deviceId}", "vault1", "device3")
					.then().statusCode(201);
		}

		@Test
		@DisplayName("PUT /vaults/vault2/keys/device1 returns 409")
		public void testGrantAccess2() {
			given().contentType(ContentType.TEXT).body("jwe4")
					.when().put("/vaults/{vaultId}/keys/{deviceId}", "vault2", "device1")
					.then().statusCode(409);
		}

		@Test
		@DisplayName("PUT /vaults/vault1/keys/nonExistingDevice returns 404")
		public void testGrantAccess3() {
			given().contentType(ContentType.TEXT).body("jwe3")
					.when().put("/vaults/{vaultId}/keys/{deviceId}", "vault1", "nonExistingDevice")
					.then().statusCode(404);
		}

	}

	@Nested
	@DisplayName("Managing members as user2")
	@FlywayTest(value = @DataSource(url = "jdbc:h2:mem:test"), clean = false)
	@TestSecurity(user = "User Name 2", roles = {"user", "vault-owner"})
	@OidcSecurity(claims = {
			@Claim(key = "sub", value = "user2")
	})
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	public class ManageMembers {

		@Test
		@Order(1)
		@DisplayName("PUT /vaults/vault2/members/user3 returns 404")
		public void addNonExistingUser() {
			when().put("/vaults/{vaultId}/members/{userId}", "vault2", "user3")
					.then().statusCode(404);
		}

		@Test
		@Order(2)
		@DisplayName("GET /vaults/vault2/members does not contain user2")
		public void getMembers1() {
			when().get("/vaults/{vaultId}/members", "vault2")
					.then().statusCode(200)
					.body("id", not(hasItems("user2")));
		}

		@Test
		@Order(3)
		@DisplayName("PUT /vaults/vault2/members/user2 returns 201")
		public void addUser1() {
			when().put("/vaults/{vaultId}/members/{userId}", "vault2", "user2")
					.then().statusCode(201);
		}

		@Test
		@Order(4)
		@DisplayName("GET /vaults/vault2/members does contain user2")
		public void getMembers2() {
			when().get("/vaults/{vaultId}/members", "vault2")
					.then().statusCode(200)
					.body("id", hasItems("user2"));
		}

		@Test
		@Order(5)
		@DisplayName("GET /vaults/vault2/devices-requiring-access-grant contains device2")
		public void testGetDevicesRequiringAccess1() {
			when().get("/vaults/{vaultId}/devices-requiring-access-grant", "vault2")
					.then().statusCode(200)
					.body("id", hasItems("device2"));
		}

		@Test
		@Order(6)
		@DisplayName("PUT /vaults/vault2/keys/device2 returns 201")
		public void testGrantAccess() {
			given().contentType(ContentType.TEXT).body("jwe4")
					.when().put("/vaults/{vaultId}/keys/{deviceId}", "vault2", "device2")
					.then().statusCode(201);
		}

		@Test
		@Order(7)
		@DisplayName("GET /vaults/vault2/devices-requiring-access-grant contains device2")
		public void testGetDevicesRequiringAccess2() {
			when().get("/vaults/{vaultId}/devices-requiring-access-grant", "vault2")
					.then().statusCode(200)
					.body("id", not(hasItems("device2")));
		}

		@Test
		@Order(8)
		@DisplayName("DELETE /vaults/vault2/members/user2 returns 204")
		public void removeUser2() {
			when().delete("/vaults/{vaultId}/members/{userId}", "vault2", "user2")
					.then().statusCode(204);
		}

		@Test
		@Order(9)
		@DisplayName("GET /vaults/vault2/members does not contain user2")
		public void getMembers3() {
			when().get("/vaults/{vaultId}/members", "vault2")
					.then().statusCode(200)
					.body("id", not(hasItems("user2")));
		}

	}

	@Nested
	@DisplayName("As unauthenticated user")
	public class AsAnonymous {

		@DisplayName("401 Unauthorized")
		@ParameterizedTest(name = "{0} {1}")
		@CsvSource(value = {
				"GET, /vaults",
				"GET, /vaults/vault1",
				"GET, /vaults/vault1/members",
				"PUT, /vaults/vault1/members/user1",
				"DELETE, /vaults/vault1/members/user1",
				"GET, /vaults/vault1/devices-requiring-access-grant",
				"GET, /vaults/vault1/keys/device1"
		})
		public void testGet(String method, String path) {
			when().request(method, path)
					.then().statusCode(401);
		}

	}

}