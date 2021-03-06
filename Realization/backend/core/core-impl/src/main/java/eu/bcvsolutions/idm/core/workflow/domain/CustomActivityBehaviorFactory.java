package eu.bcvsolutions.idm.core.workflow.domain;

import java.util.List;

import org.activiti.bpmn.model.FieldExtension;
import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.service.EmailNotificationSender;

/**
 * Sending emails from activity through our emailer.
 * 
 * @author svandav
 *
 */
public class CustomActivityBehaviorFactory extends DefaultActivityBehaviorFactory {
	
	@Autowired private EmailNotificationSender emailService;
	@Autowired private IdmIdentityService identityService;

	@Override
	protected MailActivityBehavior createMailActivityBehavior(String taskId, List<FieldExtension> fields) {
		List<FieldDeclaration> fieldDeclarations = createFieldDeclarations(fields);
		CustomMailActivityBehavior customMailActivityBehavior = (CustomMailActivityBehavior) 
				ClassDelegate.defaultInstantiateDelegate(CustomMailActivityBehavior.class, fieldDeclarations);
		customMailActivityBehavior.setEmailService(emailService);
		customMailActivityBehavior.setIdentityService(identityService);
		return customMailActivityBehavior;
	}
	
}
