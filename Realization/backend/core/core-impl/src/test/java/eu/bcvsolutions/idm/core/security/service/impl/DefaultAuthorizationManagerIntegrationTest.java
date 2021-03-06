package eu.bcvsolutions.idm.core.security.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator;
import eu.bcvsolutions.idm.core.security.evaluator.UuidEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

/**
 * Test for authorities evaluation.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultAuthorizationManagerIntegrationTest extends AbstractEvaluatorIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmAuthorizationPolicyService service;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmCacheManager cacheManager;
	@Autowired private IdmAuthorizationPolicyService authorizationPolicyService;
	//
	private DefaultAuthorizationManager manager;
	
	@Before
	public void init() {		
		super.disableDefaultRole();
		//
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultAuthorizationManager.class);
	}
	
	@Test
	public void testAuthorizableTypes() {
		Set<AuthorizableType> authorizableTypes = manager.getAuthorizableTypes();
		//
		AuthorizableType role = authorizableTypes.stream()
				.filter(a -> {
					return IdmRole.class.equals(a.getType());
				})
				.findFirst()
	            .get();
		assertNotNull(role);
	}
	
	@Test
	public void testSupportedEvaluators() {
		List<AuthorizationEvaluatorDto> dtos = manager.getSupportedEvaluators();
		//
		assertTrue(dtos.size() > 1); // TODO: improve (check uuid and base evaluator at least)
	}
	
	@Test
	public void testEvaluate() {
		loginAsAdmin();
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		getHelper().createBasePolicy(role.getId(), IdmBasePermission.READ);		
		// prepare identity
		IdmIdentityDto identity = getHelper().createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
		// assign role
		getHelper().createIdentityRole(identity, role);
		logout();
		//
		// without login
		assertFalse(manager.evaluate(role, IdmBasePermission.READ));
		assertFalse(manager.evaluate(role, IdmBasePermission.UPDATE));
		assertFalse(manager.evaluate(role, IdmBasePermission.ADMIN));
		assertFalse(manager.evaluate(role, IdmBasePermission.AUTOCOMPLETE));
		//
		try {
			getHelper().login(identity);
			//
			// evaluate	access
			assertTrue(manager.evaluate(role, IdmBasePermission.READ));
			assertFalse(manager.evaluate(role, IdmBasePermission.UPDATE));
			assertFalse(manager.evaluate(role, IdmBasePermission.ADMIN));
			assertFalse(manager.evaluate(role, IdmBasePermission.AUTOCOMPLETE));			
		} finally {
			logout();
		}
	}
	
	@Test
	public void testPredicate() {
		loginAsAdmin();
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
		getHelper().createBasePolicy(role.getId(), IdmBasePermission.AUTOCOMPLETE);	
		// prepare identity
		IdmIdentityDto identity = getHelper().createIdentity();
		identity.setPassword(new GuardedString("heslo"));
		identityService.save(identity);
		// assign role
		getHelper().createIdentityRole(identity, role);
		logout();
		//
		// empty without login
		IdmRoleFilter filter = new IdmRoleFilter();
		assertEquals(0, roleService.find(filter, null, IdmBasePermission.READ).getTotalElements());
		assertEquals(0, roleService.find(filter, null, IdmBasePermission.AUTOCOMPLETE).getTotalElements());
		//
		try {			
			getHelper().login(identity);
			//
			// evaluate	access
			assertEquals(1, roleService.find(filter, null, IdmBasePermission.READ).getTotalElements());
			assertEquals(roleService.find(null).getTotalElements(), 
					roleService.find(filter, null, IdmBasePermission.AUTOCOMPLETE).getTotalElements());			
		} finally {
			logout();
		}
	}
	
	@Test
	public void testFindValidPolicies() {
		try {
			loginAsAdmin();
			// prepare role
			IdmRoleDto role = getHelper().createRole();
			IdmRoleDto role2 = getHelper().createRole();
			getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			getHelper().createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);
			// prepare identity
			IdmIdentityDto identity = getHelper().createIdentity();
			// assign role
			getHelper().createIdentityRole(identity, role);
			getHelper().createIdentityRole(identity, role2);
			//
			assertEquals(2, service.getEnabledPolicies(identity.getId(), IdmRole.class).size());
		} finally {
			logout();
		}
	}
	
	@Test
	@Transactional
	public void testFindValidPoliciesWithInvalidRole() {
		try {
			loginAsAdmin();
			// prepare role
			IdmRoleDto role = getHelper().createRole();
			IdmRoleDto role2 = getHelper().createRole();
			role2.setDisabled(true);
			roleService.save(role2);
			getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			getHelper().createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);	
			// prepare identity
			IdmIdentityDto identity = getHelper().createIdentity();
			// assign role
			getHelper().createIdentityRole(identity, role);
			getHelper().createIdentityRole(identity, role2);
			//
			List<IdmAuthorizationPolicyDto> policies = service.getEnabledPolicies(identity.getId(), IdmRole.class);
			assertEquals(1, policies.size());
			assertEquals(role.getId(), policies.get(0).getRole());
		} finally {
			logout();
		}
	}
	
	@Test
	@Transactional
	public void testFindValidPoliciesWithInvalidIdentityRole() {
		try {
			loginAsAdmin();
			// prepare role
			IdmRoleDto role = getHelper().createRole();
			IdmRoleDto role2 = getHelper().createRole();
			getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			getHelper().createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);	
			// prepare identity
			IdmIdentityDto identity = getHelper().createIdentity();
			// assign role
			getHelper().createIdentityRole(identity, role);
			IdmIdentityRoleDto assignedRole = getHelper().createIdentityRole(identity, role2);
			assignedRole.setValidFrom(LocalDate.now().plusDays(1));
			identityRoleService.save(assignedRole);
			//
			List<IdmAuthorizationPolicyDto> policies = service.getEnabledPolicies(identity.getId(), IdmRole.class);
			assertEquals(1, policies.size());
			assertEquals(role.getId(), policies.get(0).getRole());
		} finally {
			logout();
		}
	}
	
	@Test
	public void testFindValidPoliciesWithInvalidIdentityContractByDisabled() {
		try {
			loginAsAdmin();
			// prepare role
			IdmRoleDto role = getHelper().createRole();
			IdmRoleDto role2 = getHelper().createRole();
			getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			getHelper().createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);	
			// prepare identity
			IdmIdentityDto identity = getHelper().createIdentity();
			// assign role
			getHelper().createIdentityRole(identity, role);
			IdmIdentityContractDto contract = getHelper().createContract(identity);
			contract.setState(ContractState.DISABLED);	
			identityContractService.save(contract);
			getHelper().createIdentityRole(contract, role2);
			//
			List<IdmAuthorizationPolicyDto> policies = service.getEnabledPolicies(identity.getId(), IdmRole.class);
			assertEquals(1, policies.size());
			assertEquals(role.getId(), policies.get(0).getRole());
		} finally {
			logout();
		}
	}
	
	@Test
	@Transactional
	public void testFindValidPoliciesWithInvalidIdentityContractByDates() {
		try {
			loginAsAdmin();
			// prepare role
			IdmRoleDto role = getHelper().createRole();
			IdmRoleDto role2 = getHelper().createRole();
			getHelper().createUuidPolicy(role.getId(), role.getId(), IdmBasePermission.READ);		
			getHelper().createBasePolicy(role2.getId(), IdmBasePermission.AUTOCOMPLETE);	
			// prepare identity
			IdmIdentityDto identity = getHelper().createIdentity();
			// assign role
			getHelper().createIdentityRole(identity, role);
			IdmIdentityContractDto contract = new IdmIdentityContractDto();
			contract.setIdentity(identity.getId());
			contract.setPosition("position-" + System.currentTimeMillis());
			contract.setValidFrom(LocalDate.now().plusDays(1));
			contract = identityContractService.save(contract);
			getHelper().createIdentityRole(contract, role2);
			//
			List<IdmAuthorizationPolicyDto> policies = service.getEnabledPolicies(identity.getId(), IdmRole.class);
			assertEquals(1, policies.size());
			assertEquals(role.getId(), policies.get(0).getRole());
		} finally {
			logout();
		}
	}
	
	@Test
	@Transactional
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testCache() {
		// create and login identity
		IdmIdentityDto identity = getHelper().createIdentity();
		UUID mockIdentity = UUID.randomUUID();
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		IdmAuthorizationPolicyDto policy = getHelper().createBasePolicy(role.getId(), IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		getHelper().createIdentityRole(identity, role);
		//
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_DEFINITION_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
		//
		cacheManager.cacheValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity, new HashMap<>());
		cacheManager.cacheValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity, new HashMap<>());
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
		//
		// without login
		Set<String> permissions = manager.getPermissions(role);
		Assert.assertTrue(permissions.isEmpty());
		//
		try {
			getHelper().login(identity);
			//
			// new entity is not supported with cache, but permissions are evaluated
			permissions = manager.getPermissions(new IdmRoleDto());
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			//
			// load from db
			permissions = manager.getPermissions(role);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			// load from cache
			permissions = manager.getPermissions(role);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_DEFINITION_CACHE_NAME, policy.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			// check cache content - one
			ValueWrapper cacheValue = cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId());
			List<IdmAuthorizationPolicyDto> cachedPolicies = (List) ((Map) cacheValue.get()).get(role.getClass());
			Assert.assertEquals(1, cachedPolicies.size());
			Assert.assertEquals(BasePermissionEvaluator.class.getCanonicalName(), 
					((IdmAuthorizationPolicyDto) cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_DEFINITION_CACHE_NAME, 
							cachedPolicies.get(0)).get()).getEvaluatorType());
			cacheValue = cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId());
			permissions = (Set) ((Map) cacheValue.get()).get(role.getId());
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			//
			// change policy => evict whole cache
			policy.setPermissions(IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ, IdmBasePermission.UPDATE);
			authorizationPolicyService.save(policy);
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_DEFINITION_CACHE_NAME, policy.getId()));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
			//
			cacheManager.cacheValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity, new HashMap<>());
			cacheManager.cacheValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity, new HashMap<>());
			permissions = manager.getPermissions(role);
			Assert.assertEquals(3, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.getName())));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
		} finally {
			logout(); // evict logged identity cache only
		}
		// check cache is evicted only for logged identity
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
	}
	
	@Test
	@Transactional
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testCacheAfterContractIsChanged() {
		// create and login identity
		IdmIdentityDto identity = getHelper().createIdentity();
		UUID mockIdentity = UUID.randomUUID();
		// prepare role
		IdmRoleDto role = getHelper().createRole();
		getHelper().createBasePolicy(role.getId(), IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		getHelper().createIdentityRole(identity, role);
		//
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
		Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
		//
		cacheManager.cacheValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity, new HashMap<>());
		cacheManager.cacheValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity, new HashMap<>());
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
		Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
		//
		// without login
		Set<String> permissions = manager.getPermissions(role);
		Assert.assertTrue(permissions.isEmpty());
		//
		try {
			getHelper().login(identity);
			//
			// new entity is not supported with cache, but permissions are evaluated
			permissions = manager.getPermissions(new IdmRoleDto());
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			//
			// load from db
			permissions = manager.getPermissions(role);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			// load from cache
			permissions = manager.getPermissions(role);
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			// check cache content - one
			ValueWrapper cacheValue = cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId());
			List<UUID> cachedPolicies = (List) ((Map) cacheValue.get()).get(role.getClass());
			Assert.assertEquals(1, cachedPolicies.size());
			Assert.assertEquals(BasePermissionEvaluator.class.getCanonicalName(), 
					((IdmAuthorizationPolicyDto) cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_DEFINITION_CACHE_NAME, 
							cachedPolicies.get(0)).get()).getEvaluatorType());
			cacheValue = cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId());
			permissions = (Set) ((Map) cacheValue.get()).get(role.getId());
			Assert.assertEquals(2, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			//
			// change contract => evict cache of logged identity
			getHelper().createContract(identity);
			//
			// check cache is evicted only for logged identity
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identity.getId()));
			Assert.assertNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, identity.getId()));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, mockIdentity));
			Assert.assertNotNull(cacheManager.getValue(AuthorizationManager.PERMISSION_CACHE_NAME, mockIdentity));
		} finally {
			logout();
		}
	}
	
	@Test
	@Transactional
	public void testDistictPolicies() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto roleTwo = getHelper().createRole();
		IdmAuthorizationPolicyDto policy = getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdmBasePermission.AUTOCOMPLETE, 
				IdmBasePermission.READ);
		getHelper().createIdentityRole(identity, role);
		getHelper().createIdentityRole(identity, role);
		getHelper().createIdentityRole(identity, roleTwo);
		getHelper().createIdentityRole(identity, roleTwo);
		//
		List<IdmAuthorizationPolicyDto> enabledDistinctPolicies = manager.getEnabledDistinctPolicies(identity.getId(), IdmIdentity.class);
		Assert.assertEquals(1, enabledDistinctPolicies.size());
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policy.getId())));
		//
		IdmAuthorizationPolicyDto policyTwo = getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdmBasePermission.READ,
				IdmBasePermission.AUTOCOMPLETE);
		//
		enabledDistinctPolicies = manager.getEnabledDistinctPolicies(identity.getId(), IdmIdentity.class);
		Assert.assertEquals(1, enabledDistinctPolicies.size());
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policy.getId()) || p.getId().equals(policyTwo.getId())));
		//
		IdmAuthorizationPolicyDto policyThree = getHelper().createBasePolicy(
				role.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				IdmBasePermission.AUTOCOMPLETE);
		//
		enabledDistinctPolicies = manager.getEnabledDistinctPolicies(identity.getId(), IdmIdentity.class);
		Assert.assertEquals(2, enabledDistinctPolicies.size());
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policy.getId()) || p.getId().equals(policyTwo.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policyThree.getId())));
		//
		// with parameters
		ConfigurationMap propsFour = new ConfigurationMap();
		propsFour.put("one", "valueOne");
		propsFour.put("two", "valueTwo");
		IdmAuthorizationPolicyDto policyFour = getHelper().createAuthorizationPolicy(
				roleTwo.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				UuidEvaluator.class,
				propsFour,
				IdmBasePermission.READ,
				IdmBasePermission.AUTOCOMPLETE);
		//
		ConfigurationMap propsFive = new ConfigurationMap();
		propsFive.put("two", "valueTwo");
		propsFive.put("one", "valueOne");
		IdmAuthorizationPolicyDto policyFive = getHelper().createAuthorizationPolicy(
				roleTwo.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				UuidEvaluator.class,
				propsFive,
				IdmBasePermission.AUTOCOMPLETE,
				IdmBasePermission.READ);
		//
		enabledDistinctPolicies = manager.getEnabledDistinctPolicies(identity.getId(), IdmIdentity.class);
		Assert.assertEquals(3, enabledDistinctPolicies.size());
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policy.getId()) || p.getId().equals(policyTwo.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policyThree.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policyFour.getId()) || p.getId().equals(policyFive.getId())));
		//
		ConfigurationMap propsSix = new ConfigurationMap();
		propsSix.put("one", "valueOneU");
		propsSix.put("two", "valueTwo");
		IdmAuthorizationPolicyDto policySix = getHelper().createAuthorizationPolicy(
				roleTwo.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				UuidEvaluator.class,
				propsSix,
				IdmBasePermission.AUTOCOMPLETE,
				IdmBasePermission.READ);
		//
		ConfigurationMap propsSeven = new ConfigurationMap();
		propsSeven.put("one", "valueOneU");
		propsSeven.put("two", "valueTwo");
		IdmAuthorizationPolicyDto policySeven = getHelper().createAuthorizationPolicy(
				roleTwo.getId(), 
				CoreGroupPermission.IDENTITY,
				IdmIdentity.class,
				UuidEvaluator.class,
				propsSeven,
				IdmBasePermission.READ);
		//
		enabledDistinctPolicies = manager.getEnabledDistinctPolicies(identity.getId(), IdmIdentity.class);
		Assert.assertEquals(5, enabledDistinctPolicies.size());
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policy.getId()) || p.getId().equals(policyTwo.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policyThree.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policyFour.getId()) || p.getId().equals(policyFive.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policySix.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policySeven.getId())));
	}
	
	@Test
	@Transactional
	public void testDefaultRoleSubRoles() {
		IdmIdentityDto identity = getHelper().createIdentity();
		// create new default role with two enabled sub roles + one disabled.
		IdmRoleDto defaultRole = getHelper().createRole();
		IdmRoleDto subRoleOne = getHelper().createRole();
		IdmRoleDto subRoleTwo = getHelper().createRole();
		IdmRoleDto role = getHelper().createRole();
		role.setDisabled(true);
		IdmRoleDto disabledSubRole = roleService.save(role);
		getHelper().createRoleComposition(defaultRole, subRoleOne);
		getHelper().createRoleComposition(subRoleOne, subRoleTwo);
		getHelper().createRoleComposition(defaultRole, disabledSubRole);
		//
		// create distinct authorization policies
		IdmAuthorizationPolicyDto policyDefault = getHelper().createBasePolicy(defaultRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.CREATE);
		IdmAuthorizationPolicyDto policyOne = getHelper().createBasePolicy(subRoleOne.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.UPDATE);
		IdmAuthorizationPolicyDto policyTwo = getHelper().createBasePolicy(subRoleTwo.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.READ);
		IdmAuthorizationPolicyDto policyDisabled =  getHelper().createBasePolicy(disabledSubRole.getId(), CoreGroupPermission.IDENTITY, IdmIdentity.class, IdmBasePermission.DELETE);
		//
		getHelper().setConfigurationValue(RoleConfiguration.PROPERTY_DEFAULT_ROLE, defaultRole.getCode());
		//
		List<IdmAuthorizationPolicyDto> enabledDistinctPolicies = manager.getEnabledDistinctPolicies(identity.getId(), IdmIdentity.class);
		Assert.assertEquals(3, enabledDistinctPolicies.size());
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policyDefault.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policyOne.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().anyMatch(p -> p.getId().equals(policyTwo.getId())));
		Assert.assertTrue(enabledDistinctPolicies.stream().allMatch(p -> !p.getId().equals(policyDisabled.getId())));
		//
		Set<String> authorities = manager.getAuthorities(identity.getId(), IdmIdentity.class);
		Assert.assertEquals(3, authorities.size());
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.equals(IdmBasePermission.CREATE.getName())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.equals(IdmBasePermission.UPDATE.getName())));
		Assert.assertTrue(authorities.stream().anyMatch(a -> a.equals(IdmBasePermission.READ.getName())));
		Assert.assertTrue(authorities.stream().allMatch(a -> !a.equals(IdmBasePermission.DELETE.getName())));
		//
		try {
			getHelper().login(identity);
			Set<String> permissions = identityService.getPermissions(identity.getId());
			Assert.assertEquals(3, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.CREATE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.UPDATE.getName())));
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.READ.getName())));
			Assert.assertTrue(permissions.stream().allMatch(p -> !p.equals(IdmBasePermission.DELETE.getName())));
		} finally {
			logout();
		}
	}
}
