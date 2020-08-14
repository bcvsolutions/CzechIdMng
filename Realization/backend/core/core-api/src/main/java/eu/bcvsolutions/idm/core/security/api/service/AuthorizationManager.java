package eu.bcvsolutions.idm.core.security.api.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.CoreModule;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;

/**
 * Provides authorization evaluators to target read / write services
 * 
 * @author Radek Tomiška
 */
public interface AuthorizationManager {
	
	String PERMISSION_CACHE_NAME = String.format("%s:permission-cache", CoreModule.MODULE_ID);
	String AUTHORIZATION_POLICY_CACHE_NAME = String.format("%s:authorization-policy-cache", CoreModule.MODULE_ID);
	
	/**
	 * Return security predicate for given permissions and root by {@link BaseEntity} type.
	 * Calls all registered and enabled {@link AuthorizationEvaluator} which support {@link BaseEntity} type.
	 * 
	 * @param root evaluated {@link BaseEntity} type root
	 * @param query
	 * @param builder
	 * @param permission permissions to evaluate (and)
	 * @return Never returns {@code null}.
	 */
	<E extends Identifiable> Predicate getPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder, BasePermission... permission);
	
	/**
	 * Returns, what logged user could do with given entity
	 * 
	 * @param entity
	 * @return
	 */
	<E extends Identifiable> Set<String> getPermissions(E entity);
	
	/**
	 * Returns, what logged user could do with given entity by given policy
	 * 
	 * @param entity
	 * @param policy
	 * @return
	 */
	<E extends Identifiable> Set<String> getPermissions(E entity, AuthorizationPolicy policy);
	
	/**
	 * Returns authorities, what given identity could do with given domain authorizable type.
	 * 
	 * @param identityId
	 * @param authorizableType
	 * @return
	 */
	<E extends Identifiable> Set<String> getAuthorities(UUID identityId, Class<E> authorizableType);
	
	/**
	 * Returns base authorities configured for given policy. Authorities are used as "what given identity" could do - without entity is defined.
	 * 
	 * @param identityId
	 * @param policy
	 * @return
	 */
	Set<String> getAuthorities(UUID identityId, AuthorizationPolicy policy);
	
	/**
	 * Returns true, when currently logged user has all given permissions on given entity.
	 * 
	 * @param entity
	 * @param permission permissions to evaluate (AND)
	 * @return
	 */
	<E extends Identifiable> boolean evaluate(E entity, BasePermission... permission);
	
	/**
	 * Returns supported evaluators definitions
	 * 
	 * @return
	 */
	List<AuthorizationEvaluatorDto> getSupportedEvaluators();
	
	/**
	 * Returns all authorizable types
	 * 
	 * @return
	 */
	Set<AuthorizableType> getAuthorizableTypes();
}
