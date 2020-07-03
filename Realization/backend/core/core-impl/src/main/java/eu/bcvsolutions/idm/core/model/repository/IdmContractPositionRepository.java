package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition;

/**
 * Identity contract's other position.
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
public interface IdmContractPositionRepository extends AbstractEntityRepository<IdmContractPosition> {

	String FIND_BY_WORK_PROSITION_QUERY = "select e from IdmTreeNode wp, #{#entityName} e join e.workPosition n"
			+ " where"
			+ " wp.id = :workPositionId"
			+ " and"
			+ " n.treeType = wp.treeType" // more tree types
			+ " and"
			+ " ("
				+ " (n.id = wp.id)" // takes all recursion
				+ " or"
				+ " ("
					+ " ?#{[1] == null ? '' : #recursionType.name()} = 'DOWN'"
					+ " and n.forestIndex.lft between wp.forestIndex.lft and wp.forestIndex.rgt"
				+ " )"
				+ " or"
				+ " ("
					+ " ?#{[1] == null ? '' : #recursionType.name()} = 'UP'"
					+ " and wp.forestIndex.lft between n.forestIndex.lft and n.forestIndex.rgt"
				+ " )"
			+ " )";
	
	/**
	 * Contract positions with given tree node (by workPositionId) recursively (by recursionType).
	 * 
	 * @param workPositionId
	 * @param recursionType
	 * @return
	 * @see #findByWorkPosition(UUID, RecursionType, Pageable)
	 * @deprecated @since 10.4.0 use {@link IdmContractPositionFilter#PARAMETER_RECURSION_TYPE}
	 */
	@Query(value = FIND_BY_WORK_PROSITION_QUERY)
	List<IdmContractPosition> findAllByWorkPosition(
			@Param("workPositionId") UUID workPositionId, 
			@Param("recursionType") RecursionType recursionType);
}
