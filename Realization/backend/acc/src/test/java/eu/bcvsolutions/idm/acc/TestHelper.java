package eu.bcvsolutions.idm.acc;

import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Reuses core TestHelper and adds acc spec. methods
 * 
 * @author Radek Tomiška
 *
 */
public interface TestHelper extends eu.bcvsolutions.idm.test.api.TestHelper {
	
	static final String ATTRIBUTE_MAPPING_NAME = "__NAME__";
	static final String ATTRIBUTE_MAPPING_ENABLE = "__ENABLE__";
	static final String ATTRIBUTE_MAPPING_PASSWORD = IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME;
	static final String ATTRIBUTE_MAPPING_FIRSTNAME = "FIRSTNAME";
	static final String ATTRIBUTE_MAPPING_LASTNAME = "LASTNAME";
	static final String ATTRIBUTE_MAPPING_EMAIL = "EMAIL";
	
	/**
	 * Prepares conntector and system for fiven table name.
	 * Test database is used. 
	 * Generated system name will be used.
	 * 
	 * @param tableName see {@link TestResource#TABLE_NAME}
	 * @return
	 */
	SysSystemDto createSystem(String tableName);
	
	/**
	 * Prepares conntector and system for fiven table name.
	 * Test database is used. 
	 * 
	 * @param tableName see {@link TestResource#TABLE_NAME}
	 * @param systemName
	 * @return
	 */
	SysSystemDto createSystem(String tableName, String systemName);
	
	/**
	 * Creates system for {@link TestResource} with schema generated by given table.
	 * Test database is used.
	 * Generated system name will be used.
	 * 
	 * @see TestResource#TABLE_NAME
	 * @param withMapping default mapping will be included
	 * @return
	 */
	SysSystemDto createTestResourceSystem(boolean withMapping);
	
	/**
	 * Creates system for {@link TestResource} with schema generated by given table.
	 * Test database is used.
	 * 
	 * see TestResource#TABLE_NAME
	 * @param withMapping default mapping will be included
	 * @param systemName
	 * @return
	 */
	SysSystemDto createTestResourceSystem(boolean withMapping, String systemName);
	
	/**
	 * Returns default mapping - provisioning, identity
	 * 
	 * @see #createSystem(String, boolean)
	 * @param system
	 * @return
	 */
	SysSystemMappingDto getDefaultMapping(SysSystemDto system);
	
	/**
	 * Assing system to given role with default mapping (provisioning, identity)
	 * 
	 * @see #getDefaultMapping(SysSystem)
	 * @param role
	 * @param system
	 * @return
	 */
	SysRoleSystemDto createRoleSystem(IdmRoleDto role, SysSystemDto system);
	
	/**
	 * Find account on target system
	 * 
	 * @param uid
	 * @return
	 */
	TestResource findResource(String uid);
	
	/**
	 * Creates system entity (IDENTITY) with random name on given system
	 * 
	 * @param system
	 * @return
	 */
	SysSystemEntityDto createSystemEntity(SysSystemDto system);
}
