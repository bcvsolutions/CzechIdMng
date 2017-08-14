package eu.bcvsolutions.idm.ic.czechidm.domain;

import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.annotation.IcConfigurationClassProperty;
import eu.bcvsolutions.idm.ic.impl.IcConfigurationPropertyImpl;

/**
 * Convert utility for CzechIdM implementation
 * 
 * @author svandav
 *
 */
public class CzechIdMIcConvertUtil {

	public static IcConfigurationProperty convertConfigurationProperty(IcConfigurationClassProperty property) {
		if(property == null){
			return null;
		}
		IcConfigurationPropertyImpl icProperty = new IcConfigurationPropertyImpl();
		icProperty.setConfidential(property.confidential());
		icProperty.setDisplayName(property.displayName());
		icProperty.setHelpMessage(property.helpMessage());
		icProperty.setName(property.displayName());
		icProperty.setRequired(property.required());
		icProperty.setOrder(property.order());
		
		return icProperty;
	}

	
}
