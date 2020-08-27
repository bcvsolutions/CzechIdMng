package eu.bcvsolutions.idm.acc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem_;
import eu.bcvsolutions.idm.acc.repository.AccUniformPasswordSystemRepository;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;

/**
 * {@link AccUniformPasswordSystemDto} filter by {@link SysSystemDto} id
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Uniform password system filter - by system id (equals)")
public class AccUniformPasswordSystemSystemIdFilter extends AbstractFilterBuilder<AccUniformPasswordSystem, AccUniformPasswordSystemFilter> {
	
	@Autowired
	public AccUniformPasswordSystemSystemIdFilter(AccUniformPasswordSystemRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return AccUniformPasswordSystemFilter.PARAMETER_SYSTEM_ID;
	}
	
	@Override
	public Predicate getPredicate(Root<AccUniformPasswordSystem> root, AbstractQuery<?> query, CriteriaBuilder builder, AccUniformPasswordSystemFilter filter) {
		if (filter.getSystemId() == null) {
			return null;
		}

		return builder.equal(root.get(AccUniformPasswordSystem_.system).get(AbstractEntity_.id), filter.getSystemId());
	}	
}