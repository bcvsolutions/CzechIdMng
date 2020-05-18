package eu.bcvsolutions.idm.core.security.evaluator.role;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.model.entity.IdmRequest;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Currently logged user can work with requests, when identity was involved to approving.
 * 
 * Search ({@link #getPermissions}) is not implemented.
 * 
 * @author Vít Švanda
 */
@Component
@Description("Currently logged user can work with requests, when identity was involved to approving.")
public class RequestByWfInvolvedIdentityEvaluator extends AbstractAuthorizationEvaluator<IdmRequest> {
	
	private final WorkflowProcessInstanceService processService;
	private final SecurityService securityService;
	
	@Autowired
	public RequestByWfInvolvedIdentityEvaluator(
			SecurityService securityService,
			WorkflowProcessInstanceService processService) {
		Assert.notNull(securityService, "Service is required.");
		Assert.notNull(processService, "Service is required.");
		//
		this.securityService = securityService;
		this.processService = processService;
	}
	
	@Override
	public Set<String> getPermissions(IdmRequest entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null || !securityService.isAuthenticated() || entity.getWfProcessId() == null) {
			return permissions;
		}
		//
		// search process instance by role request - its returned, if currently logged identity was involved in wf
		WorkflowProcessInstanceDto processInstance = processService.get(entity.getWfProcessId(), true);
		if (processInstance != null) {
			permissions.addAll(policy.getPermissions());
		}		
		return permissions;
	}
}
