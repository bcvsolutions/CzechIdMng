package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Form definition service - CRUD.
 * Use {@link FormService} instead in your modules.
 * 
 * @author Radek Tomiška
 * @see FormService
 */
public interface IdmFormDefinitionService extends 
		EventableDtoService<IdmFormDefinitionDto, IdmFormDefinitionFilter>,
		AuthorizableService<IdmFormDefinitionDto>,
		ScriptEnabled {

	/**
	 * Default definition name for type (if no name is given)
	 */
	String DEFAULT_DEFINITION_CODE = "default";
	
	/**
	 * Returns form definition by given type and code (unique).
	 * 
	 * @param type required
	 * @param code [optional] if code is {@code null}, then "default" code for given type is used.
	 * @return
	 */
	IdmFormDefinitionDto findOneByTypeAndCode(String type, String code);
	
	/**
	 * Returns main definition for given type (unique).
	 * 
	 * @param type required
	 * @return
	 */
	IdmFormDefinitionDto findOneByMain(String type);
	
	/**
	 * Returns all definitions by given type
	 * 
	 * @param type required
	 * @return
	 */
	List<IdmFormDefinitionDto> findAllByType(String type);
	
	/**
	 * Returns true, when given owner type support eav forms. If {@link AbstractDto} owner type is given, 
	 * then underlying {@link AbstractEntity} is resolved automatically => {@link AbstractEntity} has 
	 * to implement  {@link FormableEntity}.
	 * 
	 * @param ownerType
	 * @return
	 * @since 7.6.0
	 */
	boolean isFormable(Class<? extends Identifiable> ownerType);
	
	/**
	 * Returns owner type - owner type has to be entity class - dto class can be given.
	 * Its used as default definition type for given owner type.
	 * 
	 * @param owner
	 * @return
	 * @since 7.6.0
	 */
	String getOwnerType(Identifiable owner);
	
	
	/**
	 * Returns owner type - owner type has to be entity class - dto class can be given.
	 * Its used as default definition type for given owner type.
	 * 
	 * @param ownerType
	 * @return
	 * @since 7.6.0
	 */
	String getOwnerType(Class<? extends Identifiable> ownerType);
	
	/**
	 * Returns {@link FormableEntity}. Owner type has to be entity class - dto class can be given.
	 * 
	 * @param ownerType
	 * @return
	 * @since 7.6.0
	 */
	Class<? extends FormableEntity> getFormableOwnerType(Class<? extends Identifiable> ownerType);
	
	/**
	 * Creates / updates definition with given attributes.
	 * Update attributes in definition automatically except change persistent type and confidential. 
	 * This incompatible changes have to be solved externally (e.g. by change script). 
	 * Adding parameter is compatible change.
	 * Removing attribute is ignored now - attributes remain in definition a can be removed e.g. by change script.
	 * 
	 * @param ownerType
	 * @param definitionCode
	 * @param attributes
	 * @return
	 * @since 7.6.0
	 */
	IdmFormDefinitionDto updateDefinition(Class<? extends Identifiable> ownerType, String definitionCode, List<IdmFormAttributeDto> attributes);
	
	/**
	 * Creates / updates definition with given attributes.
	 * Update attributes in definition automatically except change persistent type and confidential. 
	 * This incompatible changes have to be solved externally (e.g. by change script). 
	 * Adding parameter is compatible change.
	 * Removing attribute is ignored now - attributes remain in definition a can be removed e.g. by change script.
	 * 
	 * @param definitionType
	 * @param definitionCode
	 * @param attributes
	 * @return
	 * @since 7.7.0
	 */
	IdmFormDefinitionDto updateDefinition(String definitionType, String definitionCode, List<IdmFormAttributeDto> attributes);
	
	/**
	 * Creates / updates definition with given attributes.
	 * Update attributes in definition automatically except change persistent type and confidential. 
	 * This incompatible changes have to be solved externally (e.g. by change script). 
	 * Adding parameter is compatible change.
	 * Removing attribute is ignored now - attributes remain in definition a can be removed e.g. by change script.
	 * 
	 * @param definition type and code has to be given
	 * @param attributes
	 * @return
	 * @since 9.7.0
	 */
	IdmFormDefinitionDto updateDefinition(IdmFormDefinitionDto definition);

	/**
	 * Export only definition without attributes.
	 * 
	 * @param id
	 * @param batch
	 */
	void exportOnlyDefinition(UUID id, IdmExportImportDto batch);
}
