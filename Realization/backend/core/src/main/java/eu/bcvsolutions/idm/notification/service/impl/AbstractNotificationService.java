package eu.bcvsolutions.idm.notification.service.impl;

import java.util.List;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.notification.service.NotificationService;

public abstract class AbstractNotificationService implements NotificationService {
	
	public static final String DEFAULT_TOPIC = "default";

	@Override
	public boolean send(IdmMessage message, IdmIdentity recipient) {
		return send(DEFAULT_TOPIC, message, recipient);
	}
	
	@Override
	public boolean send(IdmMessage message, List<IdmIdentity> recipients) {
		return send(DEFAULT_TOPIC, message, recipients);
	}

	@Override
	public boolean send(String topic, IdmMessage message, IdmIdentity recipient) {
		return send(topic, message, Lists.newArrayList(recipient));
	}
	
	@Override
	public boolean send(String topic, IdmMessage message, List<IdmIdentity> recipients) {
		IdmNotificationLog notification = new IdmNotificationLog();
		notification.setTopic(topic);
		notification.setMessage(message);
		recipients.forEach(recipient -> {
			notification.getRecipients().add(new IdmNotificationRecipient(notification, recipient));
		});		
		return send(notification);
	}
	
	/**
	 * Clone notification message
	 * 
	 * @param notification
	 * @return
	 */
	protected IdmMessage cloneMessage(IdmNotification notification) {
		return new IdmMessage(notification.getMessage().getSubject(), notification.getMessage().getTextMessage(), notification.getMessage().getHtmlMessage());
	}
	
	/**
	 * Clone recipients
	 * 
	 * @param notification - recipients new parent
	 * @param recipient - source recipient
	 * @return
	 */
	protected IdmNotificationRecipient cloneRecipient(IdmNotification notification, IdmNotificationRecipient recipient) {
		return new IdmNotificationRecipient(notification, recipient.getIdentityRecipient(), recipient.getRealRecipient());
	}
}
