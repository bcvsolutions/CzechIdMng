package eu.bcvsolutions.idm.core.security.evaluator.role;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractTransitiveEvaluator;

/**
 * Permissions to authorization policies by role
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Permissions to authorization policies by role")
public class AuthorizationPolicyByRoleEvaluator extends AbstractTransitiveEvaluator<IdmAuthorizationPolicy> {

	@Autowired private AuthorizationManager authorizationManager;
	@Autowired private SecurityService securityService;
	
	@Override
	protected Identifiable getOwner(IdmAuthorizationPolicy entity) {
		return entity.getRole();
	}
	
	@Override
	protected Class<? extends Identifiable> getOwnerType() {
		return IdmRole.class;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmAuthorizationPolicy> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasAuthority(securityService.getCurrentId(), policy, permission)) {
			return null;
		}
		// identity subquery
		Subquery<IdmRole> subquery = query.subquery(IdmRole.class);
		Root<IdmRole> subRoot = subquery.from(IdmRole.class);
		subquery.select(subRoot);		
		subquery.where(builder.and(
				authorizationManager.getPredicate(subRoot, query, builder, permission),
				builder.equal(root.get(IdmAuthorizationPolicy_.role	), subRoot) // correlation attribute
				));
		//
		return builder.exists(subquery);
	}
}
