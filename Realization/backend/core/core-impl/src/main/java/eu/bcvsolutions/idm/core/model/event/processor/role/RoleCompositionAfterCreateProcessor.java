package eu.bcvsolutions.idm.core.model.event.processor.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleCompositionProcessor;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.RoleCompositionEvent.RoleCompositionEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.AddNewRoleCompositionTaskExecutor;

/**
 * Assign sub roles for currently assigned roles, after composition is created.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Component(RoleCompositionAfterCreateProcessor.PROCESSOR_NAME)
@Description("Assign sub roles for currently assigned roles, after composition is created.")
public class RoleCompositionAfterCreateProcessor
		extends CoreEventProcessor<IdmRoleCompositionDto> 
		implements RoleCompositionProcessor {
	
	public static final String PROCESSOR_NAME = "core-role-composition-after-create-processor";
	//
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	public RoleCompositionAfterCreateProcessor() {
		super(RoleCompositionEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleCompositionDto> process(EntityEvent<IdmRoleCompositionDto> event) {
		IdmRoleCompositionDto roleComposition = event.getContent();
		Assert.notNull(roleComposition.getId(), "Composition identifier is required.");
		Assert.notNull(roleComposition.getSub(), "Composition sub role is required."); // just for sure
		//
		AddNewRoleCompositionTaskExecutor addRoleCompositionTask = AutowireHelper.createBean(AddNewRoleCompositionTaskExecutor.class);
		addRoleCompositionTask.setRoleCompositionId(roleComposition.getId());
		try {
			if (event.getPriority() == PriorityType.IMMEDIATE) {
				longRunningTaskManager.executeSync(addRoleCompositionTask);
			} else {
				longRunningTaskManager.execute(addRoleCompositionTask);
			}
		} catch (AcceptedException ex) {
			DefaultEventResult<IdmRoleCompositionDto> result = new DefaultEventResult<>(event, this);
			result.setSuspended(true);
			//
			return result;
		}
		//
		return new DefaultEventResult<>(event, this);
	}
}
