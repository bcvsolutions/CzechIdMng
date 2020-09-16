package eu.bcvsolutions.idm.core.notification.service.impl;

import java.time.ZonedDateTime;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;

/**
 * Sends notifications.
 * 
 * @author Radek Tomiška
 *
 */
@Component("notificationManager")
public class DefaultNotificationManager extends AbstractNotificationSender<IdmNotificationLogDto>
		implements NotificationManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultNotificationManager.class);
	private final IdmNotificationLogService notificationLogService;
	private final ProducerTemplate producerTemplate;

	@Autowired
	public DefaultNotificationManager(IdmNotificationLogService notificationLogService,
			ProducerTemplate producerTemplate) {
		Assert.notNull(notificationLogService, "Service is required.");
		Assert.notNull(producerTemplate, "Producer template is required.");
		//
		this.notificationLogService = notificationLogService;
		this.producerTemplate = producerTemplate;
	}

	@Override
	public String getType() {
		return IdmNotificationLog.NOTIFICATION_TYPE;
	}

	@Override
	public Class<? extends BaseEntity> getNotificationType() {
		return notificationLogService.getEntityClass();
	}

	@Override
	@Transactional
	public IdmNotificationLogDto send(IdmNotificationDto notification) {
		Assert.notNull(notification, "Notification is required!");
		//
		IdmNotificationLogDto notificationLog = createLog(notification);
		return sendNotificationLog(notificationLog);
	}

	/**
	 * Sends existing notification to routing
	 * 
	 * @param notificationLog
	 * @return
	 */
	private IdmNotificationLogDto sendNotificationLog(IdmNotificationLogDto notificationLog) {
		LOG.info("Sending notification [{}]", notificationLog);
		// send notification to routing
		producerTemplate.sendBody("direct:notifications", notificationLog);
		return notificationLog;
	}

	/**
	 * Persists new notification record from given notification. 
	 * Input notification type is leaved unchanged - is needed for another processing (routing).
	 * 
	 * @param notification
	 * @return
	 */
	private IdmNotificationLogDto createLog(IdmNotificationDto notification) {
		Assert.notNull(notification, "Notification is required.");
		Assert.notNull(notification.getMessage(), "Message is required.");
		// we can only create log, if notification is instance of
		// IdmNotificationLog
		if (notification instanceof IdmNotificationLogDto) {
			notification.setSent(ZonedDateTime.now());
			IdmNotificationLogDto notificationLog = notificationLogService.save((IdmNotificationLogDto) notification);
			notificationLog.setType(notification.getType()); // set previous type - is needed for choose correct notification sender
			notificationLog.setAttachments(saveNotificationAttachments(notificationLog, notification.getAttachments()));
			//
			return notificationLog;
		}
		// we need to clone notification
		IdmNotificationLogDto notificationLog = new IdmNotificationLogDto();
		notificationLog.setType(notification.getType());
		notificationLog.setSent(ZonedDateTime.now());
		// clone message
		notificationLog.setMessage(cloneMessage(notification));
		// clone recipients
		for (IdmNotificationRecipientDto recipient : notification.getRecipients()) {
			notificationLog.getRecipients()
					.add(cloneRecipient(notificationLog, recipient, recipient.getRealRecipient()));
		}
		notificationLog.setIdentitySender(notification.getIdentitySender());
		notificationLog = notificationLogService.save(notificationLog);
		notificationLog.setType(notification.getType()); // set previous type - is needed for choose correct notification sender
		notificationLog.setAttachments(saveNotificationAttachments(notificationLog, notification.getAttachments()));
		//
		return notificationLog;
	}

}
