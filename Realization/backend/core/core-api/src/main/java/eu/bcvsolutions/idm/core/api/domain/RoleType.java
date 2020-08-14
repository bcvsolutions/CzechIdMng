package eu.bcvsolutions.idm.core.api.domain;

/**
 * Role could be used for different purpose.
 * 
 * @author Radek Tomiška 
 *
 */
public enum RoleType {
	SYSTEM, // system role - provided by product CzechIdM
	BUSINESS, // role could contain technical roles
	TECHNICAL, // "leaf"
	LOGIN; // login role - for quarantine etc.
}
