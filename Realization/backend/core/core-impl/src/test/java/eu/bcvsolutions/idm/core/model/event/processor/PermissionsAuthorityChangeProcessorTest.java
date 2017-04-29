package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Tests IdmRole's authority modifications, which must set new
 * IdmAuthorityChange timestamp for all identities in given role.
 * 
 * To compute the difference in role authorities, one must checkout the
 * original entity from storage, {@see IdmRoleRepository#getPersistedRoleAuthorities(IdmRole)}.
 * But since tests are usually run in single transaction, the role
 * is not persisted into the storage and we can not find the original
 * entity. Therefore there are multiple transactions in each test,
 * one to create new role, another to update its authorities.
 * 
 * @author Jan Helbich
 *
 */
public class PermissionsAuthorityChangeProcessorTest extends AbstractIdentityAuthoritiesProcessorTest {
	
	@Autowired
	private IdmAuthorizationPolicyRepository policyRepository;
	
	@Autowired
	private SecurityService securityService;
	
	@Test
	public void testRemoveAuthorityUpdateUsers() throws Exception {
		IdmRole role = getTestRole();
		IdmIdentity i = getTestUser();
		IdmIdentityContract c = getTestContract(i);
		getTestIdentityRole(role, c);
		
		IdmAuthorityChange ac = acRepository.findByIdentity(i);
		Assert.assertNotNull(ac);
		Assert.assertNotNull(ac.getAuthChangeTimestamp());
		DateTime origChangeTime = ac.getAuthChangeTimestamp();
		
		sleep();
		
		clearAuthPolicies(role);
		
		ac = acRepository.findByIdentity(i);
		Assert.assertNotNull(ac);
		Assert.assertNotNull(ac.getAuthChangeTimestamp());
		Assert.assertTrue(origChangeTime.getMillis() < ac.getAuthChangeTimestamp().getMillis());
	}

	@Test
	public void testAddAuthorityUpdateUsers() throws Exception {
		IdmRole role = getTestRole();
		IdmIdentity i = getTestUser();
		IdmIdentityContract c = getTestContract(i);
		getTestIdentityRole(role, c);
		
		IdmAuthorityChange ac = acRepository.findByIdentity(i);
		Assert.assertNotNull(ac);
		Assert.assertNotNull(ac.getAuthChangeTimestamp());
		DateTime origChangeTime = ac.getAuthChangeTimestamp();
		
		sleep();
		
		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus transactionStatus) {
				getTestPolicy(role, IdmBasePermission.EXECUTE, IdmGroupPermission.APP);
				return null;
			}
		});

		ac = acRepository.findByIdentity(i);
		Assert.assertNotNull(ac);
		Assert.assertNotNull(ac.getAuthChangeTimestamp());
		Assert.assertTrue(origChangeTime.getMillis() < ac.getAuthChangeTimestamp().getMillis());
	}
	
	/**
	 * In case the identity in role does not have IdmAuthorityChange entity
	 * relation, changing role's authorities must create one.
	 * @throws Exception
	 */
	@Test
	public void testCreateAuthorityChangeEntity() throws Exception {
		IdmRole role = getTestRole();
		IdmIdentity i = getTestUser();
		IdmIdentityContract c = getTestContract(i);
		getTestIdentityRole(role, c);
		
		deleteAuthorityChangedEntity(i);
		IdmAuthorityChange ac = acRepository.findByIdentity(i);
		Assert.assertNull(ac);
		
		sleep();
		
		clearAuthPolicies(role);
		
		ac = acRepository.findByIdentity(i);
		Assert.assertNotNull(ac);
		Assert.assertNotNull(ac.getAuthChangeTimestamp());
	}
	
	/**
	 * Change permissions type for given policy.
	 * @throws Exception
	 */
	@Test
	public void testChangePersmissions() throws Exception {
		securityService.setSystemAuthentication();
		
		IdmRole role = getTestRole();
		IdmIdentity i = getTestUser();
		IdmIdentityContract c = getTestContract(i);
		getTestIdentityRole(role, c);
		
		IdmAuthorityChange ac = acRepository.findByIdentity(i);
		Assert.assertNotNull(ac);
		Assert.assertNotNull(ac.getAuthChangeTimestamp());
		DateTime origChangeTime = ac.getAuthChangeTimestamp();
		
		sleep();
		
		changeAuthorizationPolicyPermissions(role);
		
		ac = acRepository.findByIdentity(i);
		Assert.assertNotNull(ac);
		Assert.assertNotNull(ac.getAuthChangeTimestamp());
		Assert.assertTrue(origChangeTime.getMillis() < ac.getAuthChangeTimestamp().getMillis());
	}

	private void changeAuthorizationPolicyPermissions(IdmRole role) {
		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				policyRepository.getPolicies(role.getId(), false)
				.forEach(policy -> {
					policy.setGroupPermission(CoreGroupPermission.AUDIT_READ);
					authorizationPolicyService.save(authorizationPolicyService.toDto(policy, null));
				});
				return null;
			}
		});
	}

	private void deleteAuthorityChangedEntity(IdmIdentity i) {
		// delete authority change to simulate empty relation
		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				IdmAuthorityChange ac = acRepository.findByIdentity(i);
				acRepository.delete(ac);
				return null;
			}
		});
	}

	private void sleep() throws InterruptedException {
		// simulation of passing time for timestamp comparison
		Thread.sleep(10);
	}

	private void clearAuthPolicies(IdmRole role) {
		getTransactionTemplate().execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus transactionStatus) {
				List<IdmAuthorizationPolicy> policies = policyRepository.getPolicies(role.getId(), false);
				policies.forEach(policy -> authorizationPolicyService.delete(authorizationPolicyService.toDto(policy, null)));
				return null;
			}
		});
	}

}
