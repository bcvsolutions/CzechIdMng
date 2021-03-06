package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormableService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.repository.IdmContractSliceRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Contract time slices administration
 * 
 * @author svandav
 *
 */
@Service("contractSliceService")
public class DefaultIdmContractSliceService 
		extends AbstractFormableService<IdmContractSliceDto, IdmContractSlice, IdmContractSliceFilter>
		implements IdmContractSliceService {
	
	@Autowired
	public DefaultIdmContractSliceService(
			IdmContractSliceRepository repository,
			FormService formService,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager, formService);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.CONTRACTSLICE, getEntityClass());
	}
	
	// !!! I want to remove disable from the slice ... confirm if is this OK
	@Override
	protected IdmContractSlice toEntity(IdmContractSliceDto dto, IdmContractSlice entity) {
		IdmContractSlice contract = super.toEntity(dto, entity);
		if (contract != null && dto != null) {
			contract.setDisabled(dto.isDisabled()); // redundant attribute for queries
		}
		return contract;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmContractSlice> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmContractSliceFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			Path<IdmTreeNode> wp = root.get(IdmContractSlice_.workPosition);
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(IdmContractSlice_.position)), "%" + filter.getText().toLowerCase() + "%"),
							builder.like(builder.lower(wp.get(IdmTreeNode_.name)), "%" + filter.getText().toLowerCase() + "%"),
							builder.like(builder.lower(wp.get(IdmTreeNode_.code)), "%" + filter.getText().toLowerCase() + "%")
							)
					);
		}
		if (filter.getTreeNode() != null) {
			predicates.add(builder.equal(root.get(IdmContractSlice_.workPosition).get(AbstractEntity_.id), filter.getTreeNode()));
		}
		if (filter.getIdentity() != null) {
			predicates.add(builder.equal(root.get(IdmContractSlice_.identity).get(AbstractEntity_.id), filter.getIdentity()));
		}
		if (filter.getValidTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmContractSlice_.validTill), filter.getValidTill()));
		}
		if (filter.getValidFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmContractSlice_.validFrom), filter.getValidFrom()));
		}
		if (filter.getExterne() != null) {
			predicates.add(builder.equal(root.get(IdmContractSlice_.externe), filter.getExterne()));
		}
		if (filter.getMain() != null) {
			predicates.add(builder.equal(root.get(IdmContractSlice_.main), filter.getMain()));
		}
		if (filter.getValid() != null) {
			if (filter.getValid()) {
				final LocalDate today = LocalDate.now();
				predicates.add(
						builder.and(
								builder.or(
										builder.lessThanOrEqualTo(root.get(IdmContractSlice_.validFrom), today),
										builder.isNull(root.get(IdmContractSlice_.validFrom))
										),
								builder.or(
										builder.greaterThanOrEqualTo(root.get(IdmContractSlice_.validTill), today),
										builder.isNull(root.get(IdmContractSlice_.validTill))
										),
								builder.equal(root.get(IdmContractSlice_.disabled), Boolean.FALSE)
								)								
						);
			} else {
				final LocalDate today = LocalDate.now();
				predicates.add(
						builder.or(
								builder.lessThan(root.get(IdmContractSlice_.validTill), today),
								builder.greaterThan(root.get(IdmContractSlice_.validFrom), today),
								builder.equal(root.get(IdmContractSlice_.disabled), Boolean.TRUE)
								)
						);
			}
		}
		if (filter.getValidNowOrInFuture() != null) {
			if (filter.getValidNowOrInFuture()) {
				predicates.add(
						builder.and(
								builder.or(
										builder.greaterThanOrEqualTo(root.get(IdmContractSlice_.validTill), LocalDate.now()),
										builder.isNull(root.get(IdmContractSlice_.validTill))
										),
								builder.equal(root.get(IdmContractSlice_.disabled), Boolean.FALSE)
							));
			} else {
				predicates.add(builder.lessThan(root.get(IdmContractSlice_.validTill), LocalDate.now()));
			}
		}
		if (filter.getState() != null) {
			predicates.add(builder.equal(root.get(IdmContractSlice_.state), filter.getState()));
		}
		if (filter.getExcludeContract() != null) {
			predicates.add(builder.notEqual(root.get(IdmContractSlice_.id), filter.getExcludeContract()));
		}
		if (filter.getParentContract() != null) {
			predicates.add(builder.equal(root.get(IdmContractSlice_.parentContract).get(IdmIdentityContract_.id), filter.getParentContract()));
		}
		if (filter.getContractCode() != null) {
			predicates.add(builder.equal(root.get(IdmContractSlice_.contractCode), filter.getContractCode()));
		}
		if (filter.getUsingAsContract() != null) {
			predicates.add(builder.equal(root.get(IdmContractSlice_.usingAsContract), filter.getUsingAsContract()));
		}
		if (filter.getShouldBeUsingAsContract() != null) {
			if (filter.getShouldBeUsingAsContract()) {
				final LocalDate today = LocalDate.now();
				predicates.add(
						builder.and(
								builder.or(
										builder.lessThanOrEqualTo(root.get(IdmContractSlice_.validFrom), today),
										builder.isNull(root.get(IdmContractSlice_.validFrom))
										),
								builder.or(
										builder.greaterThanOrEqualTo(root.get(IdmContractSlice_.validTill), today),
										builder.isNull(root.get(IdmContractSlice_.validTill))
										)
								)								
						);
			}else {
				final LocalDate today = LocalDate.now();
				predicates.add(
						builder.or(
								builder.greaterThan(root.get(IdmContractSlice_.validFrom), today),
								builder.lessThan(root.get(IdmContractSlice_.validTill), today)
								)
						);
			}
		}
		//
		return predicates;
	}

}
