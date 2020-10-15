package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete given roles
 *
 * @author svandav
 * @author Ondrej Husnik
 *
 */
@Component(RoleDeleteBulkAction.NAME)
@Description("Delete given roles.")
public class RoleDeleteBulkAction extends AbstractRemoveBulkAction<IdmRoleDto, IdmRoleFilter> {

	public static final String NAME = "role-delete-bulk-action";

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ROLE_DELETE);
	}

	@Override
	public ResultModels prevalidate() {
		IdmBulkActionDto action = getAction();
		List<UUID> entities = getEntities(action, new StringBuilder());
		ResultModels result = new ResultModels();

		Map<ResultModel, Long> models = new HashMap<>();
		entities.forEach(roleId -> {
			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
			identityRoleFilter.setRoleId(roleId);
			IdmRoleDto role = getService().get(roleId);
			long count = identityRoleService.count(identityRoleFilter);
			if (count > 0) {
				models.put(new DefaultResultModel(CoreResultCode.ROLE_DELETE_BULK_ACTION_NUMBER_OF_IDENTITIES,
						ImmutableMap.of("role", role.getCode(), "count", count)), count);
			}
		});
		
		long conceptsToModify = entities //
				.stream() //
				.map(roleId -> {
					IdmConceptRoleRequestFilter roleRequestFilter = new IdmConceptRoleRequestFilter();
					roleRequestFilter.setRoleId(roleId);
					return conceptRoleRequestService.count(roleRequestFilter);
				})
				.reduce(0L, Long::sum);

		ResultModel conceptCountResult = null;
		if (conceptsToModify > 0) {
			conceptCountResult = new DefaultResultModel(CoreResultCode.ROLE_DELETE_BULK_ACTION_CONCEPTS_TO_MODIFY,
					ImmutableMap.of("conceptCount", conceptsToModify));
		}
		
		// Sort by count
		List<Entry<ResultModel, Long>> collect = models //
				.entrySet() //
				.stream() //
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //
				.limit(5) //
				.collect(Collectors.toList()); //
		collect.forEach(entry -> {
			result.addInfo(entry.getKey());
		});
		
		if (conceptCountResult != null) {
			result.addInfo(conceptCountResult);
		}

		return result;
	}

	@Override
	public ReadWriteDtoService<IdmRoleDto, IdmRoleFilter> getService() {
		return roleService;
	}
}
