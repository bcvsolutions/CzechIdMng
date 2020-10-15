package eu.bcvsolutions.idm.core.notification.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationRecipient;

/**
 * Repository for notification recipients
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmNotificationRecipientRepository extends AbstractEntityRepository<IdmNotificationRecipient> {

	/**
	 * @deprecated @since 10.6.0 - use service filter
	 */
	@Deprecated
	@Query("select e from IdmNotificationRecipient e left join e.notification n where (n.id = ?#{[0].notification})")
	Page<IdmNotificationRecipient> find(IdmNotificationRecipientFilter filter, Pageable pageable);
	
	/**
	 * Clears identity id from all recipient (raw recipient remains)
	 * 
	 * @param identity
	 * @return
	 * @deprecated @since 10.4.0 @NotFound annotation is used on recipient now
	 */
	@Deprecated
	@Modifying
	@Query("update #{#entityName} e set e.identityRecipient = null where e.identityRecipient.id = :identityId")
	int clearIdentity(@Param("identityId") UUID identityId);
}
