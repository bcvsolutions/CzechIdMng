package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Executes account management and provisioning
 * 
 * @author Radek Tomiška
 * @author Vít Švanda
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(IdentityAccountManagementBulkAction.NAME)
@Description("Executes account management and provisioning")
public class IdentityAccountManagementBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "acc-identity-account-management-bulk-action";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private SecurityService securityService;
	@Autowired private AccAccountManagementService accountManagementService;
	@Autowired private ProvisioningService provisioningService;
	
	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		if (!securityService.hasAnyAuthority(AccGroupPermission.SYSTEM_ADMIN)) {
			throw new ForbiddenEntityException((BaseDto)dto, AccGroupPermission.SYSTEM);
		}
		// Execute account management for entire identity (for all identity-roles)
		accountManagementService.resolveIdentityAccounts(dto);
		// Execute provisioning for entire identity
		provisioningService.doProvisioning(dto);
		//
		return new OperationResult(OperationState.EXECUTED);
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> authorities = super.getAuthorities();
		authorities.add(AccGroupPermission.SYSTEM_ADMIN);
		return authorities;
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITY_READ, CoreGroupPermission.IDENTITY_UPDATE);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 1500;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
