package org.cryptomator.hub.filters;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.cryptomator.hub.entities.VaultAccess;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Map;

@QuarkusTest
public class VaultRoleFilterTest {

	ResourceInfo resourceInfo = Mockito.mock(ResourceInfo.class);
	UriInfo uriInfo = Mockito.mock(UriInfo.class);
	ContainerRequestContext context = Mockito.mock(ContainerRequestContext.class);
	JsonWebToken jwt = Mockito.mock(JsonWebToken.class);
	VaultRoleFilter filter = new VaultRoleFilter();

	@BeforeEach
	public void setup() {
		filter.resourceInfo = resourceInfo;
		filter.jwt = jwt;

		Mockito.doReturn(uriInfo).when(context).getUriInfo();
	}

	@Test
	@DisplayName("error 403 if annotated resource has no vaultId path param")
	public void testFilterWithMissingVaultId() throws NoSuchMethodException {
		Mockito.doReturn(getClass().getMethod("allowMember")).when(resourceInfo).getResourceMethod();
		Mockito.doReturn(new MultivaluedHashMap<>()).when(uriInfo).getPathParameters();

		Assertions.assertThrows(ForbiddenException.class, () -> filter.filter(context));
	}

	@Test
	@DisplayName("error 401 if JWT is missing")
	public void testFilterWithMissingJWT() throws NoSuchMethodException {
		Mockito.doReturn(getClass().getMethod("allowMember")).when(resourceInfo).getResourceMethod();
		Mockito.doReturn(new MultivaluedHashMap<>(Map.of(VaultRole.DEFAULT_VAULT_ID_PARAM, "7E57C0DE-0000-4000-8000-000100001111"))).when(uriInfo).getPathParameters();

		Assertions.assertThrows(NotAuthorizedException.class, () -> filter.filter(context));
	}

	@Test
	@DisplayName("error 403 if user2 tries to access 7E57C0DE-0000-4000-8000-000100001111")
	public void testFilterWithInsufficientPrivileges() throws NoSuchMethodException {
		Mockito.doReturn(getClass().getMethod("allowOwner")).when(resourceInfo).getResourceMethod();
		Mockito.doReturn(new MultivaluedHashMap<>(Map.of(VaultRole.DEFAULT_VAULT_ID_PARAM, "7E57C0DE-0000-4000-8000-000100001111"))).when(uriInfo).getPathParameters();
		Mockito.doReturn("user2").when(jwt).getSubject();

		var e = Assertions.assertThrows(ForbiddenException.class, () -> filter.filter(context));

		Assertions.assertEquals("Vault role required: OWNER", e.getMessage());
	}

	@Test
	@DisplayName("pass if user1 tries to access 7E57C0DE-0000-4000-8000-000100001111 (user1 is OWNER of vault)")
	public void testFilterSuccess1() throws NoSuchMethodException {
		Mockito.doReturn(getClass().getMethod("allowOwner")).when(resourceInfo).getResourceMethod();
		Mockito.doReturn(new MultivaluedHashMap<>(Map.of(VaultRole.DEFAULT_VAULT_ID_PARAM, "7E57C0DE-0000-4000-8000-000100001111"))).when(uriInfo).getPathParameters();
		Mockito.doReturn("user1").when(jwt).getSubject();

		Assertions.assertDoesNotThrow(() -> filter.filter(context));
	}

	@Test
	@DisplayName("pass if user2 tries to access 7E57C0DE-0000-4000-8000-000100002222 (user2 is member of group2, which is OWNER of the vault)")
	public void testFilterSuccess2() throws NoSuchMethodException {
		Mockito.doReturn(getClass().getMethod("allowOwner")).when(resourceInfo).getResourceMethod();
		Mockito.doReturn(new MultivaluedHashMap<>(Map.of(VaultRole.DEFAULT_VAULT_ID_PARAM, "7E57C0DE-0000-4000-8000-000100002222"))).when(uriInfo).getPathParameters();
		Mockito.doReturn("user2").when(jwt).getSubject();

		Assertions.assertDoesNotThrow(() -> filter.filter(context));
	}

	/*
	 * "real" methods for testing below, as we can not mock Method.class without breaking Mockito
	 */

	@VaultRole({VaultAccess.Role.MEMBER})
	public void allowMember() {}

	@VaultRole({VaultAccess.Role.OWNER})
	public void allowOwner() {}

}