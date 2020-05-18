package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;


/**
 * Filter for {@link IdmRoleGuaranteeRoleDto} - roles
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
public class IdmRoleGuaranteeRoleFilter extends DataFilter implements ExternalIdentifiableFilter {
	/**
	 * guarantee as role
	 */
	public static final String PARAMETER_GUARANTEE_ROLE = "guaranteeRole";
	/**
	 * Guarantee type
	 */
	public static final String PARAMETER_GUARANTEE_TYPE = "guaranteetype";
	
	public IdmRoleGuaranteeRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleGuaranteeRoleFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleGuaranteeRoleDto.class, data);
	}
	
	public UUID getRole() {
		return DtoUtils.toUuid(data.getFirst(IdmRoleGuaranteeFilter.PARAMETER_ROLE));
	}
	
	public void setRole(UUID role) {
		data.set(IdmRoleGuaranteeFilter.PARAMETER_ROLE, role);
	}
	
	public UUID getGuaranteeRole() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_GUARANTEE_ROLE));
	}
	
	public void setGuaranteeRole(UUID guaranteeRole) {
		data.set(PARAMETER_GUARANTEE_ROLE, guaranteeRole);
	}
	
	public String getType() {
		return getParameterConverter().toString(getData(), PARAMETER_GUARANTEE_TYPE);
	}

	public void setType(String type) {
		set(PARAMETER_GUARANTEE_TYPE, type);
	}
}
