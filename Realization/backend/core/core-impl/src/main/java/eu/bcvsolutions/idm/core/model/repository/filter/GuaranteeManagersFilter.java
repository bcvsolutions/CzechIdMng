package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee;
import eu.bcvsolutions.idm.core.model.entity.IdmContractGuarantee_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Managers criteria builder:
 * - by guarantee only
 * - only "valid" identity can be manager
 * - only valid or valid in future contracts can have managers
 * - additional filter parameter - IdmIdentityFilter.PARAMETER_VALID_CONTRACT_MANAGERS
 * 
 * @author Radek Tomiška
 *
 */
@Component("guaranteeManagersFilter")
@Description("Filter for find managers for given identity. "
		+ "Supports managers by guarantee only. "
		+ "Only valid identity can be manager.")
public class GuaranteeManagersFilter 
		extends AbstractFilterBuilder<IdmIdentity, IdmIdentityFilter> {
	
	@Override
	public String getName() {
		return IdmIdentityFilter.PARAMETER_MANAGERS_FOR;
	}
	
	@Autowired
	public GuaranteeManagersFilter(IdmIdentityRepository repository) {
		super(repository);
	}
	
	/**
	 * Predicate for manager as guarantee configured manually
	 * 
	 * @param root
	 * @param query
	 * @param builder
	 * @param filter
	 * @return
	 */
	public Predicate getGuaranteesPredicate(Root<IdmIdentity> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		// manager as guarantee
		Subquery<IdmIdentityContract> subqueryGuarantee = query.subquery(IdmIdentityContract.class);
		Root<IdmContractGuarantee> subRootGuarantee = subqueryGuarantee.from(IdmContractGuarantee.class);
		Path<IdmIdentityContract> pathIc = subRootGuarantee.get(IdmContractGuarantee_.identityContract);
		subqueryGuarantee.select(pathIc);
		//
		subqueryGuarantee.where(
              builder.and(
            		  getValidNowOrInFuturePredicate(pathIc, builder, filter),
            		  builder.equal(pathIc.get(IdmIdentityContract_.identity).get(IdmIdentity_.id), filter.getManagersFor()),
            		  builder.equal(subRootGuarantee.get(IdmContractGuarantee_.guarantee), root),
            		  filter.getManagersByContract() != null // concrete contract id only
            		  	? builder.equal(pathIc.get(IdmIdentityContract_.id), filter.getManagersByContract())
            		  	: builder.conjunction()
              		));
		return builder.exists(subqueryGuarantee);
	}
	
	/**
	 * Predicate for valid or valid in future contracts - by given filter.
	 * 
	 * @return predicate - never return null (conjunction, if filter is not set).
	 * @since 10.3.0
	 */
	public Predicate getValidNowOrInFuturePredicate(Path<IdmIdentityContract> pathIc, CriteriaBuilder builder, IdmIdentityFilter filter) {
		Boolean validContractManagers = filter.getValidContractManagers();
		// not set => nullable filter
		if (validContractManagers == null) {
			return builder.conjunction();
		}
		// valid
		Predicate validContractManagersPredicate = RepositoryUtils.getValidNowOrInFuturePredicate(pathIc, builder);
		// invalid
		if (BooleanUtils.isFalse(validContractManagers)) {
			validContractManagersPredicate = builder.not(validContractManagersPredicate);
		}
		return validContractManagersPredicate;
	}

	@Override
	public Predicate getPredicate(Root<IdmIdentity> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		if (filter.getManagersFor() == null) {
			return null;
		}
		if (filter.getManagersByTreeType() != null || !filter.isIncludeGuarantees()) {
			// guarantees is not needed
			return builder.disjunction();
		}
		//
		Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
		Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
		subquery.select(subRoot);
		//
		subquery.where(builder.and(
				//
				// valid identity only
				builder.equal(root.get(IdmIdentity_.disabled), Boolean.FALSE),
				//
        		// valid contract only
				RepositoryUtils.getValidNowOrInFuturePredicate(subRoot, builder),
				//
        		// not disabled, not excluded contract
        		builder.equal(subRoot.get(IdmIdentityContract_.disabled), Boolean.FALSE),
        		builder.or(
        				builder.notEqual(subRoot.get(IdmIdentityContract_.state), ContractState.EXCLUDED),
        				builder.isNull(subRoot.get(IdmIdentityContract_.state))
        		),
        		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
        		getGuaranteesPredicate(root, query, builder, filter)
        		));
		//
		return builder.exists(subquery);
	}
	
	@Override
	public int getOrder() {
		return 5;
	}
}
