package eu.bcvsolutions.idm.core.security.service.impl;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Token integration test
 * - CRUD
 * - purge old tokens
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultTokenManagerIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmCacheManager cacheManager;
	//
	private DefaultTokenManager manager;
	
	@Before
	public void init() {
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultTokenManager.class);
	}
	
	@Test
	public void testOwnerType() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		//
		Assert.assertNotNull(manager.getOwnerType(owner));
		Assert.assertEquals(manager.getOwnerType(owner), manager.getOwnerType(owner.getClass()));
	}
	
	@Test
	public void testCrudToken() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		IdmTokenDto token = createToken(owner, null, null);
		//
		Assert.assertNotNull(token.getId());
		Assert.assertEquals(owner.getId(), token.getOwnerId());
		Assert.assertEquals(manager.getOwnerType(owner), token.getOwnerType());
		//
		IdmTokenDto getToken = manager.getToken(token.getId());
		//
		Assert.assertEquals(token.getId(), getToken.getId());
		Assert.assertEquals(owner.getId(), getToken.getOwnerId());
		Assert.assertFalse(getToken.isDisabled());
		//
		List<IdmTokenDto> tokens = manager.getTokens(owner);
		//
		Assert.assertEquals(1, tokens.size());
		Assert.assertEquals(token.getId(), tokens.get(0).getId());
		//
		manager.disableTokens(owner);
		//
		getToken = manager.getToken(token.getId());
		Assert.assertEquals(token.getId(), getToken.getId());
		Assert.assertEquals(owner.getId(), getToken.getOwnerId());
		Assert.assertTrue(getToken.isDisabled());
		//
		manager.deleteTokens(owner);
		//
		tokens = manager.getTokens(owner);
		Assert.assertTrue(tokens.isEmpty());
		Assert.assertNull(manager.getToken(token.getId()));
	}
	
	@Test
	public void testVerifyTokenOk() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		IdmTokenDto token = createToken(owner, null, null);
		//
		Assert.assertEquals(token.getId(), manager.verifyToken(token.getId()).getId());
		
		token = createToken(owner, null, ZonedDateTime.now().plusMinutes(1));
		//
		Assert.assertEquals(token.getId(), manager.verifyToken(token.getId()).getId());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testVerifyTokenNotExists() {
		manager.verifyToken(UUID.randomUUID());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testVerifyTokenDisabled() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		IdmTokenDto token = createToken(owner, null, ZonedDateTime.now().minusNanos(1));
		token.setDisabled(true);
		token = manager.saveToken(owner, token);
		//
		manager.verifyToken(token.getId());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testVerifyTokenExpired() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		IdmTokenDto token = createToken(owner, null, ZonedDateTime.now().minusNanos(1));
		//
		manager.verifyToken(token.getId());
	}
	
	/**
	 * Disabled token has to set expiration
	 */
	@Test
	public void testDisableSetExpiration() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		IdmTokenDto token = createToken(owner, null, null);
		//
		manager.disableTokens(owner);
		//
		token = manager.getToken(token.getId());
		Assert.assertTrue(token.isDisabled());
		Assert.assertNotNull(token.getExpiration());
		Assert.assertFalse(token.getExpiration().isAfter(ZonedDateTime.now()));
		//
		token = createToken(owner, null, ZonedDateTime.now().plusDays(1));
		//
		manager.disableTokens(owner);
		//
		token = manager.getToken(token.getId());
		Assert.assertTrue(token.isDisabled());
		Assert.assertNotNull(token.getExpiration());
		Assert.assertFalse(token.getExpiration().isAfter(ZonedDateTime.now()));
		//
		ZonedDateTime expired = ZonedDateTime.now().minusDays(1);
		token = createToken(owner, null, expired);
		//
		manager.disableTokens(owner);
		//
		token = manager.getToken(token.getId());
		Assert.assertTrue(token.isDisabled());
		Assert.assertNotNull(token.getExpiration());
		Assert.assertEquals(expired, token.getExpiration());
	}
	
	@Test
	public void testPurgeTokens() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		String typeOne = getHelper().createName();
		String typeTwo = getHelper().createName();
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		//
		createToken(owner, typeOne, null);
		createToken(owner, typeOne, now.plusDays(1));
		createToken(owner, typeOne, now);
		createToken(owner, typeOne, now.minusDays(1));
		IdmTokenDto tokenOneExpiredTwoDaysBefore = createToken(owner, typeOne, now.minusDays(2));
		createToken(owner, typeTwo, null);
		createToken(owner, typeTwo, now.plusDays(1));
		createToken(owner, typeTwo, now);
		createToken(owner, typeTwo, now.minusDays(1));
		IdmTokenDto tokenTwoExpiredTwoDaysBefore = createToken(owner, typeTwo, now.minusDays(2));
		
		List<IdmTokenDto> tokens = manager.getTokens(owner);
		Assert.assertEquals(10, tokens.size());
		// 2 weeks by default 
		// TODO: configurable
		manager.purgeTokens();
		//
		tokens = manager.getTokens(owner);
		Assert.assertEquals(10, tokens.size());
		//
		manager.purgeTokens(typeTwo, now.minusDays(1));
		//
		tokens = manager.getTokens(owner);
		Assert.assertEquals(9, tokens.size());
		Assert.assertFalse(tokens.stream().anyMatch(t -> t.getId().equals(tokenTwoExpiredTwoDaysBefore.getId())));
		//
		manager.purgeTokens(null, now.minusDays(1));
		//
		tokens = manager.getTokens(owner);
		Assert.assertEquals(8, tokens.size());
		Assert.assertFalse(tokens.stream().anyMatch(t -> t.getId().equals(tokenOneExpiredTwoDaysBefore.getId())));
		//
		manager.purgeTokens(typeOne, null);
		//
		tokens = manager.getTokens(owner);
		Assert.assertEquals(4, tokens.size());
		Assert.assertFalse(tokens.stream().anyMatch(t -> t.getTokenType().equals(typeOne)));
		//
		manager.purgeTokens(null, null);
		//
		tokens = manager.getTokens(owner);
		Assert.assertTrue(tokens.isEmpty());
	}
	
	@Test
	public void testDisableToken() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		IdmTokenDto token = createToken(owner, null, null);
		Assert.assertNull(token.getExpiration());
		Assert.assertFalse(token.isDisabled());
		//
		token = manager.disableToken(token.getId());
		//
		Assert.assertNotNull(token.getExpiration());
		Assert.assertTrue(token.isDisabled());
	}
	
	protected IdmTokenDto createToken(IdmIdentityDto owner, String tokenType, ZonedDateTime expiration) {
		IdmTokenDto token = new IdmTokenDto();
		token.setIssuedAt(ZonedDateTime.now());
		token.setToken("mock");
		token.setExpiration(expiration);
		if (tokenType != null) {
			token.setTokenType(tokenType);
		} else {
			token.setTokenType("mock");
		}
		//
		return manager.saveToken(owner, token);
	}
	
	@Test
	public void testDisableTokenAfterIdentityIsDeleted() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTokenDto token = createToken(identity, null, null);
		Assert.assertFalse(token.isDisabled());
		//
		identityService.delete(identity);
		//
		token = manager.getToken(token.getId());
		//
		Assert.assertTrue(token.isDisabled());
	}
	
	@Test
	public void testDisableTokenAfterIdentityIsDisabled() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTokenDto token = createToken(identity, null, null);
		Assert.assertFalse(token.isDisabled());
		//
		identityService.disable(identity.getId());
		//
		token = manager.getToken(token.getId());
		//
		Assert.assertTrue(token.isDisabled());
	}
	
	@Test
	public void testEvictTokenCache() {
		IdmIdentityDto owner = new IdmIdentityDto(UUID.randomUUID());
		IdmTokenDto token = createToken(owner, null, null);
		token = manager.getToken(token.getId());
		//
		Assert.assertNotNull(cacheManager.getValue(TokenManager.TOKEN_CACHE_NAME, token.getId()));
		//
		token.setDisabled(true);
		manager.saveToken(owner, token);
		//
		Assert.assertNull(cacheManager.getValue(TokenManager.TOKEN_CACHE_NAME, token.getId()));
	}
}
