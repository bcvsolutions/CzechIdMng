package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmAuthorizationPolicyService
		extends ReadWriteDtoService<IdmAuthorizationPolicyDto, IdmAuthorizationPolicyFilter>,
		AuthorizableService<IdmAuthorizationPolicyDto> {
	
	/**
	 * Returns all enabled policies for given identity and entity type
	 * 
	 * @param identityId identity's id
	 * @param entityType
	 * @return
	 */
	List<IdmAuthorizationPolicyDto> getEnabledPolicies(UUID identityId, Class<? extends Identifiable> entityType);
	
	/**
	 * Returns active role's authorities by configured policies for given identity
	 * 
	 * @param identityId
	 * @param role
	 */
	Set<GrantedAuthority> getEnabledRoleAuthorities(UUID identityId, UUID roleId);
	
	/**
	 * Returns role policies
	 * 
	 * @param roleId
	 * @param disabled
	 * @return
	 */
	List<IdmAuthorizationPolicyDto> getRolePolicies(UUID roleId, boolean disabled);
	
	/**
	 * Returns authorities from default user role by configuration {@value #PROPERTY_DEFAULT_ROLE} for given identity.
	 * Sub roles are supported @since 10.5.0.
	 * Authorities are loaded on login only.
	 * 
	 * @param identityId logged identity
	 * @return default role authorities.
	 * @see RoleConfiguration#getDefaultRole()
	 */
	Set<GrantedAuthority> getDefaultAuthorities(UUID identityId);
	
	/**
	 * Returns policies from default user role by configuration {@value IdmRoleService#PROPERTY_DEFAULT_ROLE}.
	 * Sub roles are supported @since 10.5.0.
	 * 
	 * @param entityType policies by given entity type (~authorizable type)
	 * @return
	 * @see RoleConfiguration#getDefaultRole()
	 */
	List<IdmAuthorizationPolicyDto> getDefaultPolicies(Class<? extends Identifiable> entityType);

	/**
	 * Returns a set of granted authorities from enabled authorization policies for given identity.
	 * 
	 * @param identityId
	 * @param policies
	 * @return
	 */
	Set<GrantedAuthority> getGrantedAuthorities(UUID identityId, List<IdmAuthorizationPolicyDto> policies);
	
	
}
