package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationAttachmentFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationAttachmentService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.core.notification.entity.IdmMessage_;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient_;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate_;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotification_;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Super class for all notification services
 * 
 * @author Radek Tomiška
 *
 * @param <DTO> {@link IdmNotificationDto} type
 * @param <E> {@link IdmNotification} type 
 * @param <F> {@link IdmNotificationFilter} type (mainly {@link IdmNotificationFilter} itself)
 */
public class AbstractNotificationLogService<DTO extends IdmNotificationDto, E extends IdmNotification, F extends IdmNotificationFilter>  
		extends AbstractReadWriteDtoService<DTO, E, F>
		implements AuthorizableService<IdmNotificationDto> {

	@Autowired private AttachmentManager attachmentManager;
	@Autowired private IdmNotificationAttachmentService notificationAttachmentService;
	
    public AbstractNotificationLogService(AbstractEntityRepository<E> repository) {
        super(repository);
    }

    @Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(NotificationGroupPermission.NOTIFICATION, null); // notifications doesn't support data security for now 
	}
    
    @Override
    @Transactional
    public void deleteInternal(DTO dto) {
    	Assert.notNull(dto, "Notification is required.");
    	UUID notificationId = dto.getId();
    	Assert.notNull(notificationId, "Notification identifier is required.");
    	//
		try {
			//
	    	// delete recipients is done by hiberante mapping - see IdmNotification
			//
			// delete notification attachments
			IdmNotificationAttachmentFilter notificationAttachmentFilter = new IdmNotificationAttachmentFilter();
			notificationAttachmentFilter.setNotification(notificationId);
			notificationAttachmentService
				.find(notificationAttachmentFilter, null)
				.forEach(notificationAttachmentService::delete);
			//
			// delete attachments - owned by notification only
			attachmentManager.deleteAttachments(dto);
	    	//
	    	// delete child notifications ... 
			F filter = getFilterClass().getDeclaredConstructor().newInstance();
			filter.setParent(notificationId);
	    	find(filter, null).getContent().forEach(this::delete);
	    	//
	    	super.deleteInternal(dto);
		} catch (ReflectiveOperationException ex) {
			throw new CoreException(
					String.format(
							"Service [%s] has wrong filter, fix implemented filter class [%s] (add default constructor).",
							this.getClass(), getFilterClass()),
					ex);
		}
    	
    }
    

    @Override
    protected E toEntity(DTO dto, E entity) {
    	Class<E> e = getEntityClass();
    	if (e.getSimpleName().equals(IdmNotification.class.getSimpleName())) {
			return super.toEntity(dto, entity);
		}
    	
        final E result = super.toEntity(dto, entity);
        if (result == null) {
        	return null;
        }
        result.getRecipients().forEach(r -> r.setNotification(result));
        return result;
    }

    /**
	 * We want to have recipients in returned lists.
	 */
	@Override
	protected List<DTO> toDtos(List<E> entities, boolean trimmed) {
		return super.toDtos(entities, false);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<E> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, F filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			Path<IdmMessage> message = root.get(IdmNotification_.message);
			predicates.add(builder.or(
					builder.like(builder.lower(message.get(IdmMessage_.subject)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(message.get(IdmMessage_.textMessage)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(message.get(IdmMessage_.htmlMessage)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmNotification_.topic)), "%" + filter.getText().toLowerCase() + "%")
					));
		}
		if (filter.getTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmNotification_.created), filter.getTill()));
		}
		if (filter.getFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmNotification_.created), filter.getFrom()));
		}
		if (filter.getSender() != null) {
			predicates.add(builder.equal(root.get(IdmNotification_.identitySender).get(IdmIdentity_.username), filter.getSender()));
		}
		if (filter.getRecipient() != null) {
			Subquery<IdmNotificationRecipient> subquery = query.subquery(IdmNotificationRecipient.class);
			Root<IdmNotificationRecipient> subRoot = subquery.from(IdmNotificationRecipient.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmNotificationRecipient_.notification), root), // correlation attr
                    		builder.equal(subRoot.get(IdmNotificationRecipient_.identityRecipient).get(IdmIdentity_.username), filter.getRecipient())
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		if (filter.getState() != null) {
			Subquery<IdmNotification> sent = query.subquery(IdmNotification.class);
			Root<IdmNotification> sentRoot = sent.from(IdmNotification.class);
			sent.select(sentRoot);
			sent.where(
                    builder.and(
                    		builder.equal(sentRoot.get(IdmNotification_.parent), root), // correlation attr
                    		builder.isNotNull(sentRoot.get(IdmNotification_.sent))
                    		)
            );
			if (filter.getState() == NotificationState.NOT) {
				predicates.add(builder.not(builder.exists(sent)));
			} else {
				Subquery<IdmNotification> notSent = query.subquery(IdmNotification.class);
				Root<IdmNotification> notSentRoot = notSent.from(IdmNotification.class);
				notSent.select(notSentRoot);
				notSent.where(
	                    builder.and(
	                    		builder.equal(notSentRoot.get(IdmNotification_.parent), root), // correlation attr
	                    		builder.isNull(notSentRoot.get(IdmNotification_.sent))
	                    		)
	            );
				if (filter.getState() == NotificationState.PARTLY) {
					predicates.add(builder.and(builder.exists(notSent), builder.exists(sent)));
				} else { // all
					predicates.add(builder.and(builder.not(builder.exists(notSent)), builder.exists(sent)));
				}
			}		
		}
		if (filter.getNotificationType() != null)  {
			predicates.add(builder.equal(root.type(), builder.literal(filter.getNotificationType())));
		}
		if (filter.getParent() != null) {
			predicates.add(builder.equal(root.get(IdmNotification_.parent).get(IdmNotification_.id), filter.getParent()));
		}
		if (filter.getSent() != null) {
			if (filter.getSent()) {
				predicates.add(builder.isNotNull(root.get(IdmNotification_.sent)));
			} else {
				predicates.add(builder.isNull(root.get(IdmNotification_.sent)));
			}
		}
		String topic = filter.getTopic();
		if (StringUtils.isNotEmpty(topic)) {
			predicates.add(builder.equal(root.get(IdmNotification_.topic), topic));
		}
		UUID templateId = filter.getTemplateId();
		if (templateId != null) {
			Path<IdmMessage> message = root.get(IdmNotification_.message);
			predicates.add(builder.equal(message.get(IdmMessage_.template).get(IdmNotificationTemplate_.id), templateId));
		}
		UUID identitySenderId = filter.getIdentitySender();
		if(identitySenderId != null) {
			predicates.add(builder.equal(root.get(IdmNotification_.identitySender).get(IdmIdentity_.id), identitySenderId));
		}
		return predicates;
	}
}
