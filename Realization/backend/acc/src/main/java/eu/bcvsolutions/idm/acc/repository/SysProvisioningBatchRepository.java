package eu.bcvsolutions.idm.acc.repository;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning operation batches. Any operation has request and batch.
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningBatchRepository extends AbstractEntityRepository<SysProvisioningBatch> {
	
	/**
	 * Finds batch for given operation.
	 * 
	 * @param operation
	 * @return
	 * @deprecated use {@link #findBatch(UUID, UUID, String)}
	 */
	@Deprecated
	@Query(value = "select distinct(o.batch) from SysProvisioningOperation o"
			+ " where"
			+ " o.system = ?#{[0].system}"
			+ " and"
			+ " (o.entityIdentifier = ?#{[0].entityIdentifier} or ?#{[0].entityIdentifier} is null)"
			+ " and"
			+ " o.systemEntityUid = ?#{[0].systemEntityUid}")
	SysProvisioningBatch findBatch(SysProvisioningOperation operation);
	
	/**
	 * Finds batch for given operation.
	 * 
	 * @param systemId
	 * @param entityIdentifier
	 * @param systemEntityUid
	 * @return
	 */
	@Query(value = "select distinct(o.batch) from SysProvisioningOperation o"
			+ " where"
			+ " o.system.id = :systemId"
			+ " and"
			+ " (o.entityIdentifier = :entityIdentifier or :entityIdentifier is null)"
			+ " and"
			+ " o.systemEntityUid = :systemEntityUid")
	SysProvisioningBatch findBatch(
			@Param("systemId") UUID systemId,
			@Param("entityIdentifier") UUID entityIdentifier, 
			@Param("systemEntityUid") String systemEntityUid);
	
	/**
	 * Returns batches by their request's state
	 * 
	 * @param state
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e where exists (select o.id from SysProvisioningOperation o"
			+ " where"
			+ " o.batch = e"
			+ " and"
			+ " o.result.state = :state)")
	Page<SysProvisioningBatch> findByOperationState(@Param("state") OperationState state, Pageable pageable);
	
	/**
	 * Returns batches by their system is virtual and request's state
	 * 
	 * @param state
	 * @param pageable
	 * @return
	 */
	@Query(value = "select e from #{#entityName} e where exists (select o.id from SysProvisioningOperation o"
			+ " where"
			+ " o.batch = e"
			+ " and"
			+ " o.system.virtual = :virtualSystem"
			+ " and"
			+ " o.result.state = :state)")
	Page<SysProvisioningBatch>  findByVirtualSystemAndOperationState(@Param("virtualSystem") Boolean virtualSystem, @Param("state") OperationState state, Pageable pageable);
	
	/**
	 * Returns unprocessed planned batches
	 * 
	 * @param date
	 * @param pageable
	 * @return
	 */
	Page<SysProvisioningBatch> findByNextAttemptLessThanEqual(@Param("nextAttempt") DateTime date, Pageable pageable);
}
