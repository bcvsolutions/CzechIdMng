package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Form attributes definition - CRUD.
 * Use {@link FormService} instead in your modules.
 * 
 * @author Radek Tomiška
 * @see FormService
 */
public interface IdmFormAttributeService extends 
		EventableDtoService<IdmFormAttributeDto, IdmFormAttributeFilter>,
		AuthorizableService<IdmFormAttributeDto> {
	
	/**
	 * Finds one attribute from given definition by given attribute name
	 * 
	 * @param definitionType
	 * @param definitionCode
	 * @param attributeCode
	 * @return
	 */
	IdmFormAttributeDto findAttribute(String definitionType, String definitionCode, String attributeCode, BasePermission... permission);

}
