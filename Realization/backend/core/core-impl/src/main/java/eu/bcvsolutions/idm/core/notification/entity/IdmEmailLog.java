package eu.bcvsolutions.idm.core.notification.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Notification log for email.
 * 
 * @author Radek Tomiška
 */
@Entity
@Table(name = "idm_notification_email")
public class IdmEmailLog extends IdmNotificationLog  {
	
	private static final long serialVersionUID = -6492542811469689133L;
	public static final String NOTIFICATION_TYPE = "email";
	
	@Override
	public String getType() {
		return NOTIFICATION_TYPE;
	}
}
