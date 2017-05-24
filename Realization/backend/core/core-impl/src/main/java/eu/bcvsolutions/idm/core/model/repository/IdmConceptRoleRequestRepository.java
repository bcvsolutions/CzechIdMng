package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.ConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;

/**
 * Repository for role request
 * @author svandav
 *
 */
public interface IdmConceptRoleRequestRepository extends AbstractEntityRepository<IdmConceptRoleRequest, ConceptRoleRequestFilter> {
	
	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from IdmConceptRoleRequest e" +
	        " where " +
	        " (?#{[0].roleRequestId} is null or e.roleRequest.id = ?#{[0].roleRequestId})" +
	        " and" +
	        " (?#{[0].identityRoleId} is null or e.identityRole.id = ?#{[0].identityRoleId})" +
	        " and" +
	        " (?#{[0].roleId} is null or e.role.id = ?#{[0].roleId})" +
	        " and" +
	        " (?#{[0].identityContractId} is null or e.identityContract.id = ?#{[0].identityContractId})" +
	        " and" +
	        " (?#{[0].roleTreeNodeId} is null or e.roleTreeNode.id = ?#{[0].roleTreeNodeId})" +
	        " and" +
	        " (?#{[0].operation} is null or e.operation = ?#{[0].operation})" +
	        " and" +
	        " (?#{[0].state} is null or e.state = ?#{[0].state})")
	Page<IdmConceptRoleRequest> find(ConceptRoleRequestFilter filter, Pageable pageable);
	
	/**
	 * Finds all concepts for this request
	 * 
	 * @param roleRequestId
	 * @return
	 */
	List<IdmConceptRoleRequest> findAllByRoleRequest_Id(@Param("roleRequestId") UUID roleRequestId);

}
