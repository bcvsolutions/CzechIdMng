package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Bulk operation for save identity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("identitySaveBulkAction")
@Description("Bulk action save identity.")
public class IdentitySaveBulkAction extends AbstractIdentityBulkAction {

	public static final String NAME = "identity-save-bulk-action";
	
	public static final String ONLY_NOTIFY_CODE = "onlyNotify";
	
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private EntityEventManager entityEventManager;
	
	@Override
	protected OperationResult processIdentity(IdmIdentityDto dto) {
		if (isOnlyNotify()) {
			entityEventManager.changedEntity(dto);
		} else {
			identityService.save(dto);
		}
		return new OperationResult(OperationState.EXECUTED);
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getOnlyNotifyAttribute());
		return formAttributes;
	}
	
	@Override
	protected BasePermission[] getPermissionForIdentity() {
		BasePermission[] permissions =  {
				IdmBasePermission.UPDATE,
				IdmBasePermission.READ
		};
		return permissions;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	/**
	 * Is set only notify event
	 *
	 * @return
	 */
	private boolean isOnlyNotify() {
		Boolean onlyNotify = this.getParameterConverter().toBoolean(getProperties(), ONLY_NOTIFY_CODE);
		return onlyNotify != null ? onlyNotify.booleanValue() : false;
	}

	/**
	 * Get {@link IdmFormAttributeDto} for checkbox only notify
	 *
	 * @return
	 */
	private IdmFormAttributeDto getOnlyNotifyAttribute() {
		IdmFormAttributeDto primaryContract = new IdmFormAttributeDto(
				ONLY_NOTIFY_CODE, 
				ONLY_NOTIFY_CODE, 
				PersistentType.BOOLEAN);
		primaryContract.setDefaultValue(Boolean.FALSE.toString());
		return primaryContract;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 1500;
	}
}