package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service layer for long running tasks.
 * 
 * @author Radek Tomiška
 * @author Jan Helbich
 *
 */
public interface IdmLongRunningTaskService extends
	EventableDtoService<IdmLongRunningTaskDto, IdmLongRunningTaskFilter>,
	AuthorizableService<IdmLongRunningTaskDto> {

	/**
	 * Returns tasks for given instance id (server) and state
	 * 
	 * @param instanceId - server id 
	 * @param state
	 * @return
	 * 
	 * @see ConfigurationService
	 */
	List<IdmLongRunningTaskDto> findAllByInstance(String instanceId, OperationState state);
	
	/**
	 * Updates long running task attributes
	 * 
	 * @param id long running task ID
	 * @param count
	 * @param counter
	 */
	void updateState(UUID id, Long count, Long counter);

	/**
	 * Create LRT by given scheduled task and executor.
	 * 
	 * @param scheduledTask
	 * @param taskExecutor
	 * @param instanceId
	 * @return
	 * @deprecated @since 10.4.0 use {@link LongRunningTaskManager#saveLongRunningTask(LongRunningTaskExecutor, UUID, OperationState)}
	 */
	@Deprecated
	IdmLongRunningTaskDto create(
			IdmScheduledTaskDto scheduledTask, 
			SchedulableTaskExecutor<?> taskExecutor,
			String instanceId);
}
