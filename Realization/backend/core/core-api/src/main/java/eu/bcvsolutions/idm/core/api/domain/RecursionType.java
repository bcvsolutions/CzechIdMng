package eu.bcvsolutions.idm.core.api.domain;

/**
 * Recursion type - e.g. used in automatic roles 
 * 
 * @author Radek Tomiška 
 *
 */
public enum RecursionType {
	NO, // without recursion
	DOWN, // all children
	UP // all parents
}
