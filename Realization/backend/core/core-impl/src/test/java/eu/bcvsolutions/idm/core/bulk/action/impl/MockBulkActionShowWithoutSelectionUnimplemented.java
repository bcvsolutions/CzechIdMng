package eu.bcvsolutions.idm.core.bulk.action.impl;

import org.springframework.stereotype.Component;

/**
 * Mock bulk action for testing action setting:
 * - showWithSelection
 * - showWithoutSelection
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class MockBulkActionShowWithoutSelectionUnimplemented extends MockBulkAction {
	
	@Override
	public String getName() {
		return this.getClass().getCanonicalName();
	}
	
	@Override
	public boolean showWithoutSelection() {
		return true;
	}
}