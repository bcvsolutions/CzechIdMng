package eu.bcvsolutions.idm.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Abstract class for testing bulk actions
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class AbstractBulkActionTest extends AbstractIntegrationTest {

	@Autowired
	protected BulkActionManager bulkActionManager;
	@Autowired
	protected IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	protected IdmConfigurationService configurationService;
	@Autowired
	protected IdmProcessedTaskItemService processedTaskItemService;
	@Autowired
	protected ObjectMapper objectMapper;
	
	/**
	 * Return processed items for long running task
	 *
	 * @param longRunningTask
	 * @return
	 */
	protected List<IdmProcessedTaskItemDto> getItemsForLrt(IdmLongRunningTaskDto longRunningTask) {
		return processedTaskItemService.findLogItems(longRunningTask, null).getContent();
	}
	
	/**
	 * Find bulk action
	 *
	 * @param entity
	 * @param name
	 * @return
	 */
	protected IdmBulkActionDto findBulkAction(Class<? extends BaseEntity> entity, String name) {
		List<IdmBulkActionDto> actions = bulkActionManager.getAvailableActions(entity);
		assertFalse(actions.isEmpty());
		
		for (IdmBulkActionDto action : actions) {
			if (action.getName().equals(name)) {
				return action;
			}
		}
		fail("For entity class: " + entity.getSimpleName() + " was not found bulk action: " + name);
		return null;
	}
	
	/**
	 * Find bulk action
	 *
	 * @param dto
	 * @param name
	 * @return
	 */
	protected IdmBulkActionDto findBulkActionForDto(Class<? extends BaseDto> dto, String name) {
		List<IdmBulkActionDto> actions = bulkActionManager.getAvailableActionsForDto(dto);
		assertFalse(actions.isEmpty());
		
		for (IdmBulkActionDto action : actions) {
			if (action.getName().equals(name)) {
				return action;
			}
		}
		fail("For entity class: " + dto.getSimpleName() + " was not found bulk action: " + name);
		return null;
	}

	/**
	 * Create user with given base permission
	 *
	 * @param permissions
	 * @return
	 */
	protected IdmIdentityDto createUserWithAuthorities(BasePermission ...permissions) {
		IdmIdentityDto createIdentity = getHelper().createIdentity();
		
		IdmRoleDto createRole = getHelper().createRole();
		
		getHelper().createBasePolicy(createRole.getId(), permissions);
		getHelper().createIdentityRole(createIdentity, createRole);
		
		return createIdentity;
	}
	
	/**
	 * Create list of identities, without passwords
	 *
	 * @param count
	 * @return
	 */
	protected List<IdmIdentityDto> createIdentities(int count) {
		List<IdmIdentityDto> identites = new ArrayList<>(count);
		//
		for (int index = 0; index < count; index++) {
			// create identity without password
			identites.add(getHelper().createIdentity(getHelper().createName(), null));
		}
		//
		return identites;
	}
	
	/**
	 * Create list of roles
	 *
	 * @param count
	 * @return
	 */
	protected List<IdmRoleDto> createRoles(int count) {
		List<IdmRoleDto> roles = new ArrayList<>(count);
		//
		for (int index = 0; index < count; index++) {
			// create identity without password
			roles.add(getHelper().createRole());
		}
		//
		return roles;
	}


	/**
	 * Return list of ids from list of identities
	 *
	 * @param identites
	 * @return
	 */
	protected Set<UUID> getIdFromList(List<? extends AbstractDto> identites) {
		return identites.stream().map(AbstractDto::getId).collect(Collectors.toSet());
	}
	
	/**
	 * Check result of bulk action
	 *
	 * @param processAction
	 * @param successCount
	 * @param failedCount
	 * @param warningCount
	 * @return
	 */
	protected IdmLongRunningTaskDto checkResultLrt(IdmBulkActionDto processAction, Long successCount, Long failedCount, Long warningCount) {
		assertNotNull(processAction.getLongRunningTaskId());
		//
		IdmLongRunningTaskFilter context = new IdmLongRunningTaskFilter();
		context.setIncludeItemCounts(true);
		//		
		IdmLongRunningTaskDto taskDto = longRunningTaskService.get(processAction.getLongRunningTaskId(), context);
		assertNotNull(taskDto);
		
		if (successCount != null) {
			assertEquals(successCount, taskDto.getSuccessItemCount());
		}
		
		if (failedCount != null) {
			assertEquals(failedCount, taskDto.getFailedItemCount());
		}

		if (warningCount != null) {
			assertEquals(warningCount, taskDto.getWarningItemCount());
		}

		return taskDto;
	}

	/**
	 * Check processed items in {@link IdmProcessedTaskItemService}
	 *
	 * @param processAction
	 * @param expected
	 */
	protected void checkProcessItemsCount(IdmBulkActionDto processAction, int expected) {
		assertNotNull(processAction.getLongRunningTaskId());
		IdmLongRunningTaskDto taskDto = longRunningTaskService.get(processAction.getLongRunningTaskId());
		assertEquals(expected, getItemsForLrt(taskDto).size());
	}
	
	/**
	 * Transform object to map
	 *
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> toMap(Object object) {
		Map<String, Object> convertValue = objectMapper.convertValue(object, Map.class);
		//
		Map<String, Object> result = new HashMap<>();
		for (Entry<String, Object> entry : convertValue.entrySet()) {
			if (entry.getValue() != null) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
}
