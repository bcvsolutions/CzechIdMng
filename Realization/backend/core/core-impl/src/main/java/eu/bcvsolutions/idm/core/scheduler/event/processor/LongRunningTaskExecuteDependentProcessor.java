package eu.bcvsolutions.idm.core.scheduler.event.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.LongRunningTaskProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.event.LongRunningTaskEvent.LongRunningTaskEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmDependentTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmDependentTaskTriggerRepository;

/**
 * Execute scheduled long running tasks, which depends on currently ended scheduled task.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Execute scheduled long running tasks, which depends on currently ended scheduled task.")
public class LongRunningTaskExecuteDependentProcessor 
		extends CoreEventProcessor<IdmLongRunningTaskDto>
		implements LongRunningTaskProcessor {

	public static final String PROCESSOR_NAME = "long-running-task-execute-dependent-processor";
	//
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LongRunningTaskExecuteDependentProcessor.class);
	//
	@Autowired private IdmScheduledTaskService service;
	@Autowired private IdmDependentTaskTriggerRepository dependentTaskTriggerRepository; 
	@Autowired private SchedulerManager schedulerManager;

	public LongRunningTaskExecuteDependentProcessor() {
		super(LongRunningTaskEventType.END);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmLongRunningTaskDto> process(EntityEvent<IdmLongRunningTaskDto> event) {
		IdmLongRunningTaskDto longRunningTask = event.getContent();
		IdmScheduledTaskDto scheduledTask = service.findByLongRunningTaskId(longRunningTask.getId());
		if (scheduledTask == null) {
			LOG.debug("Execute dependent tasks is supported for scheduled tasks. LRT [{}] does not have scheduled task.", longRunningTask.getId());
			return new DefaultEventResult.Builder<IdmLongRunningTaskDto>(event, this)
					.setResult(new OperationResult(OperationState.NOT_EXECUTED))
					.build();
		}
		List<IdmDependentTaskTrigger> dependentTasks = dependentTaskTriggerRepository.findByInitiatorTaskId(scheduledTask.getQuartzTaskName());
		if (dependentTasks.isEmpty()) {
			LOG.trace("Task [{}] has not dependent tasks.", longRunningTask.getId());
			return new DefaultEventResult.Builder<IdmLongRunningTaskDto>(event, this)
					.setResult(new OperationResult(OperationState.NOT_EXECUTED))
					.build();
		}
		//
		if (longRunningTask.getResultState() != OperationState.EXECUTED) {
			LOG.debug("Task [{}] was not successfully executed. Dependent tasks [{}] will not be executed.", longRunningTask.getId(), dependentTasks.size());
			return new DefaultEventResult.Builder<IdmLongRunningTaskDto>(event, this)
					.setResult(new OperationResult(OperationState.NOT_EXECUTED))
					.build();
		}
		// find all triggers by quartz task name = job detail name. Default group is supported now only
		dependentTaskTriggerRepository
			.findByInitiatorTaskId(scheduledTask.getQuartzTaskName())
			.forEach(dependentTaskTrigger -> {
				LOG.info("Scheduled task [{}] ended. Dependent task [{}] will be executed in dryRun [{}].", 
						dependentTaskTrigger.getInitiatorTaskId(), 
						dependentTaskTrigger.getDependentTaskId(),
						longRunningTask.isDryRun());
				
				Task task = schedulerManager.getTask(dependentTaskTrigger.getDependentTaskId());
				if (task == null) {
					// Just for sure - quartz tasks can be persisted in different storage and can be reconfigured.
					// Check quartz.properties if you see this line in log.
					LOG.warn("Task [{}] was deleted and will not be executed.", dependentTaskTrigger.getDependentTaskId());
				} else {
					// new transaction - when some exception is thrown and properly catched in previous task,
					// but task end ok (just some item fails), then dependent task should be started (prevent to rollback).
					schedulerManager.runTaskNewTransactional(task.getId(), longRunningTask.isDryRun());
				}
			});

		return new DefaultEventResult.Builder<IdmLongRunningTaskDto>(event, this)
				.setResult(new OperationResult(OperationState.EXECUTED))
				.build();
	}

	/**
	 * after task ends
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
}
